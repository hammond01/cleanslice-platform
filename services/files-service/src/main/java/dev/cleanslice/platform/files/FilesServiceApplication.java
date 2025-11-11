package dev.cleanslice.platform.files;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main Spring Boot application class for Files Service.
 * Single-module architecture with package-based clean architecture.
 */
@SpringBootApplication
public class FilesServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(FilesServiceApplication.class, args);
    }
}
