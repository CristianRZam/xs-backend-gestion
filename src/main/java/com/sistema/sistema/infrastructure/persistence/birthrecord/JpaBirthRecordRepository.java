package com.sistema.sistema.infrastructure.persistence.birthrecord;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface JpaBirthRecordRepository extends JpaRepository<BirthRecordEntity, Long> {


    @Query(value = """
    SELECT 
        br.id,
        br.year,
        br.act_number,
        br.person_name,
        br.sex,
        br.birth_place,
        br.birth_datetime,
        br.district_id,
        pf.full_name AS father_name,
        pm.full_name AS mother_name,
        br.declarant_name,
        br.declarant_document,
        br.record_place,
        br.record_datetime
    FROM birth_records br
    LEFT JOIN parents pf ON pf.id = br.father_id
    LEFT JOIN parents pm ON pm.id = br.mother_id
    WHERE br.deleted_at IS NULL
        AND (:year IS NULL OR br.year = :year)
        AND (:actNumber IS NULL OR br.act_number LIKE CONCAT('%', :actNumber, '%'))
        AND (:personName IS NULL OR br.person_name LIKE CONCAT('%', :personName, '%'))
        AND (:sex IS NULL OR br.sex = :sex)
        AND (:fatherName IS NULL OR pf.full_name LIKE CONCAT('%', :fatherName, '%'))
        AND (:motherName IS NULL OR pm.full_name LIKE CONCAT('%', :motherName, '%'))
        AND (br.birth_datetime >= COALESCE(:birthDateFrom, br.birth_datetime))
        AND (br.birth_datetime <= COALESCE(:birthDateTo, br.birth_datetime))
        AND (br.record_datetime >= COALESCE(:recordDateFrom, br.record_datetime))
        AND (br.record_datetime <= COALESCE(:recordDateTo, br.record_datetime))
    ORDER BY br.created_at DESC
""", nativeQuery = true)

    List<Object[]> filter(
            @Param("year") Integer year,
            @Param("actNumber") String actNumber,
            @Param("personName") String personName,
            @Param("sex") String sex,
            @Param("fatherName") String fatherName,
            @Param("motherName") String motherName,
            @Param("birthDateFrom") LocalDateTime birthDateFrom,
            @Param("birthDateTo") LocalDateTime birthDateTo,
            @Param("recordDateFrom") LocalDateTime recordDateFrom,
            @Param("recordDateTo") LocalDateTime recordDateTo
    );


    @Query(value = """
    SELECT
        br.id,
        br.year,
        br.act_number,
        br.person_name,
        br.sex,
        br.birth_place,
        br.birth_datetime,

        d.id AS district_id,
        d.name AS district_name,
        d.province AS district_province,
        d.department AS district_department,

        pf.id AS father_id,
        pf.full_name AS father_name,
        pf.natural_of AS father_natural_of,
        pf.nationality AS father_nationality,
        pf.occupation AS father_occupation,
        pf.address AS father_address,

        pm.id AS mother_id,
        pm.full_name AS mother_name,
        pm.natural_of AS mother_natural_of,
        pm.nationality AS mother_nationality,
        pm.occupation AS mother_occupation,
        pm.address AS mother_address,

        br.declarant_name,
        br.declarant_document,
        br.record_place,
        br.record_datetime

    FROM birth_records br
    LEFT JOIN parents pf ON pf.id = br.father_id
    LEFT JOIN parents pm ON pm.id = br.mother_id
    LEFT JOIN districts d ON d.id = br.district_id
    WHERE br.deleted_at IS NULL
      AND br.id = :id
""", nativeQuery = true)
    Object findDetailById(@Param("id") Long id);




}
