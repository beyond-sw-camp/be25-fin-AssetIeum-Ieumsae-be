package com.ieumsae.assetieum.domain.member.repository;

import com.ieumsae.assetieum.domain.member.entity.Member;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, UUID> {

	Optional<Member> findByMemberNo(String memberNo);
}
