package com.platform.controller;

import com.platform.dto.ImportResult;
import com.platform.model.DataType;
import com.platform.service.pipeline.AutomotivePipelineService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/automotive")
public class AutomotiveController {

    private final AutomotivePipelineService pipelineService;

    public AutomotiveController(AutomotivePipelineService pipelineService) {
        this.pipelineService = pipelineService;
    }

    @PostMapping("/upload/{dataType}")
    public ResponseEntity<ImportResult> uploadFile(
            @PathVariable DataType dataType,
            @RequestParam("file") MultipartFile file) throws Exception {
        
        if (file.isEmpty()) {
            ImportResult errorResult = new ImportResult(0, 0, 0, 
                List.of(new com.platform.dto.ImportError(0, "file", "The uploaded file is empty")));
            return ResponseEntity.badRequest().body(errorResult);
        }
        
        ImportResult result = pipelineService.runPipeline(file, dataType);

        if (!result.hasErrors() && result.rowsImported() == 0) {
            ImportResult errorResult = new ImportResult(
                result.rowsRead(), result.rowsImported(), result.rowsRejected(),
                List.of(new com.platform.dto.ImportError(0, "file", 
                    "No rows were imported. File may be empty or have incorrect format. Rows read: " + result.rowsRead()))
            );
            return ResponseEntity.badRequest().body(errorResult);
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
