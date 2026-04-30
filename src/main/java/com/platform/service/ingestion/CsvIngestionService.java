package com.platform.service.ingestion;

import com.platform.model.DataType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles CSV file ingestion and raw data extraction.
 * Responsibility: Read CSV files and extract raw row data.
 */
@Service
public class CsvIngestionService {

    /**
     * Ingests a CSV file and extracts raw row data.
     * 
     * @param file The CSV file to ingest
     * @param dataType The expected data type for schema validation
     * @return List of raw CSV rows (excluding header)
     * @throws IOException If file reading fails
     */
    public List<String> ingestCsv(MultipartFile file, DataType dataType) throws IOException {
        List<String> rawRows = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            int rowNumber = 0;

            while ((line = reader.readLine()) != null) {
                rowNumber++;

                // Skip header row
                if (rowNumber == 1) {
                    continue;
                }

                // Skip empty lines
                if (line.isBlank()) {
                    continue;
                }

                rawRows.add(line);
            }
        }

        return rawRows;
    }

    /**
     * Validates basic CSV structure against expected schema.
     * 
     * @param rawRow The raw CSV row
     * @param dataType The expected data type
     * @return Array of field values, or null if structure is invalid
     */
    public String[] parseCsvStructure(String rawRow, DataType dataType) {
        String[] parts = rawRow.split(",", -1);
        String[] expectedFields = dataType.getSchemaFields();

        if (parts.length != expectedFields.length) {
            return null; // Structure invalid
        }

        return parts;
    }
}
