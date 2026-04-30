package com.dataplatform.dto;

import java.util.List;

public record ImportResult(
        int rowsRead,
        int rowsImported,
        int rowsRejected,
        List<ImportError> errors
) {

    public boolean hasErrors() {
        return !errors.isEmpty();
    }
}

