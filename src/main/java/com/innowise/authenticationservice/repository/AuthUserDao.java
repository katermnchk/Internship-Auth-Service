package com.innowise.authenticationservice.repository;

import com.innowise.authenticationservice.entity.AuthUser;
import java.util.Optional;

public interface AuthUserDao {

  AuthUser save(Long userId, String email, String passwordHash);

  Optional<AuthUser> getUserByEmail(String email);

  Optional<AuthUser> getUserByUserId(Long userid);

  boolean existsByEmail(String email);

  void updatePassword(Long id, String newPasswordHash);

}
