package dev.cleanslice.platform.product;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Main Spring Boot application class for Product Service.
 * Single-module architecture with package-based separation.
 */
@SpringBootApplication
@ComponentScan(basePackages = {
    "dev.cleanslice.platform.product",
    "dev.cleanslice.platform.common"
})
@EnableJpaRepositories(basePackages = "dev.cleanslice.platform.product.infrastructure.persistence.repository")
public class ProductServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProductServiceApplication.class, args);
    }
}
