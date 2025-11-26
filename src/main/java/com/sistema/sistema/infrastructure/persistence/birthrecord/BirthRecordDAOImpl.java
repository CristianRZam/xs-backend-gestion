package com.sistema.sistema.infrastructure.persistence.birthrecord;

import com.sistema.sistema.application.dto.request.birthrecord.BirthRecordFilterList;
import com.sistema.sistema.application.dto.response.birthrecord.BirthRecordDetailDTO;
import com.sistema.sistema.application.dto.response.birthrecord.BirthRecordListDTO;
import com.sistema.sistema.domain.repository.BirthRecordRepository;
import org.springframework.stereotype.Repository;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Repository
public class BirthRecordDAOImpl implements BirthRecordRepository {

    private final JpaBirthRecordRepository jpa;
    private final BirthRecordMapper mapper;

    public BirthRecordDAOImpl(JpaBirthRecordRepository jpa, BirthRecordMapper mapper) {
        this.jpa = jpa;
        this.mapper = mapper;
    }

    @Override
    public List<BirthRecordListDTO> findByDeletedAtIsNull(BirthRecordFilterList request) {

        var result = jpa.filter(
                request.getYear(),
                request.getActNumber(),
                request.getPersonName(),
                request.getSex(),
                request.getFatherName(),
                request.getMotherName(),
                request.getBirthDateFrom(),
                request.getBirthDateTo(),
                request.getRecordDateFrom(),
                request.getRecordDateTo()
        );

        return result.stream()
                .map(mapper::toListDto)
                .toList();
    }


    @Override
    public BirthRecordEntity save(BirthRecordEntity birthRecord) {
        return jpa.save(birthRecord);
    }

    @Override
    public Optional<BirthRecordEntity> findById(Long id) {
        return jpa.findById(id);
    }

    @Override
    public Optional<BirthRecordDetailDTO> findDetail(Long id) {
        Object result = jpa.findDetailById(id);
        if (result == null) return Optional.empty();

        // ✔ Ahora casteamos correctamente
        Object[] row = (Object[]) result;

        return Optional.of(mapper.toDetailDto(row));
    }




}
