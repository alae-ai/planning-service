package com.planning.microplanning.service;

import com.planning.microplanning.model.Creneau;
import com.planning.microplanning.repository.CreneauRepository;
import com.planning.microplanning.web.error.CreneauNotFoundException;
import com.planning.microplanning.web.error.CreneauStateException;
import com.planning.microplanning.web.error.ExternalServiceUnavailableException;
import com.planning.microplanning.web.error.MedecinNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CreneauServiceImpl implements CreneauService {

    private static final Sort CRENEAU_SORT = Sort.by(Sort.Direction.ASC, "date", "heureDebut");

    private final CreneauRepository creneauRepository;
    private final boolean simulateMedecinServiceDown;

    public CreneauServiceImpl(
            CreneauRepository creneauRepository,
            @Value("${planning.simulate.medecin-service.down:false}") boolean simulateMedecinServiceDown
    ) {
        this.creneauRepository = creneauRepository;
        this.simulateMedecinServiceDown = simulateMedecinServiceDown;
    }

    @Override
    public List<Creneau> findAll() {
        return creneauRepository.findAll(CRENEAU_SORT);
    }

    @Override
    public Creneau findById(Long id) {
        return creneauRepository.findById(id).orElseThrow(() -> new CreneauNotFoundException(id));
    }

    @Override
    public List<Creneau> findDisponibles() {
        return creneauRepository.findByDisponibleTrue(CRENEAU_SORT);
    }

    @Override
    public List<Creneau> findByMedecin(Long medecinId) {
        if (medecinId == null) {
            return List.of();
        }
        return creneauRepository.findByMedecinId(medecinId, CRENEAU_SORT);
    }

    @Override
    public Creneau create(Creneau creneau) {
        if (creneau == null) {
            throw new IllegalArgumentException("Creneau must not be null");
        }
        creneau.setId(null); // Ensure new ID generated
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

        return creneauRepository.findById(id).orElseThrow(() -> new CreneauNotFoundException(id));
    }

    @Override
    @Transactional
    public Creneau liberer(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("id must not be null");
        }

        // Consistency checks (business rules)
        Creneau c = creneauRepository.findById(id).orElseThrow(() -> new CreneauNotFoundException(id));
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

        return creneauRepository.findById(id).orElseThrow(() -> new CreneauNotFoundException(id));
    }

    /**
     * Academic TP-friendly "resilience" implementation:
     * simulate an external dependency (medecin-service) directly in the service layer.
     *
     * - Normal mode: returns true (dependency assumed available and doctor exists).
     * - Failure mode (planning.simulate.medecin-service.down=true): throws a controlled 503 business exception.
     */
    private boolean checkMedecinExists(Long medecinId) {
        if (medecinId == null) {
            return false;
        }
        try {
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
