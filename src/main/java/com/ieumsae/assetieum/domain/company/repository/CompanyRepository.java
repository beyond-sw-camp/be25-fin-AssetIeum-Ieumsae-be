package com.ieumsae.assetieum.domain.company.repository;

import com.ieumsae.assetieum.domain.company.entity.Company;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CompanyRepository extends JpaRepository<Company, UUID>, CompanyRepositoryCustom {

	boolean existsByCompanyCodeAndDeletedAtIsNull(String companyCode);

	boolean existsByCompanyCode(String companyCode);

	boolean existsByCompanyName(String companyName);

	Optional<Company> findByIdAndDeletedAtIsNull(UUID companyId);
}
