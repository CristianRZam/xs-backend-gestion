package com.sistema.sistema.application.common;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class FilePublicUrlBuilder {

    @Value("${app.files.public-base-url}")
    private String baseUrl;

    public String productImage(Long productId, String filename) {
        return baseUrl + "/products/" + productId + "/" + filename;
    }
}
