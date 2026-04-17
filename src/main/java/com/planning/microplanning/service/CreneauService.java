package com.planning.microplanning.service;

import com.planning.microplanning.model.Creneau;
import java.util.List;

public interface CreneauService {
    List<Creneau> findAll();
    Creneau findById(Long id);
    List<Creneau> findDisponibles();
    List<Creneau> findByMedecin(Long medecinId);
    Creneau create(Creneau creneau);
    Creneau bloquer(Long id);
    Creneau liberer(Long id);
}

