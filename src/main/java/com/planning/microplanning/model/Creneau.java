package com.planning.microplanning.model;

import java.time.LocalDate;
import java.time.LocalTime;

public class Creneau {

    private Long id;
    private Long medecinId;
    private LocalDate date;
    private LocalTime heureDebut;
    private LocalTime heureFin;
    private boolean disponible;

    public Creneau() {
    }

    public Creneau(Long medecinId, LocalDate date, LocalTime heureDebut, LocalTime heureFin, boolean disponible) {
        this.medecinId = medecinId;
        this.date = date;
        this.heureDebut = heureDebut;
        this.heureFin = heureFin;
        this.disponible = disponible;
    }

    public Creneau(Long id, Long medecinId, LocalDate date, LocalTime heureDebut, LocalTime heureFin, boolean disponible) {
        this.id = id;
        this.medecinId = medecinId;
        this.date = date;
        this.heureDebut = heureDebut;
        this.heureFin = heureFin;
        this.disponible = disponible;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getMedecinId() {
        return medecinId;
    }

    public void setMedecinId(Long medecinId) {
        this.medecinId = medecinId;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public LocalTime getHeureDebut() {
        return heureDebut;
    }

    public void setHeureDebut(LocalTime heureDebut) {
        this.heureDebut = heureDebut;
    }

    public LocalTime getHeureFin() {
        return heureFin;
    }

    public void setHeureFin(LocalTime heureFin) {
        this.heureFin = heureFin;
    }

    public boolean isDisponible() {
        return disponible;
    }

    public void setDisponible(boolean disponible) {
        this.disponible = disponible;
    }

    @Override
    public String toString() {
        return "Creneau{" +
                "id=" + id +
                ", medecinId=" + medecinId +
                ", date=" + date +
                ", heureDebut=" + heureDebut +
                ", heureFin=" + heureFin +
                ", disponible=" + disponible +
                '}';
    }
}

