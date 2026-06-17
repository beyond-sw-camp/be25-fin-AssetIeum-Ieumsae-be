package com.ieumsae.assetieum.domain.hr.hrevent.repository;

import com.ieumsae.assetieum.domain.hr.hrevent.entity.HrEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface HrEventRepository extends JpaRepository<HrEvent, UUID> {
}
