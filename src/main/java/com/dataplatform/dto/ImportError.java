package com.dataplatform.dto;

// A validation issue tied back to the source CSV row and field.
public record ImportError(int rowNumber, String field, String message) {
}
