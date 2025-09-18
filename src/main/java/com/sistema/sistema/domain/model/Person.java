package com.sistema.sistema.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Person {
    private Long id;
    private Long typeDocument;
    private String typeDocumentName;
    private String document;
    private String fullName;
    private String phone;
    private String address;
}
