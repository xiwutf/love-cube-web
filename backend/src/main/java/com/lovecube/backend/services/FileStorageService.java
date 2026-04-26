package com.lovecube.backend.services;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface FileStorageService {
    String uploadAvatar(MultipartFile file) throws IOException;
    String uploadPhoto(MultipartFile file) throws IOException;
    String uploadVerifyPhoto(MultipartFile file) throws IOException;
    boolean deleteFile(String fileUrl);
} 