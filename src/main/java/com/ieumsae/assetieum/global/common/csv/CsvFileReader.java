package com.ieumsae.assetieum.global.common.csv;

import com.ieumsae.assetieum.global.exception.BusinessException;
import com.ieumsae.assetieum.global.exception.ErrorCode;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
public class CsvFileReader {

    public List<String[]> readRows(MultipartFile file) {
        validateCsvFile(file);

        List<String[]> rows = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8)
        )) {
            String header = reader.readLine();
            if (header == null || header.isBlank()) {
                throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
            }

            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) {
                    continue;
                }

                rows.add(line.split(",", -1));
            }
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
        }

        return rows;
    }

    private void validateCsvFile(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        boolean hasCsvExtension = originalFilename != null && originalFilename.toLowerCase().endsWith(".csv");

        if (file.isEmpty() || !hasCsvExtension) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }
    }
}
