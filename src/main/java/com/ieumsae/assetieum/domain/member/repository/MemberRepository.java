package com.ieumsae.assetieum.domain.member.repository;

import com.ieumsae.assetieum.domain.member.entity.Member;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, UUID> {

	Optional<Member> findByMemberNo(String memberNo);

	Optional<Member> findByMemberNoAndCompany_CompanyCode(String memberNo, String companyCode);

	boolean existsByDepartment_IdAndDeletedAtIsNull(UUID departmentId);

	boolean existsByCompany_IdAndMemberNoAndDeletedAtIsNull(UUID companyId, String memberNo);

	boolean existsByCompany_IdAndEmailAndDeletedAtIsNull(UUID companyId, String email);

	Optional<Member> findByIdAndCompany_IdAndDeletedAtIsNull(UUID memberId, UUID companyId);
}
