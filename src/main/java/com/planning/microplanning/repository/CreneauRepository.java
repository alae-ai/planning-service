package com.planning.microplanning.repository;

import com.planning.microplanning.model.Creneau;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CreneauRepository extends JpaRepository<Creneau, Long> {
    List<Creneau> findByMedecinId(Long medecinId);

    List<Creneau> findByDisponibleTrue();

    List<Creneau> findByDisponibleFalse();

    // Convenience overloads so the service can keep returning slots ordered by (date, heureDebut).
    List<Creneau> findByMedecinId(Long medecinId, Sort sort);

    List<Creneau> findByDisponibleTrue(Sort sort);

    // Atomic state transitions for data consistency (handles concurrent requests correctly).
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update Creneau c set c.disponible=false where c.id=:id and c.disponible=true")
    int bloquerIfDisponible(@Param("id") Long id);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update Creneau c set c.disponible=true where c.id=:id and c.disponible=false")
    int libererIfBloque(@Param("id") Long id);
}
