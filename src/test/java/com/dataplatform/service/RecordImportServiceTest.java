package com.dataplatform.service;

import com.dataplatform.dto.ImportResult;
import com.dataplatform.model.Record;
import com.dataplatform.repository.RecordRepository;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for RecordImportService.
 * Tests the CSV import functionality including validation and persistence of records.
 */
class RecordImportServiceTest {

    // Real parser instance to test actual CSV parsing logic
    private final CsvRecordParser parser = new CsvRecordParser();
    // Mocked repository to isolate service logic from database operations
    private final RecordRepository repository = mock(RecordRepository.class);
    // Service under test with injected dependencies
    private final RecordImportService service = new RecordImportService(parser, repository);

    @Test
    void importsValidCsvRows() throws IOException {
        // Create a CSV with valid, well-formatted rows
        String csv = """
                name,email,age
                Jane Doe,jane@example.com,32
                Sam Jones,sam@example.com,41
                """;
        MockMultipartFile file = file(csv);

        // Mock repository to return the saved records (simulating successful save)
        when(repository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

        // Execute the import operation
        ImportResult result = service.importCsv(file);

        // Verify all rows were read and imported successfully with no errors
        assertThat(result.rowsRead()).isEqualTo(2);
        assertThat(result.rowsImported()).isEqualTo(2);
        assertThat(result.rowsRejected()).isZero();
        assertThat(result.errors()).isEmpty();
        // Verify the repository saveAll method was called
        verify(repository).saveAll(anyList());
    }

    @Test
    void returnsValidationErrorsForInvalidRows() throws IOException {
        // Create a CSV with various validation errors: missing name, invalid email, non-numeric age
        String csv = """
                name,email,age
                ,missing-name@example.com,30
                No Email,no-email,27
                Bad Age,bad-age@example.com,not-a-number
                """;
        MockMultipartFile file = file(csv);

        // Mock repository to return the saved records
        when(repository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

        // Execute the import operation
        ImportResult result = service.importCsv(file);

        // Verify all rows were read but none imported due to validation errors
        assertThat(result.rowsRead()).isEqualTo(3);
        assertThat(result.rowsImported()).isZero();
        assertThat(result.rowsRejected()).isEqualTo(3);
        // Verify errors were captured for the correct fields
        assertThat(result.errors())
                .extracting(error -> error.field())
                .containsExactly("name", "email", "age");
        // Verify repository was called with empty list (no valid records to save)
        verify(repository).saveAll(List.of());
    }

    /**
     * Helper method to create a MockMultipartFile for testing CSV uploads.
     * Simulates a file uploaded via a multipart form request.
     * @param content The CSV content as a string
     * @return A MockMultipartFile with the specified content
     */
    private MockMultipartFile file(String content) {
        return new MockMultipartFile(
                "file",
                "records.csv",
                "text/csv",
                content.getBytes(StandardCharsets.UTF_8)
        );
    }
}

