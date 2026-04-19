package com.planning.microplanning.service;

import com.planning.microplanning.model.Creneau;
import com.planning.microplanning.repository.CreneauRepository;
import com.planning.microplanning.service.dto.MedecinStatsDTO;
import com.planning.microplanning.web.error.CreneauNotFoundException;
import com.planning.microplanning.web.error.CreneauStateException;
import com.planning.microplanning.web.error.ExternalServiceUnavailableException;
import com.planning.microplanning.web.error.MedecinNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class CreneauServiceImpl implements CreneauService {

    private static final Logger log = LoggerFactory.getLogger(CreneauServiceImpl.class);
    private static final Sort CRENEAU_SORT = Sort.by(Sort.Direction.ASC, "date", "heureDebut");
    private static final LocalTime DEFAULT_HEURE_DEBUT = LocalTime.now();
    private static final int DEFAULT_DUREE_MINUTES = 30;

    private final CreneauRepository creneauRepository;
    private final String medecinServiceUrl;
    private final boolean simulateMedecinServiceDown;

    public CreneauServiceImpl(
            CreneauRepository creneauRepository,
            @Value("${service.medecin.url}") String medecinServiceUrl,
            @Value("${planning.simulate.medecin-service.down:false}") boolean simulateMedecinServiceDown
    ) {
        this.creneauRepository = creneauRepository;
        this.medecinServiceUrl = medecinServiceUrl;
        this.simulateMedecinServiceDown = simulateMedecinServiceDown;
    }

    @Override
    public List<Creneau> findAll() {
        return creneauRepository.findAll(CRENEAU_SORT).stream()
                .map(this::stabilizeAndPersistIfNeeded)
                .toList();
    }

    @Override
    public Creneau findById(Long id) {
        Creneau c = creneauRepository.findById(id).orElseThrow(() -> new CreneauNotFoundException(id));
        return stabilizeAndPersistIfNeeded(c);
    }

    @Override
    public List<Creneau> findDisponibles() {
        return creneauRepository.findByDisponibleTrue(CRENEAU_SORT).stream()
                .map(this::stabilizeAndPersistIfNeeded)
                // A slot can be returned by the repository as "available" while being expired.
                // After self-healing it becomes unavailable, so ensure it is not exposed here.
                .filter(Creneau::isDisponible)
                .toList();
    }

    @Override
    public List<Creneau> findByMedecin(Long medecinId) {
        if (medecinId == null) {
            return List.of();
        }
        return creneauRepository.findByMedecinId(medecinId, CRENEAU_SORT).stream()
                .map(this::stabilizeAndPersistIfNeeded)
                .toList();
    }

    @Override
    public List<MedecinStatsDTO> statsCreneauxReservesParMedecin(int year, int month) {
        if (year < 1) {
            throw new IllegalArgumentException("year must be >= 1");
        }
        if (month < 1 || month > 12) {
            throw new IllegalArgumentException("month must be between 1 and 12");
        }

        LocalDate start = LocalDate.of(year, month, 1);
        LocalDate end = start.withDayOfMonth(start.lengthOfMonth());

        Map<Long, Long> countsByMedecin = creneauRepository.findByDisponibleFalseAndDateBetween(start, end).stream()
                .collect(Collectors.groupingBy(Creneau::getMedecinId, Collectors.counting()));

        return countsByMedecin.entrySet().stream()
                .map(e -> new MedecinStatsDTO(e.getKey(), e.getValue()))
                .sorted(Comparator.comparing(MedecinStatsDTO::getMedecinId, Comparator.nullsLast(Long::compareTo)))
                .toList();
    }

    @Override
    @Transactional
    public Creneau create(Creneau creneau) {
        if (creneau == null) {
            throw new IllegalArgumentException("Creneau must not be null");
        }
        creneau.setId(null); // Ensure new ID generated
        stabilizeInPlace(creneau);
        return creneauRepository.save(creneau);
    }

    @Override
    @Transactional
    public Creneau bloquer(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("id must not be null");
        }

        // Consistency checks (business rules)
        Creneau c = creneauRepository.findById(id).orElseThrow(() -> new CreneauNotFoundException(id));
        c = stabilizeAndPersistIfNeeded(c);
        if (!c.isDisponible()) {
            throw new CreneauStateException("Creneau is already blocked (id=" + id + ").");
        }
        if (!checkMedecinExists(c.getMedecinId())) {
            throw new MedecinNotFoundException(c.getMedecinId());
        }

        // Atomic transition (handles concurrent requests safely)
        int updated = creneauRepository.bloquerIfDisponible(id);
        if (updated == 0) {
            Creneau now = creneauRepository.findById(id).orElseThrow(() -> new CreneauNotFoundException(id));
            if (!now.isDisponible()) {
                throw new CreneauStateException("Creneau is already blocked (id=" + id + ").");
            }
            throw new CreneauStateException("Cannot block creneau due to a concurrent update (id=" + id + ").");
        }

        return stabilizeAndPersistIfNeeded(creneauRepository.findById(id).orElseThrow(() -> new CreneauNotFoundException(id)));
    }

    @Override
    @Transactional
    public Creneau liberer(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("id must not be null");
        }

        // Consistency checks (business rules)
        Creneau c = creneauRepository.findById(id).orElseThrow(() -> new CreneauNotFoundException(id));
        c = stabilizeAndPersistIfNeeded(c);
        // Time-based consistency rule: an expired slot can never become available again.
        if (isExpired(c)) {
            return c;
        }
        if (c.isDisponible()) {
            throw new CreneauStateException("Creneau is already available (id=" + id + ").");
        }

        // Atomic transition (handles concurrent requests safely)
        int updated = creneauRepository.libererIfBloque(id);
        if (updated == 0) {
            Creneau now = creneauRepository.findById(id).orElseThrow(() -> new CreneauNotFoundException(id));
            if (now.isDisponible()) {
                throw new CreneauStateException("Creneau is already available (id=" + id + ").");
            }
            throw new CreneauStateException("Cannot free creneau due to a concurrent update (id=" + id + ").");
        }

        return stabilizeAndPersistIfNeeded(creneauRepository.findById(id).orElseThrow(() -> new CreneauNotFoundException(id)));
    }

    /**
     * "Self-healing" mechanism (academic):
     * - Detect null/invalid fields
     * - Correct them to safe defaults
     * - Persist the correction (service-level auto-stabilization)
     *
     * Notes:
     * - {@code disponible} is a primitive boolean, so it is never null in Java.
     * - When the slot is clearly corrupted (e.g., missing/invalid medecinId), we make it conservative:
     *   mark it as not available to avoid offering broken data as "disponible".
     */
    private Creneau stabilizeAndPersistIfNeeded(Creneau creneau) {
        if (creneau == null) {
            return null;
        }
        boolean changed = stabilizeInPlace(creneau);
        if (!changed) {
            return creneau;
        }
        Creneau saved = creneauRepository.save(creneau);
        log.warn("Auto-stabilized creneau id={} (data was inconsistent and was corrected).", saved.getId());
        return saved;
    }

    private boolean stabilizeInPlace(Creneau creneau) {
        Objects.requireNonNull(creneau, "creneau must not be null");
        boolean changed = false;

        // medecinId: if missing/invalid, keep record safe by disabling availability.
        if (creneau.getMedecinId() == null) {
            creneau.setMedecinId(0L);
            changed = true;
        }
        if (creneau.getMedecinId() != null && creneau.getMedecinId() <= 0) {
            if (creneau.isDisponible()) {
                creneau.setDisponible(false);
                changed = true;
            }
        }

        // date: default to "today" if missing.
        if (creneau.getDate() == null) {
            creneau.setDate(LocalDate.now());
            changed = true;
        }

        // heureDebut/heureFin: default + normalize ordering.
        LocalTime debut = creneau.getHeureDebut();
        LocalTime fin = creneau.getHeureFin();

        if (debut == null) {
            debut = DEFAULT_HEURE_DEBUT;
            creneau.setHeureDebut(debut);
            changed = true;
        }
        if (fin == null) {
            fin = debut.plusMinutes(DEFAULT_DUREE_MINUTES);
            creneau.setHeureFin(fin);
            changed = true;
        }
        if (debut != null && fin != null && !fin.isAfter(debut)) {
            creneau.setHeureFin(debut.plusMinutes(DEFAULT_DUREE_MINUTES));
            changed = true;
        }

        // Time-based consistency (self-healing):
        // if a slot ended in the past, it must not be available anymore.
        if (isExpired(creneau) && creneau.isDisponible()) {
            creneau.setDisponible(false);
            changed = true;
        }

        return changed;
    }

    /**
     * A creneau is considered expired if its end date/time (date + heureFin) is strictly before "now".
     * Business rule: expired slots are always treated as unavailable.
     */
    private boolean isExpired(Creneau creneau) {
        if (creneau == null) {
            return false;
        }
        LocalDate date = creneau.getDate();
        LocalTime heureFin = creneau.getHeureFin();
        if (date == null || heureFin == null) {
            return false;
        }
        return date.atTime(heureFin).isBefore(LocalDateTime.now());
    }

    /**
     * Academic TP-friendly "resilience" implementation:
     * simulate an external dependency (medecin-service) directly in the service layer.
     *
     * - Normal mode: returns true (dependency assumed available and doctor exists).
     * - Failure mode (planning.simulate.medecin-service.down=true): throws a controlled 503 business exception.
     */
    private boolean checkMedecinExists(Long medecinId) {
        if (medecinId == null || medecinId <= 0) {
            return false;
        }
        try {
            // Externalized URL (cloud-ready). In this TP version we only compose it to avoid hardcoding.
            String url = medecinServiceUrl + "/" + medecinId;
            if (url.isBlank()) {
                throw new IllegalStateException("service.medecin.url is blank");
            }

            if (simulateMedecinServiceDown) {
                // Simulate: external service down / timeout / connection refused.
                throw new RuntimeException("Simulated medecin-service failure");
            }

            // Simulate: doctor exists (no real HTTP call in this academic version).
            return true;
        } catch (RuntimeException ex) {
            throw new ExternalServiceUnavailableException("medecin-service", ex);
        }
    }
}
