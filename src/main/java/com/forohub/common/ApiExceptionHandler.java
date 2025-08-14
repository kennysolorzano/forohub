package com.forohub.common;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.sql.SQLIntegrityConstraintViolationException;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Manejador global de excepciones para respuestas JSON consistentes.
 */
@RestControllerAdvice
public class ApiExceptionHandler {

  // ---- 400 Bad Request: validación de @Valid en body (DTOs) ----
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ApiError> handleMethodArgumentNotValid(
      MethodArgumentNotValidException ex, HttpServletRequest req
  ) {
    Map<String, String> fieldErrors = ex.getBindingResult()
        .getFieldErrors()
        .stream()
        .collect(Collectors.toMap(
            FieldError::getField,
            fe -> fe.getDefaultMessage() == null ? "invalid" : fe.getDefaultMessage(),
            (a, b) -> a // si repite campo, nos quedamos con el primero
        ));

    ApiError body = ApiError.of(
        req.getRequestURI(),
        HttpStatus.BAD_REQUEST,
        "Validation failed",
        "There are invalid fields.",
        fieldErrors
    );
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
  }

  // ---- 400 Bad Request: JSON mal formado / encoding ----
  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<ApiError> handleNotReadable(
      HttpMessageNotReadableException ex, HttpServletRequest req
  ) {
    ApiError body = ApiError.of(
        req.getRequestURI(),
        HttpStatus.BAD_REQUEST,
        "Malformed JSON",
        rootMessage(ex)
    );
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
  }

  // ---- 400 Bad Request: validación en @RequestParam/@PathVariable ----
  @ExceptionHandler(ConstraintViolationException.class)
  public ResponseEntity<ApiError> handleConstraintViolation(
      ConstraintViolationException ex, HttpServletRequest req
  ) {
    Map<String, String> fieldErrors = new HashMap<>();
    for (ConstraintViolation<?> v : ex.getConstraintViolations()) {
      String field = v.getPropertyPath() == null ? "param" : v.getPropertyPath().toString();
      fieldErrors.put(field, v.getMessage());
    }
    ApiError body = ApiError.of(
        req.getRequestURI(),
        HttpStatus.BAD_REQUEST,
        "Constraint violation",
        "Invalid request parameters.",
        fieldErrors
    );
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
  }

  // ---- 409 Conflict: violación de clave/índice único ----
  @ExceptionHandler(DataIntegrityViolationException.class)
  public ResponseEntity<ApiError> handleDataIntegrity(
      DataIntegrityViolationException ex, HttpServletRequest req
  ) {
    String constraint = extractConstraintName(ex);
    String message;
    if (constraint != null && constraint.equalsIgnoreCase("uk_topics_title_author")) {
      message = "A topic with the same title and author already exists.";
    } else {
      message = "Data integrity violation.";
    }

    ApiError body = ApiError.of(
        req.getRequestURI(),
        HttpStatus.CONFLICT,
        "Conflict",
        message
    );
    return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
  }

  // ---- 403 Forbidden ----
  @ExceptionHandler(AccessDeniedException.class)
  public ResponseEntity<ApiError> handleAccessDenied(
      AccessDeniedException ex, HttpServletRequest req
  ) {
    ApiError body = ApiError.of(
        req.getRequestURI(),
        HttpStatus.FORBIDDEN,
        "Forbidden",
        "You do not have permission to access this resource."
    );
    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(body);
  }

  // ---- 500 Internal Server Error (catch-all) ----
  @ExceptionHandler(Exception.class)
  public ResponseEntity<ApiError> handleGeneric(
      Exception ex, HttpServletRequest req
  ) {
    ApiError body = ApiError.of(
        req.getRequestURI(),
        HttpStatus.INTERNAL_SERVER_ERROR,
        "Internal Server Error",
        rootMessage(ex)
    );
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
  }

  // -------- helpers --------

  private static String rootMessage(Throwable t) {
    Throwable cur = t;
    while (cur.getCause() != null) cur = cur.getCause();
    String msg = cur.getMessage();
    return msg == null || msg.isBlank() ? cur.toString() : msg;
  }

  /**
   * Intenta extraer el nombre de la restricción única desde distintas capas.
   */
  private static String extractConstraintName(DataIntegrityViolationException ex) {
    // 1) Hibernate ConstraintViolationException (si aplica)
    Throwable cause = ex.getMostSpecificCause();
    if (cause instanceof org.hibernate.exception.ConstraintViolationException h) {
      return h.getConstraintName();
    }
    // 2) MySQL/JDBC
    if (cause instanceof SQLIntegrityConstraintViolationException sqlEx) {
      String msg = sqlEx.getMessage();
      // Ej.: "Duplicate entry 'X' for key 'uk_topics_title_author'"
      if (msg != null) {
        int idx = msg.indexOf("for key '");
        if (idx >= 0) {
          int start = idx + "for key '".length();
          int end = msg.indexOf('\'', start);
          if (end > start) return msg.substring(start, end);
        }
      }
    }
    return null;
  }

  // Estructura de error uniforme
  public record ApiError(
      Instant timestamp,
      String path,
      int status,
      String error,
      String message,
      Map<String, String> fieldErrors
  ) {
    public static ApiError of(String path, HttpStatus status, String error, String message) {
      return new ApiError(Instant.now(), path, status.value(), error, message, null);
    }
    public static ApiError of(String path, HttpStatus status, String error, String message,
                              Map<String, String> fieldErrors) {
      return new ApiError(Instant.now(), path, status.value(), error, message, fieldErrors);
    }
  }
}
