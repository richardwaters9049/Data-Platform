package com.platform.controller;

import com.platform.dto.ImportResult;
import com.platform.model.DataType;
import com.platform.service.AutomotiveImportService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/automotive")
public class AutomotiveController {

    private final AutomotiveImportService importService;

    public AutomotiveController(AutomotiveImportService importService) {
        this.importService = importService;
    }

    @PostMapping("/upload/{dataType}")
    public ResponseEntity<ImportResult> uploadFile(
            @PathVariable DataType dataType,
            @RequestParam("file") MultipartFile file) throws IOException {
        
        ImportResult result = importService.importAutomotiveData(file, dataType);

        if (!result.hasErrors() && result.rowsImported() == 0) {
            return ResponseEntity.badRequest().body(result);
        }

        return ResponseEntity.ok(result);
    }

    @GetMapping("/data-types")
    public ResponseEntity<List<Map<String, Object>>> getDataTypes() {
        List<Map<String, Object>> dataTypes = Arrays.stream(DataType.values())
                .map(dt -> (Map<String, Object>) Map.of(
                        "name", dt.name(),
                        "displayName", dt.getDisplayName(),
                        "csvSchema", dt.getCsvSchema(),
                        "fields", (Object) dt.getSchemaFields()
                ))
                .collect(Collectors.toList());

        return ResponseEntity.ok(dataTypes);
    }

    @GetMapping("/schema/{dataType}")
    public ResponseEntity<Map<String, Object>> getSchema(@PathVariable DataType dataType) {
        Map<String, Object> schema = Map.of(
                "name", dataType.name(),
                "displayName", dataType.getDisplayName(),
                "csvSchema", dataType.getCsvSchema(),
                "fields", dataType.getSchemaFields()
        );

        return ResponseEntity.ok(schema);
    }
}
