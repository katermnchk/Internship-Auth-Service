package com.innowise.authenticationservice.service.impl;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import io.jsonwebtoken.Claims;
import java.lang.reflect.Field;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class JwtServiceImplTest {

  private JwtServiceImpl jwtService;
  private final String secretKey = "01234567890123456789012345678901";
  private final long accessTokenExpiration = 1000L * 60 * 60;

  @BeforeEach
  void setUp() throws Exception {
    jwtService = new JwtServiceImpl();
    setField(jwtService, "secretKey", secretKey);
    setField(jwtService, "accessTokenExpiration", accessTokenExpiration);
    jwtService.init();
  }

  private void setField(Object target, String fieldName, Object value) throws Exception {
    Field field = target.getClass().getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(target, value);
  }

  @Test
  void givenUserId_whenGenerateAccessToken_thenTokenIsNotNull() {
    Long userId = 123L;

    String token = jwtService.generateAccessToken(userId);

    assertNotNull(token);
  }

  @Test
  void givenValidToken_whenValidateToken_thenReturnClaims() {
    Long userId = 123L;
    String token = jwtService.generateAccessToken(userId);

    Claims claims = jwtService.validateToken(token);

    assertAll(
        () -> assertNotNull(claims),
        () -> assertThat(claims.getSubject(), is(userId.toString()))
    );
  }

  @Test
  void givenValidToken_whenExtractUserId_thenReturnCorrectUserId() {
    Long userId = 456L;
    String token = jwtService.generateAccessToken(userId);

    Long extractedUserId = jwtService.extractUserId(token);

    assertThat(extractedUserId, is(userId));
  }

  @Test
  void givenInvalidToken_whenValidateToken_thenReturnNull() {
    String invalidToken = "invalid.token.here";

    Claims claims = jwtService.validateToken(invalidToken);

    assertNull(claims);
  }
}

