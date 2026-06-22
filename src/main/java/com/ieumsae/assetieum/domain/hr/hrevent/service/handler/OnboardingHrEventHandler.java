package com.ieumsae.assetieum.domain.hr.hrevent.service.handler;

import com.ieumsae.assetieum.domain.hr.hrevent.entity.HrEvent;
import com.ieumsae.assetieum.domain.hr.hrevent.type.HrEventType;
import com.ieumsae.assetieum.domain.hr.hrtemplate.entity.HrTemplate;
import com.ieumsae.assetieum.domain.hr.hrtemplate.repository.HrTemplateRepository;
import com.ieumsae.assetieum.domain.hr.hrtemplateitem.entity.HrTemplateItem;
import com.ieumsae.assetieum.domain.hr.hrtemplateitem.repository.HrTemplateItemRepository;
import com.ieumsae.assetieum.domain.member.entity.Member;
import com.ieumsae.assetieum.domain.ticket.assetrequest.dto.AssetRequestTicketCreateRequest;
import com.ieumsae.assetieum.domain.ticket.assetrequest.dto.AssetRequestTicketCreateResponse;
import com.ieumsae.assetieum.domain.ticket.assetrequest.service.AssetRequestTicketService;
import com.ieumsae.assetieum.domain.ticket.common.service.TicketService;
import com.ieumsae.assetieum.domain.ticket.common.type.AssetType;
import com.ieumsae.assetieum.domain.ticket.common.type.RequestedUsageType;
import com.ieumsae.assetieum.global.exception.BusinessException;
import com.ieumsae.assetieum.global.exception.ErrorCode;
import com.ieumsae.assetieum.global.security.AuthenticatedMember;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OnboardingHrEventHandler implements HrEventHandler {

    private final HrTemplateRepository hrTemplateRepository;
    private final HrTemplateItemRepository hrTemplateItemRepository;
    private final AssetRequestTicketService assetRequestTicketService;
    private final TicketService ticketService;

    @Override
    public HrEventType supports() {
        return HrEventType.ONBOARDING;
    }

    @Override
    public void handle(HrEvent hrEvent) {
        HrTemplate hrTemplate = hrTemplateRepository.findByCompany_IdAndDepartment_IdAndDeletedAtIsNull(
                        hrEvent.getCompany().getId(),
                        hrEvent.getDepartment().getId()
                )
                .orElseThrow(() -> new BusinessException(ErrorCode.HR_TEMPLATE_NOT_FOUND));

        List<HrTemplateItem> hrTemplateItems = hrTemplateItemRepository.findByHrTemplate(hrTemplate);
        AuthenticatedMember targetMember = toAuthenticatedMember(hrEvent.getMember());

        for (HrTemplateItem item : hrTemplateItems) {
            AssetRequestTicketCreateResponse ticket = assetRequestTicketService.createAssetRequestTicket(targetMember, createAssetRequest(item));
            ticketService.approveDepartmentForHrEvent(hrEvent.getCompany().getId(), ticket.getTicketId());
        }

        hrEvent.complete();
    }

    private AssetRequestTicketCreateRequest createAssetRequest(HrTemplateItem item) {
        AssetRequestTicketCreateRequest request = new AssetRequestTicketCreateRequest();
        request.setRequestedUsageType(RequestedUsageType.PERSONAL);
        request.setQuantity(item.getQuantity());
        request.setRequestReason("입사 자산 신청");

        if (item.getTangibleAssetItem() != null) {
            request.setAssetType(AssetType.TANGIBLE);
            request.setAssetItemId(item.getTangibleAssetItem().getId());
            return request;
        }

        if (item.getIntangibleAssetItem() != null) {
            request.setAssetType(AssetType.INTANGIBLE);
            request.setAssetItemId(item.getIntangibleAssetItem().getId());
            return request;
        }

        throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
    }

    private AuthenticatedMember toAuthenticatedMember(Member member) {
        return new AuthenticatedMember(
                member.getId(),
                member.getCompany().getId(),
                member.getMemberNo(),
                member.getName(),
                member.getEmail(),
                member.getRole()
        );
    }
}
