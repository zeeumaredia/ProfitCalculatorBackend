package com.profitcalculator.dascher.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.net.URI;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Central error handler for all controllers. Maps exceptions to RFC 7807 ProblemDetail responses so
 * clients get consistent error shapes. Each handler logs the error and returns the appropriate HTTP
 * status.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

  // Handles missing shipment or cost type — returns 404
  @ExceptionHandler(ResourceNotFoundException.class)
  public ProblemDetail onNotFound(ResourceNotFoundException ex, HttpServletRequest req) {
    log.error("Not found: {}", ex.getMessage());
    ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
    pd.setTitle("Resource Not Found");
    pd.setInstance(URI.create(req.getRequestURI()));
    return pd;
  }

  // Handles @Valid failures on request bodies (e.g. missing shipmentRef) — returns 400 with
  // field-level errors
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ProblemDetail onValidationError(
      MethodArgumentNotValidException ex, HttpServletRequest req) {
    Map<String, String> errors =
        ex.getBindingResult().getFieldErrors().stream()
            .collect(
                Collectors.toMap(FieldError::getField, FieldError::getDefaultMessage, (a, b) -> a));
    log.error("Validation failed on {}: {}", req.getRequestURI(), errors);
    ProblemDetail pd =
        ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Validation failed");
    pd.setTitle("Bad Request");
    pd.setInstance(URI.create(req.getRequestURI()));
    pd.setProperty("errors", errors);
    return pd;
  }

  // Handles @Validated path/query param violations (e.g. id < 1) — returns 400 with field-level
  // errors
  @ExceptionHandler(ConstraintViolationException.class)
  public ProblemDetail onConstraintViolation(
      ConstraintViolationException ex, HttpServletRequest req) {
    Map<String, String> errors =
        ex.getConstraintViolations().stream()
            .collect(
                Collectors.toMap(
                    v -> {
                      String path = v.getPropertyPath().toString();
                      return path.contains(".") ? path.substring(path.lastIndexOf('.') + 1) : path;
                    },
                    ConstraintViolation::getMessage,
                    (a, b) -> a));
    log.error("Constraint violation at {}: {}", req.getRequestURI(), errors);
    ProblemDetail pd =
        ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Validation failed");
    pd.setTitle("Bad Request");
    pd.setInstance(URI.create(req.getRequestURI()));
    pd.setProperty("errors", errors);
    return pd;
  }

  // Handles duplicate shipmentRef or other DB unique constraint violations — returns 409
  @ExceptionHandler(DataIntegrityViolationException.class)
  public ProblemDetail onDataIntegrityViolation(
      DataIntegrityViolationException ex, HttpServletRequest req) {
    log.error("Data integrity violation at {}: {}", req.getRequestURI(), ex.getMessage());
    ProblemDetail pd =
        ProblemDetail.forStatusAndDetail(
            HttpStatus.CONFLICT, "A record with the same unique value already exists.");
    pd.setTitle("Conflict");
    pd.setInstance(URI.create(req.getRequestURI()));
    return pd;
  }

  // Catch-all for anything not handled above — returns 500
  @ExceptionHandler(Exception.class)
  public ProblemDetail onUnexpected(Exception ex, HttpServletRequest req) {
    log.error("Unhandled exception at {}", req.getRequestURI(), ex);
    ProblemDetail pd =
        ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, "Something went wrong");
    pd.setTitle("Internal Server Error");
    pd.setInstance(URI.create(req.getRequestURI()));
    return pd;
  }
}
