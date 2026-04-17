package com.planning.microplanning.repository;

import com.planning.microplanning.model.Creneau;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CreneauRepository extends JpaRepository<Creneau, Long> {
    List<Creneau> findByMedecinId(Long medecinId);

    List<Creneau> findByDisponibleTrue();

    // Convenience overloads so the service can keep returning slots ordered by (date, heureDebut).
    List<Creneau> findByMedecinId(Long medecinId, Sort sort);

    List<Creneau> findByDisponibleTrue(Sort sort);
}

