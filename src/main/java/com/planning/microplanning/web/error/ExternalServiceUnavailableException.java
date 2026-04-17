package com.planning.microplanning.web.error;

/**
 * Raised when an external dependency (another microservice) cannot be reached or returns an unexpected error.
 * This is converted into a controlled 503 JSON response by {@link ApiExceptionHandler}.
 */
public class ExternalServiceUnavailableException extends RuntimeException {

    private final String dependency;

    public ExternalServiceUnavailableException(String dependency) {
        super("Dependency unavailable: " + dependency);
        this.dependency = dependency;
    }

    public ExternalServiceUnavailableException(String dependency, Throwable cause) {
        super("Dependency unavailable: " + dependency, cause);
        this.dependency = dependency;
    }

    public String getDependency() {
        return dependency;
    }
}

