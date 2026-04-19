package com.planning.microplanning.service.dto;

public class MedecinStatsDTO {

    private Long medecinId;
    private long nbCreneauxReserves;

    public MedecinStatsDTO() {
    }

    public MedecinStatsDTO(Long medecinId, long nbCreneauxReserves) {
        this.medecinId = medecinId;
        this.nbCreneauxReserves = nbCreneauxReserves;
    }

    public Long getMedecinId() {
        return medecinId;
    }

    public void setMedecinId(Long medecinId) {
        this.medecinId = medecinId;
    }

    public long getNbCreneauxReserves() {
        return nbCreneauxReserves;
    }

    public void setNbCreneauxReserves(long nbCreneauxReserves) {
        this.nbCreneauxReserves = nbCreneauxReserves;
    }
}

