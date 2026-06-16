package com.ieumsae.assetieum.domain.hr.hrtemplateitem.repository;

import com.ieumsae.assetieum.domain.hr.hrtemplate.entity.HrTemplate;
import com.ieumsae.assetieum.domain.hr.hrtemplateitem.entity.HrTemplateItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HrTemplateItemRepository extends JpaRepository<HrTemplateItem, Long> {

    void deleteByHrTemplate(HrTemplate hrTemplate);
}
