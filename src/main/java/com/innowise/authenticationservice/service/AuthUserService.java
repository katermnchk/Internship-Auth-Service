package com.innowise.authenticationservice.service;

import com.innowise.authenticationservice.dto.AuthRequestDto;
import com.innowise.authenticationservice.dto.AuthResponseDto;
import com.innowise.authenticationservice.dto.LoginRequestDto;
import com.innowise.authenticationservice.dto.PasswordUpdateDto;
import com.innowise.authenticationservice.dto.RegistrationRequestDto;
import java.util.Optional;

public interface AuthUserService {

  AuthResponseDto register(RegistrationRequestDto requestDto);

  AuthResponseDto login(LoginRequestDto requestDto);

  Optional<AuthResponseDto> getByEmail(String email);

  Optional<AuthResponseDto> getById(Long id);

  boolean existsByEmail(String email);

  void updatePassword(PasswordUpdateDto updateDto);

}
