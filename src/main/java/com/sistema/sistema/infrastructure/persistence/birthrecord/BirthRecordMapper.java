package com.sistema.sistema.infrastructure.persistence.birthrecord;

import com.sistema.sistema.application.dto.request.birthrecord.BirthRecordRequest;
import com.sistema.sistema.application.dto.request.birthrecord.BirthRecordUpdateRequest;
import com.sistema.sistema.application.dto.response.birthrecord.BirthRecordDetailDTO;
import com.sistema.sistema.application.dto.response.birthrecord.BirthRecordListDTO;
import com.sistema.sistema.application.dto.response.birthrecord.BirthRecordResponse;
import com.sistema.sistema.infrastructure.persistence.district.DistrictEntity;
import com.sistema.sistema.infrastructure.persistence.parent.ParentEntity;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Arrays;

@Component
public class BirthRecordMapper {

    public BirthRecordEntity toEntity(BirthRecordRequest request, ParentEntity father, ParentEntity mother) {

        // Distrito por defecto (ID = 1)
        DistrictEntity defaultDistrict = DistrictEntity.builder()
                .id(1L)
                .build();

        return BirthRecordEntity.builder()
                .year(request.getYear())
                .actNumber(request.getActNumber())
                .personName(request.getPersonName())
                .sex(request.getSex())
                .birthPlace(request.getBirthPlace())
                .birthDatetime(request.getBirthDatetime())

                .district(defaultDistrict)

                .father(father)
                .mother(mother)

                .declarantName(request.getDeclarantName())
                .declarantDocument(request.getDeclarantDocument())
                .recordPlace(request.getRecordPlace())
                .recordDatetime(request.getRecordDatetime())
                .createdBy(request.getCreatedBy())
                .build();
    }



    public void updateEntity(BirthRecordEntity entity, BirthRecordUpdateRequest request,
                             ParentEntity father, ParentEntity mother) {

        entity.setYear(request.getYear());
        entity.setActNumber(request.getActNumber());
        entity.setPersonName(request.getPersonName());
        entity.setSex(request.getSex());
        entity.setBirthPlace(request.getBirthPlace());
        entity.setBirthDatetime(request.getBirthDatetime());

        if (father != null) entity.setFather(father);
        if (mother != null) entity.setMother(mother);

        entity.setRecordPlace(request.getRecordPlace());
        entity.setRecordDatetime(request.getRecordDatetime());
        entity.setModifiedBy(request.getModifiedBy());
        entity.setModifiedAt(LocalDateTime.now());
    }


    public BirthRecordResponse toResponse(BirthRecordEntity entity) {
        return BirthRecordResponse.builder()
                .id(entity.getId())
                .year(entity.getYear())
                .actNumber(entity.getActNumber())
                .personName(entity.getPersonName())
                .sex(entity.getSex())
                .birthPlace(entity.getBirthPlace())
                .birthDatetime(entity.getBirthDatetime())
                .districtId(entity.getDistrict() != null ? entity.getDistrict().getId() : null)
                .districtName(entity.getDistrict() != null ? entity.getDistrict().getName() : null)
                .fatherId(entity.getFather() != null ? entity.getFather().getId() : null)
                .fatherName(entity.getFather() != null ? entity.getFather().getFullName() : null)
                .motherId(entity.getMother() != null ? entity.getMother().getId() : null)
                .motherName(entity.getMother() != null ? entity.getMother().getFullName() : null)
                .declarantName(entity.getDeclarantName())
                .declarantDocument(entity.getDeclarantDocument())
                .recordPlace(entity.getRecordPlace())
                .recordDatetime(entity.getRecordDatetime())
                .createdAt(entity.getCreatedAt())
                .modifiedAt(entity.getModifiedAt())
                .build();
    }


    public BirthRecordListDTO toListDto(Object[] row) {
        return BirthRecordListDTO.builder()
                .id(((Number) row[0]).longValue())
                .year(row[1] != null ? ((Number) row[1]).intValue() : null)
                .actNumber((String) row[2])
                .personName((String) row[3])
                .sex((String) row[4])
                .birthPlace((String) row[5])
                .birthDatetime(row[6] != null ? ((java.sql.Timestamp) row[6]).toLocalDateTime() : null)
                .districtId(row[7] != null ? ((Number) row[7]).longValue() : null)
                .fatherName((String) row[8])
                .motherName((String) row[9])
                .declarantName((String) row[10])
                .declarantDocument((String) row[11])
                .recordPlace((String) row[12])
                .recordDatetime(row[13] != null ? ((java.sql.Timestamp) row[13]).toLocalDateTime() : null)
                .build();
    }


    public BirthRecordDetailDTO toDetailDto(Object[] row) {

        return BirthRecordDetailDTO.builder()
                .id(row[0] != null ? ((Number) row[0]).longValue() : null)
                .year(row[1] != null ? ((Number) row[1]).intValue() : null)
                .actNumber((String) row[2])
                .personName((String) row[3])
                .sex((String) row[4])
                .birthPlace((String) row[5])
                .birthDatetime(row[6] != null ? ((Timestamp) row[6]).toLocalDateTime() : null)

                .districtId(row[7] != null ? ((Number) row[7]).longValue() : null)
                .districtName((String) row[8])
                .districtProvince((String) row[9])
                .districtDepartment((String) row[10])

                .fatherId(row[11] != null ? ((Number) row[11]).longValue() : null)
                .fatherName((String) row[12])
                .fatherNaturalOf((String) row[13])
                .fatherNationality((String) row[14])
                .fatherOccupation((String) row[15])
                .fatherAddress((String) row[16])

                .motherId(row[17] != null ? ((Number) row[17]).longValue() : null)
                .motherName((String) row[18])
                .motherNaturalOf((String) row[19])
                .motherNationality((String) row[20])
                .motherOccupation((String) row[21])
                .motherAddress((String) row[22])

                .declarantName((String) row[23])
                .declarantDocument((String) row[24])
                .recordPlace((String) row[25])
                .recordDatetime(row[26] != null ? ((Timestamp) row[26]).toLocalDateTime() : null)

                .build();
    }



}
