package com.ieumsae.assetieum.domain.hr.hrtemplate.repository;

import com.ieumsae.assetieum.domain.hr.hrtemplate.entity.HrTemplate;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HrTemplateRepository extends JpaRepository<HrTemplate, UUID> {

    Optional<HrTemplate> findByCompany_IdAndDepartment_IdAndDeletedAtIsNull(UUID companyId, UUID departmentId);
}
