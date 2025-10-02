package com.innowise.authenticationservice.controller;

import com.innowise.authenticationservice.dto.response.ApiErrorResponse;
import com.innowise.authenticationservice.exception.ErrorMessages;
import com.innowise.authenticationservice.exception.InvalidOldPasswordException;
import com.innowise.authenticationservice.exception.InvalidPasswordException;
import com.innowise.authenticationservice.exception.InvalidRefreshTokenException;
import com.innowise.authenticationservice.exception.UserAlreadyExistsException;
import com.innowise.authenticationservice.exception.UserNotFoundException;
import jakarta.validation.ConstraintViolationException;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler({ UserNotFoundException.class })
  public ResponseEntity<ApiErrorResponse> handleNotFoundException(RuntimeException e) {
    log.warn("Entity not found: {}", e.getMessage(), e);
    return buildErrorResponse(
        HttpStatus.NOT_FOUND, List.of(e.getMessage()), ErrorMessages.NOT_FOUND
    );
  }

  @ExceptionHandler({
      UserAlreadyExistsException.class
  })
  public ResponseEntity<ApiErrorResponse> handleConflictException(RuntimeException e) {
    log.warn("Entity conflict: {}", e.getMessage(), e);
    return buildErrorResponse(HttpStatus.CONFLICT, List.of(e.getMessage()), ErrorMessages.CONFLICT);
  }

  @ExceptionHandler({
      InvalidPasswordException.class,
      InvalidRefreshTokenException.class
  })
  public ResponseEntity<ApiErrorResponse> handleBadRequestExceptions(RuntimeException e) {
    log.info("Bad request: {}", e.getMessage(), e);
    return buildErrorResponse(HttpStatus.BAD_REQUEST, List.of(e.getMessage()), ErrorMessages.BAD_REQUEST);
  }

  @ExceptionHandler(InvalidOldPasswordException.class)
  public ResponseEntity<ApiErrorResponse> handleInvalidOldPassword(InvalidOldPasswordException e) {
    log.info("Invalid old password: {}", e.getMessage());
    return buildErrorResponse(HttpStatus.BAD_REQUEST, List.of(e.getMessage()), ErrorMessages.BAD_REQUEST);
  }

  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<ApiErrorResponse> handleHttpMessageNotReadable(
      HttpMessageNotReadableException e) {
    log.warn("HttpMessageNotReadableException: {}", e.getMessage(), e);
    String message = "Malformed JSON request";
    return buildErrorResponse(HttpStatus.BAD_REQUEST, List.of(message), ErrorMessages.BAD_REQUEST);
  }

  @ExceptionHandler(ConstraintViolationException.class)
  public ResponseEntity<ApiErrorResponse> handleConstraintViolation(ConstraintViolationException e) {
    List<String> errors = e.getConstraintViolations().stream()
        .map(v -> v.getPropertyPath() + ": " + v.getMessage())
        .toList();
    log.info("Constraint violations: {} errors", errors.size());
    errors.forEach(error -> log.debug("Constraint violation: {}", error));
    return buildErrorResponse(HttpStatus.BAD_REQUEST, errors, ErrorMessages.BAD_REQUEST);
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ApiErrorResponse> handleValidationException(MethodArgumentNotValidException e) {
    List<String> errors = e.getBindingResult().getFieldErrors().stream()
        .map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
        .toList();
    log.info("Validation failed: {} errors", errors.size());
    errors.forEach(error -> log.debug("Validation error: {}", error));
    return buildErrorResponse(HttpStatus.BAD_REQUEST, errors, ErrorMessages.BAD_REQUEST);
  }

  @ExceptionHandler(MethodArgumentTypeMismatchException.class)
  public ResponseEntity<ApiErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException e) {
    log.info("Method argument type mismatch: parameter '{}', value '{}'", e.getName(), e.getValue());
    return buildErrorResponse(HttpStatus.BAD_REQUEST,
        List.of("Invalid parameter type: " + e.getName()),
        ErrorMessages.BAD_REQUEST);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ApiErrorResponse> handleAllExceptions(Exception e) {
    log.error("Unexpected error: {}", e.getMessage(), e);
    return buildErrorResponse(
        HttpStatus.INTERNAL_SERVER_ERROR,
        List.of("Unexpected error occurred"),
        ErrorMessages.INTERNAL_SERVER_ERROR
    );
  }

  private ResponseEntity<ApiErrorResponse> buildErrorResponse(
      HttpStatus status,
      List<String> messages,
      String error
  ) {
    ApiErrorResponse response = new ApiErrorResponse(status.value(), messages, error);
    return new ResponseEntity<>(response, status);
  }

}
