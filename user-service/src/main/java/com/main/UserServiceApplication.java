package com.main;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.env.Environment;

@SpringBootApplication
public class UserServiceApplication {

    private static final Logger logger = LoggerFactory.getLogger(UserServiceApplication.class);

    public static void main(String[] args) {
        Environment env = SpringApplication.run(UserServiceApplication.class, args).getEnvironment();
        String port = env.getProperty("server.port", "8080");
        logger.info("Swagger UI: http://localhost:{}/swagger-ui.html", port);
        logger.info("API Docs:   http://localhost:{}/v3/api-docs", port);
    }

}