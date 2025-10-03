package com.innowise.authenticationservice.config;

import com.innowise.authenticationservice.mapper.AuthUserRowMapper;
import com.innowise.authenticationservice.mapper.RefreshTokenRowMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MapperConfig {

  @Bean
  public RefreshTokenRowMapper refreshTokenRowMapper() {
    return new RefreshTokenRowMapper();
  }

  @Bean
  public AuthUserRowMapper authUserRowMapper() {
    return new AuthUserRowMapper();
  }

}
