package com.innowise.authenticationservice.repository.impl;

import com.innowise.authenticationservice.entity.RefreshToken;
import com.innowise.authenticationservice.mapper.RefreshTokenRowMapper;
import com.innowise.authenticationservice.repository.RefreshTokenDao;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class RefreshTokenDaoImpl implements RefreshTokenDao {

  private final JdbcTemplate jdbcTemplate;
  private final RefreshTokenRowMapper mapper;

  private static final class SQL {
    static final String CREATE = """
        INSERT INTO refresh_tokens (user_id, token, expires_at)
        VALUES (?, ?, ?)
        RETURNING id, user_id, token, expires_at, revoked, created_at
        """;

    static final String GET_ACTIVE = """
        SELECT id, user_id, token, expires_at, revoked, created_at
        FROM refresh_tokens
        WHERE token = ? AND revoked = FALSE AND expires_at > NOW()
        """;

    static final String GET_ALL_ACTIVE = """
        SELECT id, user_id, token, expires_at, revoked, created_at
        FROM refresh_tokens
        WHERE revoked = FALSE AND expires_at > NOW()
        """;

    static final String GET_ALL_BY_USER = """
        SELECT id, user_id, token, expires_at, revoked, created_at
        FROM refresh_tokens
        WHERE user_id = ?
        """;

    static final String REVOKE_BY_ID = """
        UPDATE refresh_tokens
        SET revoked = true
        WHERE id = ?
        """;

    static final String REVOKE_ALL_FOR_USER = """
        UPDATE refresh_tokens
        SET revoked = true
        WHERE user_id = ?
        """;
  }

  @Override
  public RefreshToken save(Long userId, String token, Instant expiresAt) {
    return jdbcTemplate.queryForObject(
        SQL.CREATE,
        mapper,
        userId,
        token,
        Timestamp.from(expiresAt)
    );
  }

  @Override
  public Optional<RefreshToken> getActive(String token) {
    return jdbcTemplate.query(
        SQL.GET_ACTIVE,
        mapper,
        token
    ).stream().findFirst();
  }

  @Override
  public List<RefreshToken> getAllActiveTokens() {
    return jdbcTemplate.query(SQL.GET_ALL_ACTIVE, mapper);
  }

  @Override
  public List<RefreshToken> getAllByUser(Long userId) {
    return jdbcTemplate.query(
        SQL.GET_ALL_BY_USER,
        mapper,
        userId
    );
  }

  @Override
  public void revokeById(Long id) {
    jdbcTemplate.update(
        SQL.REVOKE_BY_ID,
        id
    );
  }

  @Override
  public void revokeAllForUser(Long userId) {
    jdbcTemplate.update(
        SQL.REVOKE_ALL_FOR_USER,
        userId
    );
  }
}
