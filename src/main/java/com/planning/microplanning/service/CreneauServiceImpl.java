package com.planning.microplanning.service;

import com.planning.microplanning.model.Creneau;
import com.planning.microplanning.repository.CreneauRepository;
import com.planning.microplanning.web.error.CreneauNotFoundException;
import com.planning.microplanning.web.error.CreneauStateException;
import com.planning.microplanning.web.error.MedecinNotFoundException;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CreneauServiceImpl implements CreneauService {

    private static final Sort CRENEAU_SORT = Sort.by(Sort.Direction.ASC, "date", "heureDebut");

    private final CreneauRepository creneauRepository;

    public CreneauServiceImpl(CreneauRepository creneauRepository) {
        this.creneauRepository = creneauRepository;
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
    public Creneau bloquer(Long id) {
        Creneau c = creneauRepository.findById(id).orElseThrow(() -> new CreneauNotFoundException(id));
        if (!c.isDisponible()) {
            throw new CreneauStateException("Cannot block an already blocked slot (id=" + id + ").");
        }
        if (!checkMedecinExists(c.getMedecinId())) {
            throw new MedecinNotFoundException(c.getMedecinId());
        }
        c.setDisponible(false);
        return creneauRepository.save(c);
    }

    @Override
    public Creneau liberer(Long id) {
        Creneau c = creneauRepository.findById(id).orElseThrow(() -> new CreneauNotFoundException(id));
        if (c.isDisponible()) {
            throw new CreneauStateException("Cannot free an already available slot (id=" + id + ").");
        }
        c.setDisponible(true);
        return creneauRepository.save(c);
    }

    private boolean checkMedecinExists(Long medecinId) {
        // TODO: Future integration with medecin-service: GET http://localhost:8082/medecins/{id}
        // Stub: always true for now
        return true;
    }
}

