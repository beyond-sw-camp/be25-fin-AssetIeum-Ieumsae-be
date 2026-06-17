package com.ieumsae.assetieum.domain.hr.hrevent.dto;

import com.ieumsae.assetieum.domain.hr.hrevent.type.HrEventStatus;
import com.ieumsae.assetieum.domain.hr.hrevent.type.HrEventType;
import com.ieumsae.assetieum.global.common.page.PaginationRequest;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class HrEventSearchRequest extends PaginationRequest {

    private HrEventStatus hrEventStatus;

    private HrEventType hrEventType;

}
