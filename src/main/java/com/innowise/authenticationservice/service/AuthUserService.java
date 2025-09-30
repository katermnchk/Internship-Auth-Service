package com.innowise.authenticationservice.service;

import com.innowise.authenticationservice.dto.AuthRequestDto;
import com.innowise.authenticationservice.dto.AuthResponseDto;
import com.innowise.authenticationservice.dto.PasswordUpdateDto;
import java.util.Optional;

public interface AuthUserService {

  AuthResponseDto register(AuthRequestDto requestDto);

  Optional<AuthResponseDto> getByEmail(String email);

  Optional<AuthResponseDto> getById(Long id);

  boolean existsByEmail(String email);

  void updatePassword(PasswordUpdateDto updateDto);

}
