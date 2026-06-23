package com.ieumsae.assetieum.domain.hr.hrevent.service.handler;

import com.ieumsae.assetieum.domain.hr.hrevent.entity.HrEvent;
import com.ieumsae.assetieum.domain.hr.hrevent.type.HrEventType;
import com.ieumsae.assetieum.domain.hr.hreventassettarget.entity.HrEventAssetTarget;
import com.ieumsae.assetieum.domain.hr.hreventassettarget.repository.HrEventAssetTargetRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OffboardingHrEventHandler implements HrEventHandler {

    private final HrEventAssetTargetRepository hrEventAssetTargetRepository;
    private final HrEventAssetTargetProcessor hrEventAssetTargetProcessor;

    @Override
    public HrEventType supports() {
        return HrEventType.OFFBOARDING;
    }

    @Override
    public void handle(HrEvent hrEvent) {
        hrEvent.start();
        List<HrEventAssetTarget> targets = hrEventAssetTargetRepository
                .findAllByHrEvent_IdAndCompany_IdOrderByCreatedAtAsc(
                        hrEvent.getId(),
                        hrEvent.getCompany().getId()
                );

        for (HrEventAssetTarget target : targets) {
            hrEventAssetTargetProcessor.process(hrEvent, target, hrEvent.getCompany().getId(), false);
        }

        hrEvent.getMember().resign();
    }
}
