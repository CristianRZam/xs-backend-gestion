package com.sistema.sistema;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"com.sistema.sistema"})
public class XsSistemaGestionApplication {
    public static void main(String[] args) {
        SpringApplication.run(XsSistemaGestionApplication.class, args);
    }
}
