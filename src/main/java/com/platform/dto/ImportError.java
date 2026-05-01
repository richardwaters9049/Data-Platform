package com.platform.dto;

// A validation issue tied back to the source CSV row and field with enhanced location details.
public record ImportError(int rowNumber, String field, String message, String columnName, String csvLine) {

    // Compact constructor for backward compatibility
    public ImportError(int rowNumber, String field, String message) {
        this(rowNumber, field, message, null, null);
    }

    // Helper method to create enhanced error with column name
    public static ImportError withColumn(int rowNumber, String field, String message, String columnName, String csvLine) {
        return new ImportError(rowNumber, field, message, columnName, csvLine);
    }

    // Helper method to create enhanced error with CSV line
    public static ImportError withLine(int rowNumber, String field, String message, String csvLine) {
        return new ImportError(rowNumber, field, message, field, csvLine);
    }
}
