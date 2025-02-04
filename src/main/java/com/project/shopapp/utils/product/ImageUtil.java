package com.project.shopapp.utils.product;

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
public class ImageUtil {
    private boolean isImage(MultipartFile file) {
        String contentType = file.getContentType();
        return contentType != null && contentType.startsWith("images/");
    }


    public String storeImage(MultipartFile file) throws IOException {
        if (isImage(file) && file.getOriginalFilename() != null) {
            throw new IOException(
                    "Invalid image format"
            );
        }

        String filename = StringUtils.cleanPath(file.getOriginalFilename());

        String uniqueFilename = UUID.randomUUID().toString() + "_" + filename;

        java.nio.file.Path uploadDir = Paths.get("uploads");

        if (!Files.exists(uploadDir)) {
            Files.createDirectories(uploadDir);
        }

        java.nio.file.Path destination = Paths.get(uploadDir.toString(), uniqueFilename);

        Files.copy(file.getInputStream(), destination, StandardCopyOption.REPLACE_EXISTING);
        return uniqueFilename;
    }
}
