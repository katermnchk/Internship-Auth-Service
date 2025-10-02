package com.innowise.authenticationservice.exception;

public class InvalidOldPasswordException extends RuntimeException {

  public InvalidOldPasswordException() {
    super("Old password is incorrect");
  }

  public InvalidOldPasswordException(String message) {
    super(message);
  }
}
