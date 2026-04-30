package com.dataplatform.service;

import com.dataplatform.dto.ImportError;
import com.dataplatform.model.Record;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
public class CsvRecordParser {

    public ParsedRecord parse(String line, int rowNumber) {
        String[] parts = line.split(",", -1);
        List<ImportError> errors = new ArrayList<>();

        if (parts.length != 3) {
            errors.add(new ImportError(rowNumber, "row", "Expected 3 columns: name,email,age"));
            return ParsedRecord.invalid(errors);
        }

        String name = parts[0].trim();
        String email = parts[1].trim();
        String ageText = parts[2].trim();

        if (name.isBlank()) {
            errors.add(new ImportError(rowNumber, "name", "Name is required"));
        }

        if (email.isBlank()) {
            errors.add(new ImportError(rowNumber, "email", "Email is required"));
        } else if (!email.contains("@")) {
            errors.add(new ImportError(rowNumber, "email", "Email must contain @"));
        }

        Integer age = null;
        if (ageText.isBlank()) {
            errors.add(new ImportError(rowNumber, "age", "Age is required"));
        } else {
            try {
                age = Integer.parseInt(ageText);
                if (age < 0 || age > 130) {
                    errors.add(new ImportError(rowNumber, "age", "Age must be between 0 and 130"));
                }
            } catch (NumberFormatException ex) {
                errors.add(new ImportError(rowNumber, "age", "Age must be a whole number"));
            }
        }

        if (!errors.isEmpty()) {
            return ParsedRecord.invalid(errors);
        }

        Record record = new Record();
        record.setName(name);
        record.setEmail(email);
        record.setAge(age);
        return ParsedRecord.valid(record);
    }

    public record ParsedRecord(Optional<Record> record, List<ImportError> errors) {

        private static ParsedRecord valid(Record record) {
            return new ParsedRecord(Optional.of(record), List.of());
        }

        private static ParsedRecord invalid(List<ImportError> errors) {
            return new ParsedRecord(Optional.empty(), List.copyOf(errors));
        }

        public boolean isValid() {
            return record.isPresent();
        }
    }
}

