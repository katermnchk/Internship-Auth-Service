package com.innowise.authenticationservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class InnowiseAuthentificationServiceApplication {

  public static void main(String[] args) {
    SpringApplication.run(InnowiseAuthentificationServiceApplication.class, args);
  }

}
