package com.innowise.authenticationservice.mapper;

import com.innowise.authenticationservice.entity.RefreshToken;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.springframework.jdbc.core.RowMapper;

public class RefreshTokenRowMapper implements RowMapper<RefreshToken> {

  @Override
  public RefreshToken mapRow(ResultSet rs, int rowNum) throws SQLException {
    RefreshToken refreshToken = new RefreshToken();
    refreshToken.setId(rs.getLong("id"));
    refreshToken.setUserId(rs.getLong("user_id"));
    refreshToken.setToken(rs.getString("token"));
    refreshToken.setExpiresAt(rs.getTimestamp("expires_at").toInstant());
    refreshToken.setRevoked(rs.getBoolean("revoked"));
    refreshToken.setCreatedAt(rs.getTimestamp("created_at").toInstant());
    return refreshToken;
  }
}
