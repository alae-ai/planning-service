package com.planning.microplanning.web.error;

public class CreneauNotFoundException extends RuntimeException {

    private final Long id;

    public CreneauNotFoundException(Long id) {
        super("Creneau not found (id=" + id + ").");
        this.id = id;
    }

    public Long getId() {
        return id;
    }
}

