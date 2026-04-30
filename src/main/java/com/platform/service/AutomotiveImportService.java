package com.platform.service;

import com.platform.dto.ImportResult;
import com.platform.model.DataType;
import com.platform.service.pipeline.AutomotivePipelineService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * Legacy service maintained for backward compatibility.
 * Delegates to the new AutomotivePipelineService for ETL processing.
 * @deprecated Use AutomotivePipelineService directly for new development
 */
@Service
@Deprecated
public class AutomotiveImportService {

    private final AutomotivePipelineService pipelineService;

    public AutomotiveImportService(AutomotivePipelineService pipelineService) {
        this.pipelineService = pipelineService;
    }

    /**
     * Legacy method that delegates to the new pipeline service.
     * @deprecated Use AutomotivePipelineService.runPipeline() instead
     */
    @Deprecated
    public ImportResult importAutomotiveData(MultipartFile file, DataType dataType) throws IOException {
        try {
            return pipelineService.runPipeline(file, dataType);
        } catch (Exception e) {
            if (e instanceof IOException) {
                throw (IOException) e;
            }
            throw new IOException("Pipeline execution failed", e);
        }
    }
}
