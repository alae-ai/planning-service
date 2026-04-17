package com.planning.microplanning.dao;

import com.planning.microplanning.model.Creneau;

import java.util.List;

public interface CreneauDao {

    List<Creneau> findAll();

    Creneau findById(Long id);

    Creneau save(Creneau creneau);

    List<Creneau> findByMedecinId(Long medecinId);

    List<Creneau> findAvailable();
}

