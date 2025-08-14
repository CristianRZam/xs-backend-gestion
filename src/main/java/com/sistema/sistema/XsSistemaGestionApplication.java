package com.sistema.sistema;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"com.sistema.sistema"})
public class XsSistemaGestionApplication {
    public static void main(String[] args) {
        SpringApplication.run(XsSistemaGestionApplication.class, args);
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