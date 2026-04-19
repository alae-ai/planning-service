package com.planning.microplanning.web.error;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class ApiExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);

    @ExceptionHandler(CreneauNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleNotFound(CreneauNotFoundException ex) {
        return build("CRENEAU_NOT_FOUND", ex.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(CreneauStateException.class)
    public ResponseEntity<ApiErrorResponse> handleConflict(CreneauStateException ex) {
        return build("CRENEAU_CONFLICT", ex.getMessage(), HttpStatus.CONFLICT);
    }

    @ExceptionHandler(MedecinNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleMedecinNotFound(MedecinNotFoundException ex) {
        return build("MEDECIN_NOT_FOUND", ex.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(ExternalServiceUnavailableException.class)
    public ResponseEntity<ApiErrorResponse> handleDependencyUnavailable(ExternalServiceUnavailableException ex) {
        // Do not leak internal details: message stays business-level.
        return build("DEPENDENCY_UNAVAILABLE", ex.getMessage(), HttpStatus.SERVICE_UNAVAILABLE);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiErrorResponse> handleBadRequest(IllegalArgumentException ex) {
        return build("BAD_REQUEST", ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler({MissingServletRequestParameterException.class, MethodArgumentTypeMismatchException.class})
    public ResponseEntity<ApiErrorResponse> handleRequestParamErrors(Exception ex) {
        return build("BAD_REQUEST", ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleUnexpected(Exception ex) {
        log.error("Unhandled exception", ex);
        return build("INTERNAL_ERROR", "Unexpected error", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private ResponseEntity<ApiErrorResponse> build(String error, String message, HttpStatus status) {
        return ResponseEntity.status(status).body(new ApiErrorResponse(error, message, status.value()));
    }
}
