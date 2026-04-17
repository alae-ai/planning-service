package com.planning.microplanning.service;

import com.planning.microplanning.dao.CreneauDao;
import com.planning.microplanning.model.Creneau;
import com.planning.microplanning.web.error.CreneauNotFoundException;
import com.planning.microplanning.web.error.CreneauStateException;
import com.planning.microplanning.web.error.MedecinNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CreneauServiceImpl implements CreneauService {

    @Autowired
    private CreneauDao creneauDao;

    @Override
    public List<Creneau> findAll() {
        return creneauDao.findAll();
    }

    @Override
    public Creneau findById(Long id) {
        Creneau c = creneauDao.findById(id);
        if (c == null) {
            throw new CreneauNotFoundException(id);
        }
        return c;
    }

    @Override
    public List<Creneau> findDisponibles() {
        return creneauDao.findAvailable();
    }

    @Override
    public List<Creneau> findByMedecin(Long medecinId) {
        return creneauDao.findByMedecinId(medecinId);
    }

    @Override
    public Creneau create(Creneau creneau) {
        if (creneau == null) {
            throw new IllegalArgumentException("Creneau must not be null");
        }
        creneau.setId(null); // Ensure new ID generated
        return creneauDao.save(creneau);
    }

    @Override
    public Creneau bloquer(Long id) {
        Creneau c = creneauDao.findById(id);
        if (c == null) {
            throw new CreneauNotFoundException(id);
        }
        if (!c.isDisponible()) {
            throw new CreneauStateException("Cannot block an already blocked slot (id=" + id + ").");
        }
        if (!checkMedecinExists(c.getMedecinId())) {
            throw new MedecinNotFoundException(c.getMedecinId());
        }
        c.setDisponible(false);
        return creneauDao.save(c);
    }

    @Override
    public Creneau liberer(Long id) {
        Creneau c = creneauDao.findById(id);
        if (c == null) {
            throw new CreneauNotFoundException(id);
        }
        if (c.isDisponible()) {
            throw new CreneauStateException("Cannot free an already available slot (id=" + id + ").");
        }
        c.setDisponible(true);
        return creneauDao.save(c);
    }

    private boolean checkMedecinExists(Long medecinId) {
        // TODO: Future integration with medecin-service: GET http://localhost:8082/medecins/{id}
        // Stub: always true for now
        return true;
    }
}

