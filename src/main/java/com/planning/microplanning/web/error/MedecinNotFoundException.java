package com.planning.microplanning.web.error;

public class MedecinNotFoundException extends RuntimeException {

    private final Long id;

    public MedecinNotFoundException(Long id) {
        super("Medecin not found (id=" + id + ").");
        this.id = id;
    }

    public Long getId() {
        return id;
    }
}

