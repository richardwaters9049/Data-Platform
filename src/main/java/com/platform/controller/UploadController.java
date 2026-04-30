package com.platform.controller;

import com.platform.dto.ImportResult;
import com.platform.service.RecordImportService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api")
public class UploadController {

    private final RecordImportService importService;

    public UploadController(RecordImportService importService) {
        this.importService = importService;
    }

    @PostMapping("/upload")
    public ResponseEntity<ImportResult> uploadFile(@RequestParam("file") MultipartFile file) throws IOException {
        ImportResult result = importService.importCsv(file);

        // Validation errors are returned as a client-facing import report.
        if (result.hasErrors()) {
            return ResponseEntity.badRequest().body(result);
        }

        return ResponseEntity.ok(result);
    }
}
