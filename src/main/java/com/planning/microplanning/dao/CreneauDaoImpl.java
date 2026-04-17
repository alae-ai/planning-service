package com.planning.microplanning.dao;

import com.planning.microplanning.model.Creneau;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Repository
public class CreneauDaoImpl implements CreneauDao {

    private static final List<Creneau> DB = new ArrayList<>();
    private static final AtomicLong ID_SEQ = new AtomicLong(0);

    static {
        // Sample data for 2 doctors (medecinId = 1 and 2)
        addSample(1L, LocalDate.now().plusDays(1), LocalTime.of(9, 0), LocalTime.of(9, 30), true);
        addSample(1L, LocalDate.now().plusDays(1), LocalTime.of(9, 30), LocalTime.of(10, 0), false);
        addSample(1L, LocalDate.now().plusDays(2), LocalTime.of(14, 0), LocalTime.of(14, 30), true);

        addSample(2L, LocalDate.now().plusDays(1), LocalTime.of(10, 0), LocalTime.of(10, 30), true);
        addSample(2L, LocalDate.now().plusDays(3), LocalTime.of(11, 0), LocalTime.of(11, 30), true);
        addSample(2L, LocalDate.now().plusDays(3), LocalTime.of(11, 30), LocalTime.of(12, 0), false);
    }

    private static void addSample(Long medecinId, LocalDate date, LocalTime debut, LocalTime fin, boolean disponible) {
        Creneau c = new Creneau(ID_SEQ.incrementAndGet(), medecinId, date, debut, fin, disponible);
        DB.add(c);
    }

    @Override
    public List<Creneau> findAll() {
        return DB.stream()
                .sorted(Comparator.comparing(Creneau::getDate).thenComparing(Creneau::getHeureDebut))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    @Override
    public Creneau findById(Long id) {
        if (id == null) {
            return null;
        }
        Optional<Creneau> found = DB.stream().filter(c -> Objects.equals(c.getId(), id)).findFirst();
        return found.orElse(null);
    }

    @Override
    public Creneau save(Creneau creneau) {
        Objects.requireNonNull(creneau, "creneau must not be null");

        if (creneau.getId() == null) {
            creneau.setId(ID_SEQ.incrementAndGet());
            DB.add(creneau);
            return creneau;
        }

        for (int i = 0; i < DB.size(); i++) {
            if (Objects.equals(DB.get(i).getId(), creneau.getId())) {
                DB.set(i, creneau);
                return creneau;
            }
        }

        DB.add(creneau);
        return creneau;
    }

    @Override
    public List<Creneau> findByMedecinId(Long medecinId) {
        if (medecinId == null) {
            return List.of();
        }
        return DB.stream()
                .filter(c -> Objects.equals(c.getMedecinId(), medecinId))
                .sorted(Comparator.comparing(Creneau::getDate).thenComparing(Creneau::getHeureDebut))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    @Override
    public List<Creneau> findAvailable() {
        return DB.stream()
                .filter(Creneau::isDisponible)
                .sorted(Comparator.comparing(Creneau::getDate).thenComparing(Creneau::getHeureDebut))
                .collect(Collectors.toCollection(ArrayList::new));
    }
}

