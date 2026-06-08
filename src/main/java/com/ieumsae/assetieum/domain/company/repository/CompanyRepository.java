package com.ieumsae.assetieum.domain.company.repository;

import com.ieumsae.assetieum.domain.company.entity.Company;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CompanyRepository extends JpaRepository<Company, UUID> {

	boolean existsByCompanyCodeAndDeletedAtIsNull(String companyCode);

	Optional<Company> findByIdAndDeletedAtIsNull(UUID companyId);
}
