package dev.cleanslice.platform.files;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {
    "dev.cleanslice.platform.files",
    "dev.cleanslice.platform.common"
})
public class FilesServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(FilesServiceApplication.class, args);
    }
}
