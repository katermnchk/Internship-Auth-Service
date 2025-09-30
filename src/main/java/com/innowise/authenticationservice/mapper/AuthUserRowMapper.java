package com.innowise.authenticationservice.mapper;

import com.innowise.authenticationservice.entity.AuthUser;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.springframework.jdbc.core.RowMapper;


public class AuthUserRowMapper implements RowMapper<AuthUser> {

  @Override
  public AuthUser mapRow(ResultSet rs, int rowNum) throws SQLException {
    return new AuthUser(
        rs.getLong("id"),
        rs.getString("email"),
        rs.getString("password_hash"),
        rs.getTimestamp("created_at").toInstant(),
        rs.getTimestamp("updated_at").toInstant()
    );
  }

}
