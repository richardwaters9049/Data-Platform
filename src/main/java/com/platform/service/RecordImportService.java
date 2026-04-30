package com.platform.service;

import com.platform.dto.ImportError;
import com.platform.dto.ImportResult;
import com.platform.model.Record;
import com.platform.repository.RecordRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Service
public class RecordImportService {

    private final CsvRecordParser parser;
    private final RecordRepository repository;

    public RecordImportService(CsvRecordParser parser, RecordRepository repository) {
        this.parser = parser;
        this.repository = repository;
    }

    public ImportResult importCsv(MultipartFile file) throws IOException {
        List<ImportError> errors = new ArrayList<>();
        List<Record> recordsToSave = new ArrayList<>();
        int rowsRead = 0;

        // The first row is treated as a header; row numbers match the source file.
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            int rowNumber = 0;

            while ((line = reader.readLine()) != null) {
                rowNumber++;

                if (rowNumber == 1) {
                    continue;
                }

                if (line.isBlank()) {
                    continue;
                }

                rowsRead++;
                CsvRecordParser.ParsedRecord parsedRecord = parser.parse(line, rowNumber);

                // Keep accepted records and rejected-row errors separate for the final summary.
                if (parsedRecord.isValid()) {
                    recordsToSave.add(parsedRecord.record().orElseThrow());
                } else {
                    errors.addAll(parsedRecord.errors());
                }
            }
        }

        // Persist only the rows that passed validation.
        repository.saveAll(recordsToSave);

        return new ImportResult(
                rowsRead,
                recordsToSave.size(),
                rowsRead - recordsToSave.size(),
                List.copyOf(errors)
        );
    }
}
