package com.ieumsae.assetieum.domain.hr.hrevent.service;

import com.ieumsae.assetieum.domain.company.entity.Company;
import com.ieumsae.assetieum.domain.company.repository.CompanyRepository;
import com.ieumsae.assetieum.domain.department.entity.Department;
import com.ieumsae.assetieum.domain.department.repository.DepartmentRepository;
import com.ieumsae.assetieum.domain.hr.hrevent.dto.HrEventCreateRequest;
import com.ieumsae.assetieum.domain.hr.hrevent.dto.HrEventResponse;
import com.ieumsae.assetieum.domain.hr.hrevent.dto.HrEventSearchRequest;
import com.ieumsae.assetieum.domain.hr.hrevent.entity.HrEvent;
import com.ieumsae.assetieum.domain.hr.hrevent.repository.HrEventRepository;
import com.ieumsae.assetieum.domain.hr.hrevent.type.HrEventStatus;
import com.ieumsae.assetieum.domain.hr.hrtemplate.dto.HrTemplateResponse;
import com.ieumsae.assetieum.domain.member.entity.Member;
import com.ieumsae.assetieum.domain.member.repository.MemberRepository;
import com.ieumsae.assetieum.global.common.page.PaginationResponse;
import com.ieumsae.assetieum.global.common.util.CodeGenerator;
import com.ieumsae.assetieum.global.exception.BusinessException;
import com.ieumsae.assetieum.global.exception.ErrorCode;
import com.ieumsae.assetieum.global.security.AuthenticatedMember;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HrEventService {

    private static final String HR_EVENT_NO_PREFIX = "EVT";
    private static final String REDIS_KEY_PREFIX = "hr-event:no:";

    private final CompanyRepository companyRepository;
    private final MemberRepository memberRepository;
    private final DepartmentRepository departmentRepository;
    private final HrEventRepository hrEventRepository;

    private final CodeGenerator codeGenerator;

    @Transactional
    public HrEventResponse createHrEvent(
            HrEventCreateRequest request,
            AuthenticatedMember member
    ) {
        // 1. 입력값 검증
        Company company = companyRepository.findById(member.companyId())
                .orElseThrow(() -> new BusinessException(ErrorCode.COMPANY_NOT_FOUND));

        Member targetMember = memberRepository.findByIdAndCompany_IdAndDeletedAtIsNull(member.id(), member.companyId())
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        Department department = departmentRepository.findByIdAndCompany_IdAndDeletedAtIsNull(targetMember.getDepartment().getId(), member.companyId())
                .orElseThrow(() -> new BusinessException(ErrorCode.DEPARTMENT_NOT_FOUND));

        // 2. HR 이벤트 등록
        HrEvent hrEvent = HrEvent.builder()
                .company(company)
                .department(department)
                .member(targetMember)
                .hrEventNo(codeGenerator.generate(HR_EVENT_NO_PREFIX, REDIS_KEY_PREFIX, member.companyId()))
                .eventType(request.getEventType())
                .eventDate(request.getEventDate())
                .build();

        HrEvent savedHrEvent = hrEventRepository.save(hrEvent);

        return HrEventResponse.from(savedHrEvent);
    }

    @Transactional
    public HrTemplateResponse deleteHrEvent(
            UUID eventId,
            AuthenticatedMember member
    ) {
        // 1. 입력값 검증
        Company company = companyRepository.findById(member.companyId())
                .orElseThrow(() -> new BusinessException(ErrorCode.COMPANY_NOT_FOUND));

        HrEvent hrEvent = hrEventRepository.findByIdAndCompany_IdAndCancelledAtIsNull(eventId, member.companyId())
                .orElseThrow(() -> new BusinessException(ErrorCode.HR_EVENT_NOT_FOUND));

        if(hrEvent.getHrEventStatus() != HrEventStatus.PENDING) {
            throw new BusinessException(ErrorCode.HR_EVENT_ALREADY_IN_PROGRESS);
        }

        // 2. 이벤트 삭제 (soft delete)
        hrEvent.delete();

        return null;
    }

    public PaginationResponse<HrEventResponse> getHrEvents(
            HrEventSearchRequest request,
            AuthenticatedMember authenticatedMember
    ) {
        // 1. 입력값 검증
        companyRepository.findById(authenticatedMember.companyId())
                .orElseThrow(() -> new BusinessException(ErrorCode.COMPANY_NOT_FOUND));

        Member member = memberRepository.findByIdAndCompany_IdAndDeletedAtIsNull(authenticatedMember.id(), authenticatedMember.companyId())
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        // 2. 페이징 처리 및 반환
        Page<HrEventResponse> eventPage = hrEventRepository.search(
                member.getCompany().getId(),
                member.getDepartment().getId(),
                request.getHrEventStatus(),
                request.getHrEventType(),
                request.toPageable()
        );

        return PaginationResponse.from(eventPage);
    }
}
