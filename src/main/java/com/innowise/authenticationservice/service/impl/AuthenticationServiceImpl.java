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
import java.util.UUID;
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
    String accessToken = jwtService.generateAccessToken(user.getId());

    String refreshTokenValue = UUID.randomUUID().toString();

    refreshTokenService.revokeAllForUser(user.getId());

    RefreshTokenDto refreshTokenDto = new RefreshTokenDto(user.getId(), refreshTokenValue, refreshTokenExpiration);
    refreshTokenService.createToken(refreshTokenDto);

    return new AuthTokensDto(accessToken, refreshTokenValue);

  }

  @Override
  @Transactional
  public AuthTokensDto refreshToken(String refreshTokenValue) {
    RefreshToken refreshToken = refreshTokenService.validateToken(refreshTokenValue)
        .orElseThrow(InvalidRefreshTokenException::new);

    refreshTokenService.revokeToken(refreshToken.getId());

    String newAccessToken = jwtService.generateAccessToken(refreshToken.getUserId());

    String newRefreshTokenValue = UUID.randomUUID().toString();
    RefreshTokenDto newRefreshTokenDto = new RefreshTokenDto(refreshToken.getUserId(), newRefreshTokenValue, refreshTokenExpiration);
    refreshTokenService.createToken(newRefreshTokenDto);

    return new AuthTokensDto(newAccessToken, newRefreshTokenValue);
  }


}
