package com.sistema.sistema.application.dto.response.productimage;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ProductImageDTO {
    private Long id;
    private String imageUrl;
    private String altText;
    private Boolean isMain;
    private Integer orderNumber;
}
