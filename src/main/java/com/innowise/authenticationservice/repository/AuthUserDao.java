package com.innowise.authenticationservice.repository;

import com.innowise.authenticationservice.entity.AuthUser;
import java.util.Optional;

public interface AuthUserDao {

  AuthUser save(String email, String passwordHash);

  Optional<AuthUser> getUserByEmail(String email);

  Optional<AuthUser> getUserById(Long id);

  boolean existsByEmail(String email);

  void updatePassword(Long id, String newPasswordHash);

}
