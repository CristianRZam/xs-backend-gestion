package com.sistema.sistema.domain.repository;

import com.sistema.sistema.application.dto.request.birthrecord.BirthRecordFilterList;
import com.sistema.sistema.application.dto.response.birthrecord.BirthRecordDetailDTO;
import com.sistema.sistema.application.dto.response.birthrecord.BirthRecordListDTO;
import com.sistema.sistema.infrastructure.persistence.birthrecord.BirthRecordEntity;

import java.util.List;
import java.util.Optional;

public interface BirthRecordRepository {

    List<BirthRecordListDTO> findByDeletedAtIsNull(BirthRecordFilterList request);

    BirthRecordEntity save(BirthRecordEntity birthRecord);

    Optional<BirthRecordEntity> findById(Long id);

    Optional<BirthRecordDetailDTO> findDetail(Long id);

}
