package com.ieumsae.assetieum.domain.hr.hreventassettarget.repository;

import com.ieumsae.assetieum.domain.hr.hreventassettarget.entity.HrEventAssetTarget;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HrEventAssetTargetRepository extends JpaRepository<HrEventAssetTarget, UUID> {
}
