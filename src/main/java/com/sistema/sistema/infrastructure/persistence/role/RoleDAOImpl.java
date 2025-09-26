package com.sistema.sistema.infrastructure.persistence.role;

import com.sistema.sistema.application.dto.request.Role.RoleViewRequest;
import com.sistema.sistema.domain.model.Role;
import com.sistema.sistema.domain.repository.RoleRepository;
import com.sistema.sistema.infrastructure.security.SecurityUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Repository
public class RoleDAOImpl implements RoleRepository {

    @PersistenceContext
    private EntityManager entityManager;

    private final JpaRoleRepository jpa;
    private final RoleMapper mapper;

    public RoleDAOImpl(RoleMapper mapper, JpaRoleRepository jpaRoleRepository) {
        this.mapper = mapper;
        this.jpa = jpaRoleRepository;
    }

    @Override
    public List<Role> findAll() {
        return jpa.findAll()
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<Role> findByDeletedAtIsNull(RoleViewRequest request) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<RoleEntity> criteria = builder.createQuery(RoleEntity.class);
        Root<RoleEntity> role = criteria.from(RoleEntity.class);

        /*
         * JOIN con rolePermissions -> permission
         * (no usamos el resultado del join para filtrar aquí, pero lo hacemos
         * para que, si el proveedor JPA lo soporta, pueda evitar N+1 al mapear
         * posteriormente; además permite hacer fetch si lo cambias más adelante).
         */
        role.join("rolePermissions", JoinType.LEFT).join("permission", JoinType.LEFT);

        // Lista de predicados (filtros)
        List<Predicate> predicates = new ArrayList<>();

        // Siempre: role.deletedAt IS NULL (no traer roles eliminados lógicamente)
        predicates.add(builder.isNull(role.get("deletedAt")));

        // Filtro por nombre (case-insensitive)
        if (request.getName() != null && !request.getName().isBlank()) {
            String pattern = "%" + request.getName().trim().toLowerCase() + "%";
            predicates.add(builder.like(builder.lower(role.get("name")), pattern));
        }

        // Filtro por descripción (case-insensitive)
        if (request.getDescription() != null && !request.getDescription().isBlank()) {
            String pattern = "%" + request.getDescription().trim().toLowerCase() + "%";
            predicates.add(builder.like(builder.lower(role.get("description")), pattern));
        }

        // Filtro por estado (activo/inactivo)
        if (request.getStatus() != null) {
            predicates.add(builder.equal(role.get("active"), request.getStatus()));
        }

        // Aplicar filtros, orden y distinct para evitar duplicados por los joins
        criteria.select(role)
                .where(predicates.toArray(new Predicate[0]))
                .orderBy(builder.asc(role.get("id")));
        criteria.distinct(true);

        // Ejecutar consulta con paginación
        TypedQuery<RoleEntity> query = entityManager.createQuery(criteria);
        query.setFirstResult(Math.max(0, request.getPage()) * Math.max(1, request.getSize()));
        query.setMaxResults(Math.max(1, request.getSize()));

        List<RoleEntity> entities = query.getResultList();

        // Convertir a dominio usando tu mapper (que ahora extrae permisos desde rolePermissions)
        return entities.stream()
                .map(mapper::toDomain)
                .toList();
    }



    @Override
    public Role getRoleById(Long id) {
        RoleEntity entity = jpa.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Rol no encontrado con id: " + id));
        return mapper.toDomain(entity);
    }


    @Override
    public Role save(Role request) {
        RoleEntity entity = mapper.toEntity(request);
        RoleEntity saved = jpa.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Boolean delete(Long id) {
        RoleEntity entity = jpa.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Rol no encontrado con id: " + id));

        entity.setDeletedAt(LocalDateTime.now());
        entity.setDeletedBy(1L);

        jpa.save(entity);
        return true;
    }

    @Override
    public Role update(Role request) {
        RoleEntity existing = jpa.findById(request.getId())
                .orElseThrow(() -> new RuntimeException("Role no encontrado"));

        if (request.getName() != null) existing.setName(request.getName());
        if (request.getDescription() != null) existing.setDescription(request.getDescription());

        RoleEntity saved = jpa.save(existing);
        return mapper.toDomain(saved);
    }

    @Override
    public Boolean updateStatus(Long id) {
        RoleEntity entity = jpa.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Rol no encontrado con id: " + id));

        Long currentUserId = SecurityUtil.getCurrentUserId();
        entity.setActive(!Boolean.TRUE.equals(entity.getActive()));
        entity.setModifiedBy(currentUserId);

        jpa.save(entity);
        return true;
    }



}
