package com.innowise.authenticationservice.service;

import com.innowise.authenticationservice.dto.AuthRequestDto;
import com.innowise.authenticationservice.dto.AuthTokensDto;
import com.innowise.authenticationservice.dto.LoginRequestDto;

public interface AuthenticationService {

  AuthTokensDto login(LoginRequestDto dto);

  AuthTokensDto  refreshToken(String refreshToken);

}
