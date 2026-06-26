package com.ieumsae.assetieum.domain.hr.hrevent.repository;

import com.ieumsae.assetieum.domain.hr.hrevent.dto.HrEventResponse;
import com.ieumsae.assetieum.domain.hr.hrevent.type.HrEventStatus;
import com.ieumsae.assetieum.domain.hr.hrevent.type.HrEventType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface HrEventRepositoryCustom {
    Page<HrEventResponse> search(UUID companyId, UUID departmentId, HrEventStatus hrEventStatus, HrEventType hrEventType, Pageable pageable);
}
