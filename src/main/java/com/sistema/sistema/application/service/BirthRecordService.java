package com.sistema.sistema.application.service;

import com.sistema.sistema.application.dto.request.birthrecord.BirthRecordFilterList;
import com.sistema.sistema.application.dto.request.birthrecord.BirthRecordFormRequest;
import com.sistema.sistema.application.dto.request.birthrecord.BirthRecordRequest;
import com.sistema.sistema.application.dto.request.birthrecord.BirthRecordUpdateRequest;
import com.sistema.sistema.application.dto.response.birthrecord.BirthRecordFormResponse;
import com.sistema.sistema.application.dto.response.birthrecord.BirthRecordListDTO;
import com.sistema.sistema.application.dto.response.birthrecord.BirthRecordResponse;
import com.sistema.sistema.application.dto.response.birthrecord.BirthRecordViewResponse;
import com.sistema.sistema.domain.repository.BirthRecordRepository;
import com.sistema.sistema.domain.repository.ParentRepository;
import com.sistema.sistema.domain.usecase.BirthRecordUseCase;
import com.sistema.sistema.infrastructure.persistence.birthrecord.BirthRecordEntity;
import com.sistema.sistema.infrastructure.persistence.birthrecord.BirthRecordMapper;
import com.sistema.sistema.infrastructure.persistence.parent.ParentEntity;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class BirthRecordService implements BirthRecordUseCase {

    private final BirthRecordRepository repository;
    private final ParentRepository repositoryParent;
    private final BirthRecordMapper mapper;

    public BirthRecordService(BirthRecordRepository repository, BirthRecordMapper mapper, ParentRepository repositoryParent) {
        this.repository = repository;
        this.mapper = mapper;
        this.repositoryParent = repositoryParent;
    }

    @Override
    public BirthRecordViewResponse init(BirthRecordFilterList request) {
        List<BirthRecordListDTO> birthRecords = repository.findByDeletedAtIsNull(request);

        long totalRecords = birthRecords.size();
        long maleCount = birthRecords.stream().filter(r -> "M".equalsIgnoreCase(r.getSex())).count();
        long femaleCount = birthRecords.stream().filter(r -> "F".equalsIgnoreCase(r.getSex())).count();
        long recordsThisMonth = birthRecords.stream()
                .filter(r -> r.getBirthDatetime() != null &&
                        r.getBirthDatetime().getMonth().equals(LocalDate.now().getMonth()) &&
                        r.getBirthDatetime().getYear() == LocalDate.now().getYear())
                .count();

        return BirthRecordViewResponse.builder()
                .birthRecords(birthRecords)
                .totalRecords(totalRecords)
                .maleCount(maleCount)
                .femaleCount(femaleCount)
                .recordsThisMonth(recordsThisMonth)
                .build();
    }


    @Override
    @Transactional
    public BirthRecordResponse create(BirthRecordRequest request) {

        // =============================
        // Padre
        // =============================
        ParentEntity father = null;
        if (request.getFatherId() != null) {
            father = repositoryParent.findById(request.getFatherId())
                    .orElse(null);
        } else if (request.getFatherName() != null) {
            father = ParentEntity.builder()
                    .fullName(request.getFatherName())
                    .naturalOf(request.getFatherNaturalOf())
                    .nationality(request.getFatherNationality())
                    .occupation(request.getFatherOccupation())
                    .address(request.getFatherAddress())
                    .build();

            father = repositoryParent.save(father);
        }

        // =============================
        // Madre
        // =============================
        ParentEntity mother = null;
        if (request.getMotherId() != null) {
            mother = repositoryParent.findById(request.getMotherId())
                    .orElse(null);
        } else if (request.getMotherName() != null) {
            mother = ParentEntity.builder()
                    .fullName(request.getMotherName())
                    .naturalOf(request.getMotherNaturalOf())
                    .nationality(request.getMotherNationality())
                    .occupation(request.getMotherOccupation())
                    .address(request.getMotherAddress())
                    .build();

            mother = repositoryParent.save(mother);
        }

        // =============================
        // Crear nacimiento
        // =============================
        BirthRecordEntity entity = mapper.toEntity(
                request,
                father,
                mother
        );

        BirthRecordEntity saved = repository.save(entity);

        return mapper.toResponse(saved);
    }


    @Override
    @Transactional
    public BirthRecordResponse update(BirthRecordUpdateRequest request) {

        // =============================
        // Buscar registro existente
        // =============================
        BirthRecordEntity entity = repository.findById(request.getId())
                .orElseThrow(() -> new RuntimeException("Registro no encontrado"));

        // =============================
        // Padre
        // =============================
        ParentEntity father = null;
        if (request.getFatherId() != null) {
            father = repositoryParent.findById(request.getFatherId())
                    .orElseThrow(() -> new RuntimeException("Padre no encontrado"));
            father.setFullName(request.getFatherName());
            father.setNaturalOf(request.getFatherNaturalOf());
            father.setNationality(request.getFatherNationality());
            father.setOccupation(request.getFatherOccupation());
            father.setAddress(request.getFatherAddress());

            father = repositoryParent.save(father);
        } else {
            father = ParentEntity.builder()
                    .fullName(request.getFatherName())
                    .naturalOf(request.getFatherNaturalOf())
                    .nationality(request.getFatherNationality())
                    .occupation(request.getFatherOccupation())
                    .address(request.getFatherAddress())
                    .build();

            father = repositoryParent.save(father);
        }

        // =============================
        // Madre
        // =============================
        ParentEntity mother = null;
        if (request.getMotherId() != null) {
            mother = repositoryParent.findById(request.getMotherId())
                    .orElseThrow(() -> new RuntimeException("Madre no encontrada"));

            mother.setFullName(request.getMotherName());
            mother.setNaturalOf(request.getMotherNaturalOf());
            mother.setNationality(request.getMotherNationality());
            mother.setOccupation(request.getMotherOccupation());
            mother.setAddress(request.getMotherAddress());

            mother = repositoryParent.save(mother);
        } else {
            mother = ParentEntity.builder()
                    .fullName(request.getMotherName())
                    .naturalOf(request.getMotherNaturalOf())
                    .nationality(request.getMotherNationality())
                    .occupation(request.getMotherOccupation())
                    .address(request.getMotherAddress())
                    .build();

            mother = repositoryParent.save(mother);
        }

        // =============================
        // Actualizar nacimiento
        // =============================
        mapper.updateEntity(entity, request, father, mother);

        BirthRecordEntity saved = repository.save(entity);

        return mapper.toResponse(saved);
    }


    @Override
    public byte[] generateCertificate(Long id) {
        // Aquí generas el PDF (te dejo el hook)
        return new byte[0];
    }

    @Override
    public BirthRecordFormResponse initFormData(BirthRecordFormRequest request) {

        BirthRecordFormResponse response = new BirthRecordFormResponse();

        response.setBirthRecordDetail(
                repository.findDetail(request.getId())
                        .orElse(null)
        );

        return response;
    }
}
