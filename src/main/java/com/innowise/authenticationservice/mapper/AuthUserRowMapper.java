package com.innowise.authenticationservice.mapper;

import com.innowise.authenticationservice.entity.AuthUser;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.springframework.jdbc.core.RowMapper;


public class AuthUserRowMapper implements RowMapper<AuthUser> {

  @Override
  public AuthUser mapRow(ResultSet rs, int rowNum) throws SQLException {
    AuthUser user = new AuthUser();
    user.setId(rs.getLong("id"));
    user.setUserId(rs.getLong("user_id"));
    user.setEmail(rs.getString("email"));
    user.setPasswordHash(rs.getString("password_hash"));
    user.setCreatedAt(rs.getTimestamp("created_at").toInstant());
    user.setUpdatedAt(rs.getTimestamp("updated_at").toInstant());
    return user;
  }

}
