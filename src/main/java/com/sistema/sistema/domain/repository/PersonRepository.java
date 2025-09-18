package com.sistema.sistema.domain.repository;

import com.sistema.sistema.domain.model.Person;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.Optional;

public interface PersonRepository {
    Person save(Person person);

    Optional<Person> findByDocument(String document);
}
