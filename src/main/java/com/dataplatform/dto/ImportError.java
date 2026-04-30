package com.dataplatform.dto;

public record ImportError(int rowNumber, String field, String message) {
}

