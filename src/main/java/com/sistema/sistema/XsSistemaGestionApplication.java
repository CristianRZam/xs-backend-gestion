package com.sistema.sistema;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.time.ZoneId;
import java.util.TimeZone;

@SpringBootApplication(scanBasePackages = {"com.sistema.sistema"})
@EnableScheduling
public class XsSistemaGestionApplication {

    private static final String DEFAULT_TIME_ZONE = "America/Lima";

    public static void main(String[] args) {
        configureDefaultTimeZone();
        SpringApplication.run(XsSistemaGestionApplication.class, args);
    }

    private static void configureDefaultTimeZone() {
        String timeZoneId = System.getenv().getOrDefault("APP_TIME_ZONE", DEFAULT_TIME_ZONE);
        TimeZone.setDefault(TimeZone.getTimeZone(ZoneId.of(timeZoneId)));
    }
}







/*
package com.sistema.sistema;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.io.Encoders;

import javax.crypto.SecretKey;

@SpringBootApplication(scanBasePackages = {"com.sistema.sistema"})
public class XsSistemaGestionApplication {

    public static void main(String[] args) {
        // Generar clave segura de 512 bits para HS512
        SecretKey key = Keys.secretKeyFor(io.jsonwebtoken.SignatureAlgorithm.HS512);
        String base64Key = Encoders.BASE64.encode(key.getEncoded());

        System.out.println("===============================================");
        System.out.println("Clave JWT segura de 512 bits (Base64):");
        System.out.println(base64Key);
        System.out.println("===============================================");

        // Inicia la aplicación Spring Boot
        SpringApplication.run(XsSistemaGestionApplication.class, args);
    }
}

 */
