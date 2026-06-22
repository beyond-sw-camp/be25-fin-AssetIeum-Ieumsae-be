package com.ieumsae.assetieum.domain.hr.hreventassettarget.repository;

import com.ieumsae.assetieum.domain.hr.hreventassettarget.entity.HrEventAssetTarget;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HrEventAssetTargetRepository extends JpaRepository<HrEventAssetTarget, UUID> {

    @EntityGraph(attributePaths = {
            "member",
            "hrEvent",
            "tangibleAsset",
            "tangibleAsset.tangibleAssetItem",
            "intangibleAsset",
            "intangibleAsset.intangibleAssetItem"
    })
    List<HrEventAssetTarget> findAllByHrEvent_IdAndCompany_IdOrderByCreatedAtAsc(UUID eventId, UUID companyId);
}
