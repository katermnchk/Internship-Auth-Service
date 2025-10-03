package com.innowise.authenticationservice.exception;

public class InvalidRefreshTokenException extends RuntimeException {

  public InvalidRefreshTokenException() {

    super("Invalid or expired refresh token");
  }
}
