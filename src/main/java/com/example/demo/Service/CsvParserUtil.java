package com.example.demo.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

@Slf4j
@Component
public class CsvParserUtil {

    public <T> CsvParseResult<T> parseCsvFile(MultipartFile file, Function<CSVRecord, T> recordMapper) {
        List<T> parsedRecords = new ArrayList<>();
        List<String> errors = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            
            CSVParser csvParser = CSVFormat.DEFAULT.builder()
                    .setHeader()
                    .setSkipHeaderRecord(true)
                    .setIgnoreHeaderCase(true)
                    .setTrim(true)
                    .build()
                    .parse(reader);

            int lineNumber = 1;
            for (CSVRecord record : csvParser) {
                try {
                    lineNumber++;
                    T mappedRecord = recordMapper.apply(record);
                    if (mappedRecord != null) {
                        parsedRecords.add(mappedRecord);
                    }
                } catch (Exception e) {
                    String error = String.format("Line %d: %s", lineNumber, e.getMessage());
                    errors.add(error);
                    log.warn("Error parsing CSV line {}: {}", lineNumber, e.getMessage());
                }
            }

            return new CsvParseResult<>(parsedRecords, errors, !errors.isEmpty());

        } catch (IOException e) {
            String error = "Error reading CSV file: " + e.getMessage();
            errors.add(error);
            log.error("CSV parsing failed: {}", e.getMessage());
            return new CsvParseResult<>(List.of(), errors, true);
        }
    }

    public static class CsvParseResult<T> {
        private final List<T> records;
        private final List<String> errors;
        private final boolean hasErrors;

        public CsvParseResult(List<T> records, List<String> errors, boolean hasErrors) {
            this.records = records;
            this.errors = errors;
            this.hasErrors = hasErrors;
        }

        public List<T> getRecords() { return records; }
        public List<String> getErrors() { return errors; }
        public boolean isHasErrors() { return hasErrors; }
    }
}