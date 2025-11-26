package com.sistema.sistema.domain.usecase;


import com.sistema.sistema.application.dto.request.birthrecord.BirthRecordFilterList;
import com.sistema.sistema.application.dto.request.birthrecord.BirthRecordFormRequest;
import com.sistema.sistema.application.dto.request.birthrecord.BirthRecordRequest;
import com.sistema.sistema.application.dto.request.birthrecord.BirthRecordUpdateRequest;
import com.sistema.sistema.application.dto.response.birthrecord.BirthRecordFormResponse;
import com.sistema.sistema.application.dto.response.birthrecord.BirthRecordResponse;
import com.sistema.sistema.application.dto.response.birthrecord.BirthRecordViewResponse;

import java.util.List;

public interface BirthRecordUseCase {

    BirthRecordViewResponse init(BirthRecordFilterList request);

    BirthRecordResponse create(BirthRecordRequest request);

    BirthRecordResponse update(BirthRecordUpdateRequest request);

    byte[] generateCertificate(Long id);

    BirthRecordFormResponse initFormData(BirthRecordFormRequest request);
}
