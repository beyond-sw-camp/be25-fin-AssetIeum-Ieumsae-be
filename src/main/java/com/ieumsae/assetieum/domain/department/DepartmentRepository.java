package com.ieumsae.assetieum.domain.department;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DepartmentRepository extends JpaRepository<Department, UUID> {

	List<Department> findByCompanyId(UUID companyId);
}
