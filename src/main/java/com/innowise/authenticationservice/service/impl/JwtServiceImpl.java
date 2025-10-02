package com.innowise.authenticationservice.service.impl;

import com.innowise.authenticationservice.service.JWTService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import java.security.Key;
import java.util.Date;
import java.util.function.Function;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class JwtServiceImpl implements JWTService {

  @Value("${security.jwt.secret-key}")
  private String secretKey;

  @Value("${security.jwt.access-token-expiration}")
  private long accessTokenExpiration;

  private Key signingKey;

  @PostConstruct
  public void init() {
    this.signingKey = Keys.hmacShaKeyFor(secretKey.getBytes());
  }

  @Override
  public String generateAccessToken(Long userId) {
    return Jwts.builder()
        .setSubject(userId.toString())
        .setIssuedAt(new Date(System.currentTimeMillis()))
        .setExpiration(new Date(System.currentTimeMillis() + accessTokenExpiration))
        .signWith(signingKey, SignatureAlgorithm.HS256)
        .compact();
  }

  @Override
  public Claims validateToken(String token) {
    try {
      return Jwts.parserBuilder()
          .setSigningKey(signingKey)
          .build()
          .parseClaimsJws(token)
          .getBody();
    } catch (JwtException | IllegalArgumentException e) {
      log.error("Invalid JWT token: {}", e.getMessage());
      return null;
    }
  }

  @Override
  public Long extractUserId(String token) {
    return Long.parseLong(extractClaim(token, Claims::getSubject));
  }

  private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
    final Claims claims = extractAllClaims(token);
    return claimsResolver.apply(claims);
  }

  private Claims extractAllClaims(String token) {
    return Jwts.parserBuilder()
        .setSigningKey(signingKey)
        .build()
        .parseClaimsJws(token)
        .getBody();
  }
}
