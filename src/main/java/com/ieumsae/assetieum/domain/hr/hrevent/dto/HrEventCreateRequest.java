package com.ieumsae.assetieum.domain.hr.hrevent.dto;

import com.ieumsae.assetieum.domain.hr.hrevent.type.HrEventType;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class HrEventCreateRequest {
    @NotNull
    private UUID memberId;

    @NotNull
    private HrEventType eventType;

    @NotNull
    private LocalDateTime eventDate;
}