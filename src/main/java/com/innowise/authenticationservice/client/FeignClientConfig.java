package com.innowise.authenticationservice.client;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignClientConfig {

  @Value("${internal.api.header-name}")
  private String headerName;

  @Value("${internal.api.secret}")
  private String secret;

  @Bean
  public RequestInterceptor internalApiAuthInterceptor() {
    return template -> template.header(headerName, secret);
  }
}