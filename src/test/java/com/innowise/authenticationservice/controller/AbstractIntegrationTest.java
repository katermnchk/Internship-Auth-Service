package com.innowise.authenticationservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.innowise.authenticationservice.repository.AuthUserDao;
import com.innowise.authenticationservice.repository.RefreshTokenDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@Testcontainers
@AutoConfigureMockMvc
public abstract class AbstractIntegrationTest {

  @Autowired
  protected MockMvc mockMvc;

  @Autowired
  protected ObjectMapper objectMapper;

  @Autowired
  protected AuthUserDao authUserDao;

  @Autowired
  protected RefreshTokenDao refreshTokenDao;

  @Autowired
  protected JdbcTemplate jdbcTemplate;


  @ServiceConnection
  public static final PostgreSQLContainer<?> POSTGRE_SQL_CONTAINER =
      new PostgreSQLContainer<>("postgres:15")
      .withDatabaseName("testdb")
      .withUsername(System.getenv().getOrDefault("DB_USER", "test"))
      .withPassword(System.getenv().getOrDefault("DB_PASSWORD", "test"));

}