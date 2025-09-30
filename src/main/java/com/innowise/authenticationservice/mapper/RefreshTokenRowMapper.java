package com.innowise.authenticationservice.mapper;

import com.innowise.authenticationservice.entity.RefreshToken;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.springframework.jdbc.core.RowMapper;

public class RefreshTokenRowMapper implements RowMapper<RefreshToken> {

  @Override
  public RefreshToken mapRow(ResultSet rs, int rowNum) throws SQLException {
    return new RefreshToken(
        rs.getLong("id"),
        rs.getLong("user_id"),
        rs.getString("token"),
        rs.getTimestamp("expires_at").toInstant(),
        rs.getBoolean("revoked"),
        rs.getTimestamp("created_at").toInstant()
    );
  }
}
