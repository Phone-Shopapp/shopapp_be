package com.project.shopapp.utils.product;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Component
public class EmptyFilesUtil {
    public ResponseEntity<?> checkFile(List<MultipartFile> files) {
        if (files == null || files.isEmpty()) {
            return ResponseEntity.status(HttpStatus.LENGTH_REQUIRED)
                    .body("No files uploaded");
        }
        return null;
    }
}
