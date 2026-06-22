package com.ieumsae.assetieum.domain.inspection.inspection.entity;

import com.ieumsae.assetieum.domain.company.entity.Company;
import com.ieumsae.assetieum.domain.department.entity.Department;
import com.ieumsae.assetieum.domain.inspection.inspection.type.InspectionStatus;
import com.ieumsae.assetieum.domain.inspection.inspection.type.InspectionTargetType;
import com.ieumsae.assetieum.domain.inspection.inspection.type.InspectionType;
import com.ieumsae.assetieum.domain.inspection.inspection.type.InspectorType;
import com.ieumsae.assetieum.domain.member.entity.Member;
import com.ieumsae.assetieum.global.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.type.SqlTypes;

@Getter
@Entity
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "inspections")
public class Inspection extends BaseEntity {

    @Id
    @UuidGenerator
    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "inspection_id", columnDefinition = "CHAR(36)")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @Enumerated(EnumType.STRING)
    @Column(name = "inspection_type", nullable = false, length = 50)
    private InspectionType inspectionType;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_type", nullable = false, length = 50)
    private InspectionTargetType targetType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_department_id")
    private Department targetDepartment;

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "target_category_id", columnDefinition = "CHAR(36)")
    private UUID targetCategoryId;

    @Enumerated(EnumType.STRING)
    @Column(name = "inspector_type", nullable = false, length = 50)
    private InspectorType inspectorType;

    @Column(name = "start_date", nullable = false)
    private LocalDateTime startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDateTime endDate;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(name = "inspection_status", nullable = false, length = 50)
    private InspectionStatus inspectionStatus = InspectionStatus.READY;

    @Column(name = "description", length = 500)
    private String description;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "inspector_id", nullable = false)
    private Member inspector;

    public void start() {
        this.inspectionStatus = InspectionStatus.IN_PROGRESS;
    }
}
