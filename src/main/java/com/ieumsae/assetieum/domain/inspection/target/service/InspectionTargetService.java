package com.ieumsae.assetieum.domain.inspection.target.service;

import com.ieumsae.assetieum.domain.company.repository.CompanyRepository;
import com.ieumsae.assetieum.domain.inspection.inspection.type.InspectionType;
import com.ieumsae.assetieum.domain.inspection.target.dto.InspectionTargetResponse;
import com.ieumsae.assetieum.domain.inspection.target.dto.InspectionTargetSearchRequest;
import com.ieumsae.assetieum.domain.inspection.target.repository.InspectionTargetRepository;
import com.ieumsae.assetieum.domain.member.repository.MemberRepository;
import com.ieumsae.assetieum.global.common.page.PaginationResponse;
import com.ieumsae.assetieum.global.exception.BusinessException;
import com.ieumsae.assetieum.global.exception.ErrorCode;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InspectionTargetService {

    private final CompanyRepository companyRepository;
    private final MemberRepository memberRepository;
    private final InspectionTargetRepository inspectionTargetRepository;

    public PaginationResponse<InspectionTargetResponse> getMyInspectionTargets(
            InspectionTargetSearchRequest request,
            InspectionType inspectionType,
            UUID companyId,
            UUID memberId
    ) {
        companyRepository.findByIdAndDeletedAtIsNull(companyId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COMPANY_NOT_FOUND));

        memberRepository.findByIdAndCompany_IdAndDeletedAtIsNull(memberId, companyId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        Page<InspectionTargetResponse> targets = inspectionTargetRepository.searchMyTargets(
                        companyId,
                        memberId,
                        inspectionType,
                        request.getStatus(),
                        request.getIsResponded(),
                        request.toPageable()
                )
                .map(InspectionTargetResponse::from);

        return PaginationResponse.from(targets);
    }
}
