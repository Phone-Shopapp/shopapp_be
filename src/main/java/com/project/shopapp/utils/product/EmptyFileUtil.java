package com.project.shopapp.utils.product;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Component
public class EmptyFileUtil {
    public ResponseEntity<?> checkFile(MultipartFile file) {
        if(file.getSize() == 0 || file.isEmpty()) {
            return ResponseEntity.status(HttpStatus.LENGTH_REQUIRED)
                    .body("File empty");
        }
        else if (file.getSize() == 1) {
            return ResponseEntity.status(HttpStatus.LENGTH_REQUIRED)
                    .body("File size is 1 byte, invalid file");
        }
        else if(file.getSize() > 10 * 1024 * 1024) { // Kích thước > 10MB
            return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE)
                    .body("File is too large! Maximum size is 10MB");
        }
        String contentType = file.getContentType();
        if(contentType == null || !contentType.startsWith("image/")) {
            return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
                    .body("File must be an image");
        }
        return null;
    }
}
