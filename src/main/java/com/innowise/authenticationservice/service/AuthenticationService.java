package com.innowise.authenticationservice.service;

import com.innowise.authenticationservice.dto.AuthRequestDto;
import com.innowise.authenticationservice.dto.AuthTokensDto;

public interface AuthenticationService {

  AuthTokensDto login(AuthRequestDto dto);

  AuthTokensDto  refreshToken(String refreshToken);

}
