package com.sistema.sistema.infrastructure.persistence.parameter;

import com.sistema.sistema.application.dto.request.parameter.ParameterViewRequest;
import com.sistema.sistema.domain.model.Parameter;
import com.sistema.sistema.domain.repository.ParameterRepository;
import com.sistema.sistema.infrastructure.persistence.role.RoleEntity;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public class ParameterDAOImpl implements ParameterRepository {

    private final JpaParameterRepository jpa;
    private final ParameterMapper mapper;

    public ParameterDAOImpl(JpaParameterRepository jpa, ParameterMapper mapper) {
        this.jpa = jpa;
        this.mapper = mapper;
    }

    @Override
    public List<Parameter> findByDeletedAtIsNull(ParameterViewRequest request) {
        List<ParameterEntity> entities = jpa.findFiltered(
                request.getName(),
                request.getShortName(),
                request.getCode(),
                request.getType() == 0 ? null : request.getType()
        );

        return mapper.toDomainList(entities);
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
    public Parameter save(Parameter parameter) {
        Long maxParameterId = jpa.findMaxParameterIdByCode(parameter.getCode());
        Long nextParameterId = (maxParameterId == null ? 1 : maxParameterId + 1);

        parameter.setParameterId(nextParameterId);

        ParameterEntity entity = mapper.toEntity(parameter);
        ParameterEntity saved = jpa.save(entity);
        return mapper.toDomain(saved);
    }


    @Override
    public Parameter update(Parameter request) {
        ParameterEntity existing = jpa.findById(request.getId())
                .orElseThrow(() -> new RuntimeException("Parámetro no encontrado, no se pudo actualizar."));

        boolean codeChanged = request.getCode() != null && !request.getCode().equals(existing.getCode());

        if (request.getCode() != null) existing.setCode(request.getCode());
        if (request.getName() != null) existing.setName(request.getName());
        if (request.getShortName() != null) existing.setShortName(request.getShortName());
        if (request.getParentParameterId() != null) existing.setParentParameterId(request.getParentParameterId());
        if (request.getType() != null) existing.setType(request.getType());
        if (request.getOrderNumber() != null) existing.setOrderNumber(request.getOrderNumber());

        if (codeChanged) {
            Long maxParameterId = jpa.findMaxParameterIdByCode(request.getCode());
            Long nextParameterId = (maxParameterId == null ? 1 : maxParameterId + 1);
            existing.setParameterId(nextParameterId);
        }

        existing.setModifiedBy(1L);

        ParameterEntity saved = jpa.save(existing);
        return mapper.toDomain(saved);
    }



    @Override
    public boolean delete(Long id) {
        ParameterEntity entity = jpa.findById(id)
                .orElseThrow(() -> new RuntimeException("Parámetro no encontrado con id: " + id));

        entity.setDeletedAt(LocalDateTime.now());
        entity.setDeletedBy(1L);
        jpa.save(entity);
        return true;
    }

    @Override
    public Boolean updateStatus(Long id) {
        ParameterEntity entity = jpa.findById(id)
                .orElseThrow(() -> new RuntimeException("Parámetro no encontrado con id: " + id));

        entity.setActive(!Boolean.TRUE.equals(entity.getActive()));
        jpa.save(entity);
        return true;
    }

}
