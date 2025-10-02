package com.innowise.authenticationservice.service;


import io.jsonwebtoken.Claims;

public interface JWTService {

  String generateAccessToken(Long userId);

  Claims validateToken(String token);

  Long extractUserId(String token);

}
