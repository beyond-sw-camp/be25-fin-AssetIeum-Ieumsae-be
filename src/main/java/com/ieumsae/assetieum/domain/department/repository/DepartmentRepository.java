package com.ieumsae.assetieum.domain.department.repository;

import com.ieumsae.assetieum.domain.department.entity.Department;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DepartmentRepository extends JpaRepository<Department, UUID> {

	Optional<Department> findByIdAndCompany_IdAndDeletedAtIsNull(UUID departmentId, UUID companyId);

	boolean existsByParentDepartment_IdAndDeletedAtIsNull(UUID parentDepartmentId);

	List<Department> findAllByCompany_IdAndDeletedAtIsNullOrderByCreatedAtAsc(UUID companyId);

	@Query("""
		SELECT m.department.id, COUNT(m)
		FROM Member m
		WHERE m.company.id = :companyId
			AND m.deletedAt IS NULL
		GROUP BY m.department.id
		""")
	List<Object[]> countMembersByDepartmentId(@Param("companyId") UUID companyId);
}
