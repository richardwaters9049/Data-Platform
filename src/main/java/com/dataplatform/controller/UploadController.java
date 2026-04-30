package com.dataplatform.controller;

import com.dataplatform.dto.ImportResult;
import com.dataplatform.service.RecordImportService;
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

        if (result.hasErrors()) {
            return ResponseEntity.badRequest().body(result);
        }

        return ResponseEntity.ok(result);
    }
}
