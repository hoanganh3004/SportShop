package com.library.sportshop.service.impl;

import com.library.sportshop.service.FileUploadService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;

@Service
public class FileUploadServiceImpl implements FileUploadService {

    @Value("${app.image.directory}")
    private String uploadDir;
    private static final List<String> ALLOWED_EXTENSIONS = List.of("jpg", "jpeg", "png", "gif", "bmp", "webp");
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB

    @Override
    public List<String> uploadMultipleFiles(MultipartFile[] files) throws IOException {
        List<String> uploadedFilePaths = new ArrayList<>();
        createUploadDirectoryIfNotExists();

        for (MultipartFile file : files) {
            if (file != null && !file.isEmpty()) {
                String filePath = uploadSingleFile(file);
                uploadedFilePaths.add(filePath);
            }
        }
        return uploadedFilePaths;
    }

    @Override
    public String uploadSingleFile(MultipartFile file) throws IOException {
        validateFile(file);

        String originalFileName = file.getOriginalFilename();
        if (originalFileName == null || originalFileName.trim().isEmpty()) {
            throw new IOException("Tên file không hợp lệ");
        }

        // Chỉ lấy tên file, bỏ thư mục người dùng đính kèm
        String cleanOriginalName = Paths.get(originalFileName).getFileName().toString();

        Path uploadPath = Paths.get(this.uploadDir);
        createUploadDirectoryIfNotExists();

        Path target = resolveNonConflictingPath(uploadPath, cleanOriginalName);
        Files.write(target, file.getBytes());

        // Trả về đường dẫn Windows dạng D:\image\filename.jpg
        return target.toString();
    }

    @Override
    public boolean deleteFile(String filePath) {
        try {
            File file = new File(filePath);
            return file.exists() && file.delete();
        } catch (Exception e) {
            System.err.println("Lỗi khi xóa file: " + filePath + " - " + e.getMessage());
            return false;
        }
    }

    // Private methods

    private void validateFile(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IOException("File không được để trống");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IOException("File quá lớn. Kích thước tối đa: 5MB");
        }

        String extension = getFileExtension(file.getOriginalFilename());
        if (!ALLOWED_EXTENSIONS.contains(extension.toLowerCase())) {
            throw new IOException("Định dạng file không được hỗ trợ. Chỉ chấp nhận: "
                    + String.join(", ", ALLOWED_EXTENSIONS));
        }
    }

    private String getFileExtension(String fileName) {
        if (fileName == null)
            return "";
        int dot = fileName.lastIndexOf('.');
        return dot == -1 ? "" : fileName.substring(dot + 1);
    }

    private String getBaseName(String fileName) {
        if (fileName == null)
            return "";
        int dot = fileName.lastIndexOf('.');
        return dot == -1 ? fileName : fileName.substring(0, dot);
    }

    private Path resolveNonConflictingPath(Path directory, String desiredFileName) {
        String ext = getFileExtension(desiredFileName);
        String base = getBaseName(desiredFileName);

        Path candidate = directory.resolve(desiredFileName);
        int counter = 1;
        while (Files.exists(candidate)) {
            String newName = ext.isEmpty() ? String.format("%s(%d)", base, counter)
                    : String.format("%s(%d).%s", base, counter, ext);
            candidate = directory.resolve(newName);
            counter++;
        }
        return candidate;
    }

    private void createUploadDirectoryIfNotExists() throws IOException {
        File directory = new File(this.uploadDir);
        if (!directory.exists()) {
            boolean created = directory.mkdirs();
            if (!created) {
                throw new IOException("Không thể tạo thư mục upload: " + this.uploadDir);
            }
        }
    }
}
