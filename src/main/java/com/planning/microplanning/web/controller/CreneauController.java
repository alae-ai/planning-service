package com.planning.microplanning.web.controller;

import com.planning.microplanning.service.CreneauService;
import com.planning.microplanning.model.Creneau;
import com.planning.microplanning.web.error.CreneauNotFoundException;
import com.planning.microplanning.web.error.CreneauStateException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/creneaux")
public class CreneauController {

    @Autowired
    private CreneauService creneauService;

    @GetMapping
    public List<Creneau> findAll() {
        return creneauService.findAll();
    }

    @GetMapping("/{id}")
    public Creneau findById(@PathVariable Long id) {
        return creneauService.findById(id);
    }

    @GetMapping("/disponibles")
    public List<Creneau> findDisponibles() {
        return creneauService.findDisponibles();
    }

    @GetMapping("/medecin/{medecinId}")
    public List<Creneau> findByMedecin(@PathVariable Long medecinId) {
        return creneauService.findByMedecin(medecinId);
    }

    @PostMapping
    public ResponseEntity<Creneau> create(@RequestBody Creneau creneau) {
        Creneau saved = creneauService.create(creneau);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(saved.getId())
                .toUri();

        return ResponseEntity.created(location).body(saved);
    }

    @PutMapping("/{id}/bloquer")
    public Creneau bloquer(@PathVariable Long id) {
        return creneauService.bloquer(id);
    }

    @PutMapping("/{id}/liberer")
    public Creneau liberer(@PathVariable Long id) {
        return creneauService.liberer(id);
    }
}

