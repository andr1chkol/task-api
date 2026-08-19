package com.andr1chkol.taskapi.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.Map;

@Schema(description = "Standard API error response")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "timestamp",
        "status",
        "error",
        "message",
        "path",
        "fieldErrors"
})

public class ApiError {
    @Schema(
            description = "Timestamp when the error occurred",
            example = "2026-08-13T19:00:00",
            accessMode = Schema.AccessMode.READ_ONLY
    )
    private final LocalDateTime timestamp;

    @Schema(
            description = "HTTP status code",
            example = "400",
            accessMode = Schema.AccessMode.READ_ONLY
    )

    private final int status;

    @Schema(
            description = "HTTP error name",
            example = "Bad Request",
            accessMode = Schema.AccessMode.READ_ONLY
    )
    private final String error;

    @Schema(
            description = "Detailed error message",
            example = "Validation failed",
            accessMode = Schema.AccessMode.READ_ONLY
    )
    private final String message;

    @Schema(
            description = "Request path where the error occurred",
            example = "/tasks",
            accessMode = Schema.AccessMode.READ_ONLY
    )
    private final String path;

    @Schema(
            description = "Validation messages grouped by field name",
            example = """
                    {
                      "title": "Title must not be blank"
                    }
                    """,
            accessMode = Schema.AccessMode.READ_ONLY
    )
    private final Map<String, String> fieldErrors;


    public ApiError(LocalDateTime timestamp, int status, String error, String message, String path) {
        this.timestamp = timestamp;
        this.status = status;
        this.error = error;
        this.message = message;
        this.path = path;
        this.fieldErrors = null;
    }

    public ApiError(LocalDateTime timestamp, int status, String error, String message, String path, Map<String, String> fieldErrors) {
        this.timestamp = timestamp;
        this.status = status;
        this.error = error;
        this.message = message;
        this.path = path;
        this.fieldErrors = fieldErrors;
    }

    public String getPath() {
        return path;
    }

    public String getMessage() {
        return message;
    }

    public String getError() {
        return error;
    }

    public int getStatus() {
        return status;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public Map<String, String> getFieldErrors() {
        return fieldErrors;
    }
}
