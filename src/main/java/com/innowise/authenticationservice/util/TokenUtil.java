package com.innowise.authenticationservice.util;

import com.innowise.authenticationservice.dto.AuthResponseDto;
import com.innowise.authenticationservice.service.JWTService;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TokenUtil {

  public static Map<String, String> generateTokens(AuthResponseDto user, JWTService jwtService) {
    String accessToken = jwtService.generateAccessToken(user.getUserId());
    String refreshToken = UUID.randomUUID().toString();

    Map<String, String> tokens = new HashMap<>();
    tokens.put("accessToken", accessToken);
    tokens.put("refreshToken", refreshToken);
    return tokens;
  }

}
