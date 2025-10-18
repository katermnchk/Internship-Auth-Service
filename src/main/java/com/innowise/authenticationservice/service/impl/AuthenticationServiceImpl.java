package com.innowise.authenticationservice.service.impl;

import com.innowise.authenticationservice.dto.AuthRequestDto;
import com.innowise.authenticationservice.dto.AuthResponseDto;
import com.innowise.authenticationservice.dto.AuthTokensDto;
import com.innowise.authenticationservice.dto.RefreshTokenDto;
import com.innowise.authenticationservice.entity.RefreshToken;
import com.innowise.authenticationservice.exception.InvalidRefreshTokenException;
import com.innowise.authenticationservice.service.AuthUserService;
import com.innowise.authenticationservice.service.AuthenticationService;
import com.innowise.authenticationservice.service.JWTService;
import com.innowise.authenticationservice.service.RefreshTokenService;
import com.innowise.authenticationservice.util.TokenUtil;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {

  private final AuthUserService authUserService;
  private final RefreshTokenService refreshTokenService;
  private final JWTService jwtService;

  @Value("${security.jwt.refresh-token-expiration}")
  private long refreshTokenExpiration;

  @Override
  @Transactional
  public AuthTokensDto login(AuthRequestDto dto) {
    AuthResponseDto user = authUserService.login(dto);

    Map<String, String> tokens = TokenUtil.generateTokens(user, jwtService);
    String accessToken = tokens.get("accessToken");
    String refreshTokenValue = tokens.get("refreshToken");

    refreshTokenService.revokeAllForUser(user.getUserId());

    RefreshTokenDto refreshTokenDto =
        new RefreshTokenDto(user.getUserId(), refreshTokenValue, refreshTokenExpiration);
    refreshTokenService.createToken(refreshTokenDto);

    return new AuthTokensDto(accessToken, refreshTokenValue);

  }

  @Override
  @Transactional
  public AuthTokensDto refreshToken(String refreshTokenValue) {
    RefreshToken refreshToken = refreshTokenService.validateToken(refreshTokenValue)
        .orElseThrow(InvalidRefreshTokenException::new);

    refreshTokenService.revokeToken(refreshToken.getId());

    AuthResponseDto user = new AuthResponseDto(null, refreshToken.getUserId(), null);
    Map<String, String> tokens = TokenUtil.generateTokens(user, jwtService);
    String newAccessToken = tokens.get("accessToken");
    String newRefreshTokenValue = tokens.get("refreshToken");

    RefreshTokenDto newRefreshTokenDto =
        new RefreshTokenDto(refreshToken.getUserId(), newRefreshTokenValue, refreshTokenExpiration);
    refreshTokenService.createToken(newRefreshTokenDto);

    return new AuthTokensDto(newAccessToken, newRefreshTokenValue);
  }


}
