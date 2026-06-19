package com.ieumsae.assetieum.domain.department.repository;

import com.ieumsae.assetieum.domain.department.entity.Department;
import com.ieumsae.assetieum.domain.member.type.MemberRole;
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

	List<Department> findAllByCompany_IdAndNameNotAndDeletedAtIsNullOrderByCreatedAtAsc(
		UUID companyId,
		String name
	);

	@Query("""
		SELECT m.department.id, COUNT(m)
		FROM Member m
		WHERE m.company.id = :companyId
			AND m.deletedAt IS NULL
			AND m.role <> :adminRole
		GROUP BY m.department.id
		""")
	List<Object[]> countMembersByDepartmentId(
		@Param("companyId") UUID companyId,
		@Param("adminRole") MemberRole adminRole
	);
}
