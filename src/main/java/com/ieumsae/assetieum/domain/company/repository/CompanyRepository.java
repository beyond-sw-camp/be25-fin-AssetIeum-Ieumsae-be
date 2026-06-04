package com.ieumsae.assetieum.domain.company.repository;

import com.ieumsae.assetieum.domain.company.entity.Company;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CompanyRepository extends JpaRepository<Company, UUID> {
}
