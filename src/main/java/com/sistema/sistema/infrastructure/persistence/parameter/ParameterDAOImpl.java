package com.sistema.sistema.infrastructure.persistence.parameter;

import com.sistema.sistema.application.dto.request.parameter.ParameterViewRequest;
import com.sistema.sistema.domain.model.Parameter;
import com.sistema.sistema.domain.repository.ParameterRepository;
import com.sistema.sistema.infrastructure.persistence.role.RoleEntity;
import com.sistema.sistema.infrastructure.security.SecurityUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Repository
public class ParameterDAOImpl implements ParameterRepository {

    @PersistenceContext
    private EntityManager entityManager;

    private final JpaParameterRepository jpa;
    private final ParameterMapper mapper;

    public ParameterDAOImpl(JpaParameterRepository jpa, ParameterMapper mapper) {
        this.jpa = jpa;
        this.mapper = mapper;
    }

    @Override
    public List<Parameter> findByDeletedAtIsNull(ParameterViewRequest request) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<ParameterEntity> cq = cb.createQuery(ParameterEntity.class);
        Root<ParameterEntity> root = cq.from(ParameterEntity.class);

        List<Predicate> predicates = new ArrayList<>();

        // No eliminados
        predicates.add(cb.isNull(root.get("deletedAt")));

        if (request.getName() != null && !request.getName().isBlank()) {
            predicates.add(cb.like(cb.lower(root.get("name")), "%" + request.getName().toLowerCase() + "%"));
        }

        if (request.getShortName() != null && !request.getShortName().isBlank()) {
            predicates.add(cb.like(cb.lower(root.get("shortName")), "%" + request.getShortName().toLowerCase() + "%"));
        }

        if (request.getCode() != null && !request.getCode().isBlank()) {
            predicates.add(cb.like(cb.lower(root.get("code")), "%" + request.getCode().toLowerCase() + "%"));
        }

        if (request.getType() != 0) {
            predicates.add(cb.equal(root.get("type"), request.getType()));
        }

        if (request.getStatus() != null) {
            predicates.add(cb.equal(root.get("active"), request.getStatus()));
        }

        cq.where(predicates.toArray(new Predicate[0]));
        cq.orderBy(cb.asc(root.get("id")));

        return entityManager.createQuery(cq)
                .getResultList()
                .stream()
                .map(mapper::toDomain)
                .toList();
    }




    @Override
    public Parameter getParameterById(Long id) {
        ParameterEntity entity = jpa.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Parametro no encontrado con id: " + id));
        return mapper.toDomain(entity);
    }

    @Override
    public List<Parameter> getListParameterByCode(String code) {
        List<ParameterEntity> entities = jpa.findByDeletedAtIsNullAndCodeIgnoreCaseOrderByIdAsc(code);
        return mapper.toDomainList(entities);
    }

    @Override
    public List<Parameter> getAllListParameterByCode(String code) {
        List<ParameterEntity> entities = jpa.findByCodeIgnoreCaseOrderByIdAsc(code);
        return mapper.toDomainList(entities);
    }

    @Override
    public Parameter save(Parameter parameter) {
        Long maxParameterId = jpa.findMaxParameterIdByCode(parameter.getCode());
        Long nextParameterId = (maxParameterId == null ? 1 : maxParameterId + 1);

        parameter.setParameterId(nextParameterId);

        ParameterEntity entity = mapper.toEntity(parameter);
        Long currentUserId = SecurityUtil.getCurrentUserId();
        entity.setCreatedBy(currentUserId);
        entity.setCreatedAt(LocalDateTime.now());
        ParameterEntity saved = jpa.save(entity);
        return mapper.toDomain(saved);
    }


    @Override
    public Parameter update(Parameter request) {
        ParameterEntity existing = jpa.findById(request.getId())
                .orElseThrow(() -> new RuntimeException("Parámetro no encontrado, no se pudo actualizar."));

        boolean codeChanged = request.getCode() != null && !request.getCode().equals(existing.getCode());

        if (request.getName() != null) existing.setName(request.getName());
        if (request.getShortName() != null) existing.setShortName(request.getShortName());
        if (request.getParentParameterId() != null) existing.setParentParameterId(request.getParentParameterId());
        if (request.getType() != null) existing.setType(request.getType());
        if (request.getOrderNumber() != null) existing.setOrderNumber(request.getOrderNumber());

//        if (codeChanged) {
//            Long maxParameterId = jpa.findMaxParameterIdByCode(request.getCode());
//            Long nextParameterId = (maxParameterId == null ? 1 : maxParameterId + 1);
//            existing.setParameterId(nextParameterId);
//        }
        Long currentUserId = SecurityUtil.getCurrentUserId();

        existing.setModifiedBy(currentUserId);
        existing.setModifiedAt(LocalDateTime.now());

        ParameterEntity saved = jpa.save(existing);
        return mapper.toDomain(saved);
    }



    @Override
    public boolean delete(Long id) {
        ParameterEntity entity = jpa.findById(id)
                .orElseThrow(() -> new RuntimeException("Parámetro no encontrado con id: " + id));

        Long currentUserId = SecurityUtil.getCurrentUserId();
        entity.setDeletedAt(LocalDateTime.now());
        entity.setDeletedBy(currentUserId);
        jpa.save(entity);
        return true;
    }

    @Override
    public Boolean updateStatus(Long id) {
        ParameterEntity entity = jpa.findById(id)
                .orElseThrow(() -> new RuntimeException("Parámetro no encontrado con id: " + id));

        entity.setActive(!Boolean.TRUE.equals(entity.getActive()));

        Long currentUserId = SecurityUtil.getCurrentUserId();
        entity.setModifiedBy(currentUserId);
        entity.setModifiedAt(LocalDateTime.now());

        jpa.save(entity);
        return true;
    }

    @Override
    public boolean existsActiveAndNotDeletedParameter(Long parameterId, String code) {
        return jpa.existsActiveAndNotDeletedParameter(parameterId, code);
    }

}
