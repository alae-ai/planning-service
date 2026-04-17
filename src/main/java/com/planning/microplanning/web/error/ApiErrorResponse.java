package com.planning.microplanning.web.error;

public record ApiErrorResponse(
        String error,
        String message,
        int status
) {
}

