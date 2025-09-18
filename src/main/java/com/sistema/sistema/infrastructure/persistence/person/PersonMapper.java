package com.sistema.sistema.infrastructure.persistence.person;

import com.sistema.sistema.domain.model.Person;
import org.springframework.stereotype.Component;

@Component
public class PersonMapper {
    public Person toDomain(PersonEntity e) {
        if (e == null) return null;
        return Person.builder()
                .id(e.getId())
                .typeDocument(e.getTypeDocument())
                .document(e.getDocument())
                .fullName(e.getFullName())
                .phone(e.getPhone())
                .address(e.getAddress())
                .build();
    }

    public PersonEntity toEntity(Person p) {
        if (p == null) return null;
        return PersonEntity.builder()
                .id(p.getId())
                .typeDocument(p.getTypeDocument())
                .document(p.getDocument())
                .fullName(p.getFullName())
                .phone(p.getPhone())
                .address(p.getAddress())
                .build();
    }
}
