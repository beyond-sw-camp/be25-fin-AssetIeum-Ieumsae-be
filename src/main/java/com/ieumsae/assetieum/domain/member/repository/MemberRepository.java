package com.ieumsae.assetieum.domain.member.repository;

import com.ieumsae.assetieum.domain.member.entity.Member;
import com.ieumsae.assetieum.domain.member.type.MemberRole;
import com.ieumsae.assetieum.domain.member.type.MemberStatus;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MemberRepository extends JpaRepository<Member, UUID> {

	Optional<Member> findByMemberNo(String memberNo);

	Optional<Member> findByMemberNoAndCompany_CompanyCode(String memberNo, String companyCode);

	boolean existsByDepartment_IdAndDeletedAtIsNull(UUID departmentId);

	long countByDepartment_IdAndDeletedAtIsNull(UUID departmentId);

	boolean existsByCompany_IdAndMemberNo(UUID companyId, String memberNo);

	boolean existsByCompany_IdAndEmailAndDeletedAtIsNull(UUID companyId, String email);

	boolean existsByCompany_IdAndRoleAndDeletedAtIsNull(UUID companyId, MemberRole role);

	Optional<Member> findFirstByCompany_IdAndRoleAndDeletedAtIsNull(UUID companyId, MemberRole role);

	Optional<Member> findByIdAndCompany_IdAndDeletedAtIsNull(UUID memberId, UUID companyId);

	List<Member> findAllByCompany_IdAndStatusAndDeletedAtIsNull(UUID companyId, MemberStatus status);

	@Query("""
		SELECT m
		FROM Member m
		WHERE m.company.id = :companyId
			AND m.deletedAt IS NULL
			AND (
				:keyword IS NULL
				OR LOWER(m.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
				OR LOWER(m.memberNo) LIKE LOWER(CONCAT('%', :keyword, '%'))
				OR LOWER(m.email) LIKE LOWER(CONCAT('%', :keyword, '%'))
			)
			AND (:departmentId IS NULL OR m.department.id = :departmentId)
			AND (:status IS NULL OR m.status = :status)
		""")
	Page<Member> searchMembers(
		@Param("companyId") UUID companyId,
		@Param("keyword") String keyword,
		@Param("departmentId") UUID departmentId,
		@Param("status") MemberStatus status,
		Pageable pageable
	);
}
