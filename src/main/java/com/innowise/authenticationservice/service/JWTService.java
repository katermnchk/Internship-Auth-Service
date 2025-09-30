package com.innowise.authenticationservice.service;


public interface JWTService {

  String generateAccessToken(Long userId);

  boolean validateToken(String token);

  Long extractUserId(String token);

}
