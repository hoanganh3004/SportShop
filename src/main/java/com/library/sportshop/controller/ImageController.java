package com.library.sportshop.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

@Controller
@RequestMapping("/images")
public class ImageController {

    private static final Logger log = LoggerFactory.getLogger(ImageController.class);

    @Value("${app.image.directory}")
    private String imageDirectory;

    @GetMapping("/{filename:.+}")
    public ResponseEntity<Resource> serveImage(@PathVariable String filename) {
        try {
            // Tạo đường dẫn đến file ảnh
            Path imagePath = Paths.get(imageDirectory + filename);
            File imageFile = imagePath.toFile();
            
            // Kiểm tra file có tồn tại không
            if (!imageFile.exists() || !imageFile.isFile()) {
                log.warn("Image not found: {}", filename);
                return ResponseEntity.notFound().build();
            }
            
            // Tạo resource từ file
            Resource resource = new FileSystemResource(imageFile);
            
            // Xác định content type dựa trên extension
            String contentType = getContentType(filename);
            
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
                    .body(resource);
                    
        } catch (Exception e) {
            log.error("Error serving image: {}", filename, e);
            return ResponseEntity.notFound().build();
        }
    }
    
    private String getContentType(String filename) {
        String extension = filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
        switch (extension) {
            case "jpg":
            case "jpeg":
                return "image/jpeg";
            case "png":
                return "image/png";
            case "gif":
                return "image/gif";
            case "bmp":
                return "image/bmp";
            case "webp":
                return "image/webp";
            default:
                return "application/octet-stream";
        }
    }
}
