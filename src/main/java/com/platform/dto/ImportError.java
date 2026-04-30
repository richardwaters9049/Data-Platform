package com.platform.dto;

// A validation issue tied back to the source CSV row and field with enhanced location details.
public record ImportError(int rowNumber, String field, String message, String columnName, String csvLine) {
    
    // Constructor for backward compatibility
    public ImportError(int rowNumber, String field, String message) {
        this(rowNumber, field, message, null, null);
    }
    
    // Full constructor with enhanced details
    public ImportError(int rowNumber, String field, String message, String columnName, String csvLine) {
        this.rowNumber = rowNumber;
        this.field = field;
        this.message = message;
        this.columnName = columnName;
        this.csvLine = csvLine;
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
