package com.innowise.authenticationservice.repository.impl;

import com.innowise.authenticationservice.entity.AuthUser;
import com.innowise.authenticationservice.mapper.AuthUserRowMapper;
import com.innowise.authenticationservice.repository.AuthUserDao;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;


@Repository
@RequiredArgsConstructor
public class AuthUserDaoImpl implements AuthUserDao {

  private final JdbcTemplate jdbcTemplate;
  private final AuthUserRowMapper mapper = new AuthUserRowMapper();

  public static final class SQL {
    static final String CREATE_USER = """
        INSERT INTO auth_users (user_id, email, password_hash)
        VALUES (?, ?, ?)
        RETURNING id, email, password_hash, created_at, updated_at
        """;

    static final String GET_USER_BY_ID = """
        SELECT id, email, password_hash, created_at, updated_at
        FROM auth_users
        WHERE id = ?
        """;

    static final String GET_USER_BY_EMAIL = """
        SELECT id, email, password_hash, created_at, updated_at
        FROM auth_users
        WHERE email = ?
        """;

    static final String EXISTS_BY_EMAIL = """
       SELECT COUNT(*) FROM auth_users
       WHERE email = ?
       """;

    static final String UPDATE_PASSWORD = """
        UPDATE auth_users
        SET password_hash = ?, updated_at = NOW()
        WHERE id = ?
        """;
  }


  @Override
  public AuthUser save(Long userId, String email, String passwordHash) {
    return jdbcTemplate.queryForObject(
        SQL.CREATE_USER,
        mapper,
        userId,
        email,
        passwordHash
    );
  }

  @Override
  public Optional<AuthUser> getUserById(Long id) {
    return jdbcTemplate.query(
        SQL.GET_USER_BY_ID,
        mapper,
        id
    ).stream().findFirst();
  }

  @Override
  public Optional<AuthUser> getUserByEmail(String email) {
    return jdbcTemplate.query(
        SQL.GET_USER_BY_EMAIL,
        mapper,
        email
    ).stream().findFirst();
  }

  @Override
  public boolean existsByEmail(String email) {
    Integer count = jdbcTemplate.queryForObject(
        SQL.EXISTS_BY_EMAIL,
        Integer.class,
        email
    );
    return count != null && count > 0;
  }

  @Override
  public void updatePassword(Long id, String newPasswordHash) {
    jdbcTemplate.update(
        SQL.UPDATE_PASSWORD,
        newPasswordHash,
        id
    );
  }

}
