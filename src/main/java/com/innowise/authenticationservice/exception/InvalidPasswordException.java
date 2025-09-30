package com.innowise.authenticationservice.exception;

public class InvalidPasswordException extends RuntimeException {

  public InvalidPasswordException() {
    super("Invalid password");
  }
}
