package com.mobile.backend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;


@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(RuntimeException.class)
  public ResponseEntity<ErrorResponse> handleRuntimeException(RuntimeException ex) {
    if (ex.getMessage().contains("Invalid credentials")) {
      return ResponseEntity
        .status(HttpStatus.UNAUTHORIZED)
        .body(new ErrorResponse(
          LocalDateTime.now(),
          HttpStatus.UNAUTHORIZED.value(),
          "Unauthorized",
          "Invalid email or password"
        ));
    }
    
    return ResponseEntity
      .status(HttpStatus.INTERNAL_SERVER_ERROR)
      .body(new ErrorResponse(
        LocalDateTime.now(),
        HttpStatus.INTERNAL_SERVER_ERROR.value(),
        "Internal Server Error",
        ex.getMessage()
      ));
  }

  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex) {
    return ResponseEntity
      .status(HttpStatus.BAD_REQUEST)
      .body(new ErrorResponse(
        LocalDateTime.now(),
        HttpStatus.BAD_REQUEST.value(),
        "Bad Request",
        ex.getMessage()
      ));
  }

  @ExceptionHandler(org.springframework.dao.DataIntegrityViolationException.class)
  public ResponseEntity<ErrorResponse> handleDataIntegrityViolation(
    org.springframework.dao.DataIntegrityViolationException ex
  ) {
    String message = "Database constraint violation";
    
    if (ex.getMessage().contains("username")) {
      message = "Username is required";
    } else if (ex.getMessage().contains("email")) {
      message = "Invalid email format";
    }
    
    return ResponseEntity
      .status(HttpStatus.BAD_REQUEST)
      .body(new ErrorResponse(
        LocalDateTime.now(),
        HttpStatus.BAD_REQUEST.value(),
        "Bad Request",
        message
      ));
  }

  public record ErrorResponse(
    LocalDateTime timestamp,
    int status,
    String error,
    String message
  ) {}
}