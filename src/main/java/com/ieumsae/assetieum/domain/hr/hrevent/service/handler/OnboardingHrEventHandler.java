package com.ieumsae.assetieum.domain.hr.hrevent.service.handler;

import com.ieumsae.assetieum.domain.hr.hrevent.entity.HrEvent;
import com.ieumsae.assetieum.domain.hr.hrevent.type.HrEventType;
import org.springframework.stereotype.Component;

@Component
public class OnboardingHrEventHandler implements HrEventHandler {

    @Override
    public HrEventType supports() {
        return HrEventType.ONBOARDING;
    }

    @Override
    public void handle(HrEvent hrEvent) {
    }
}
