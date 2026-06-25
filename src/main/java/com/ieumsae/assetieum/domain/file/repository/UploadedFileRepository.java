package com.ieumsae.assetieum.domain.file.repository;

import com.ieumsae.assetieum.domain.file.entity.UploadedFile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UploadedFileRepository extends JpaRepository<UploadedFile, Long> {
}
