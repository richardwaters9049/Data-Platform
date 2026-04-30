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

class RecordImportServiceTest {

    private final CsvRecordParser parser = new CsvRecordParser();
    private final RecordRepository repository = mock(RecordRepository.class);
    private final RecordImportService service = new RecordImportService(parser, repository);

    @Test
    void importsValidCsvRows() throws IOException {
        String csv = """
                name,email,age
                Jane Doe,jane@example.com,32
                Sam Jones,sam@example.com,41
                """;
        MockMultipartFile file = file(csv);

        when(repository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

        ImportResult result = service.importCsv(file);

        assertThat(result.rowsRead()).isEqualTo(2);
        assertThat(result.rowsImported()).isEqualTo(2);
        assertThat(result.rowsRejected()).isZero();
        assertThat(result.errors()).isEmpty();
        verify(repository).saveAll(anyList());
    }

    @Test
    void returnsValidationErrorsForInvalidRows() throws IOException {
        String csv = """
                name,email,age
                ,missing-name@example.com,30
                No Email,no-email,27
                Bad Age,bad-age@example.com,not-a-number
                """;
        MockMultipartFile file = file(csv);

        when(repository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

        ImportResult result = service.importCsv(file);

        assertThat(result.rowsRead()).isEqualTo(3);
        assertThat(result.rowsImported()).isZero();
        assertThat(result.rowsRejected()).isEqualTo(3);
        assertThat(result.errors())
                .extracting(error -> error.field())
                .containsExactly("name", "email", "age");
        verify(repository).saveAll(List.of());
    }

    private MockMultipartFile file(String content) {
        return new MockMultipartFile(
                "file",
                "records.csv",
                "text/csv",
                content.getBytes(StandardCharsets.UTF_8)
        );
    }
}

