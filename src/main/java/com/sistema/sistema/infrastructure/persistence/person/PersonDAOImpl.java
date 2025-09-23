package com.sistema.sistema.infrastructure.persistence.person;

import com.sistema.sistema.domain.model.Person;
import com.sistema.sistema.domain.repository.PersonRepository;
import com.sistema.sistema.infrastructure.persistence.user.UserEntity;
import com.sistema.sistema.infrastructure.security.SecurityUtil;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public class PersonDAOImpl implements PersonRepository {

    private final JpaPersonRepository jpa;
    private final PersonMapper mapper;

    public PersonDAOImpl(JpaPersonRepository jpa, PersonMapper mapper) {
        this.jpa = jpa;
        this.mapper = mapper;
    }

    @Override
    public Person save(Person person) {
        PersonEntity entity = mapper.toEntity(person);
        Long currentUserId = SecurityUtil.getCurrentUserId();
        if (entity.getId() == null) {
            entity.setCreatedBy(currentUserId);
        }
        entity.setModifiedBy(currentUserId);

        PersonEntity saved = jpa.save(entity);
        return mapper.toDomain(saved);
    }


    @Override
    public Optional<Person> findByDocument(String document) {
        return jpa.findByDocumentAndDeletedAtIsNull(document)
                .map(mapper::toDomain);
    }

    @Override
    public boolean delete(Long id) {
        // 1. Buscar usuario
        PersonEntity entity = jpa.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Persona no encontrado con id: " + id));

        // 2. Recuperar el ID del usuario logueado desde el SecurityContext
        Long currentUserId = SecurityUtil.getCurrentUserId();

        // 3. Marcar como eliminado
        entity.setDeletedAt(LocalDateTime.now());
        entity.setDeletedBy(currentUserId);

        // 4. Guardar cambios
        jpa.save(entity);

        return true;
    }

}
