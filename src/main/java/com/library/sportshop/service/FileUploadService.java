package com.library.sportshop.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface FileUploadService {

    List<String> uploadMultipleFiles(MultipartFile[] files) throws IOException;

    String uploadSingleFile(MultipartFile file) throws IOException;

    boolean deleteFile(String filePath);
}
