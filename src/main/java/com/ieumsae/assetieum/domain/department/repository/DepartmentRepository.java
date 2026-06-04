package com.ieumsae.assetieum.domain.department.repository;

import com.ieumsae.assetieum.domain.department.entity.Department;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DepartmentRepository extends JpaRepository<Department, UUID> {
}
