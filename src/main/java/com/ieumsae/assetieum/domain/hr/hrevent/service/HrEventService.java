package com.ieumsae.assetieum.domain.hr.hrevent.service;

import com.ieumsae.assetieum.domain.company.entity.Company;
import com.ieumsae.assetieum.domain.company.repository.CompanyRepository;
import com.ieumsae.assetieum.domain.department.entity.Department;
import com.ieumsae.assetieum.domain.department.repository.DepartmentRepository;
import com.ieumsae.assetieum.domain.hr.hrevent.dto.HrEventCreateRequest;
import com.ieumsae.assetieum.domain.hr.hrevent.dto.HrEventResponse;
import com.ieumsae.assetieum.domain.hr.hrevent.entity.HrEvent;
import com.ieumsae.assetieum.domain.hr.hrevent.repository.HrEventRepository;
import com.ieumsae.assetieum.domain.hr.hrtemplate.dto.HrTemplateResponse;
import com.ieumsae.assetieum.domain.member.entity.Member;
import com.ieumsae.assetieum.domain.member.repository.MemberRepository;
import com.ieumsae.assetieum.global.common.util.CodeGenerator;
import com.ieumsae.assetieum.global.exception.BusinessException;
import com.ieumsae.assetieum.global.exception.ErrorCode;
import com.ieumsae.assetieum.global.security.AuthenticatedMember;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    public HrTemplateResponse deleteHrEvent(AuthenticatedMember member) {
        return null;
    }
}
