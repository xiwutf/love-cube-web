package com.lovecube.backend.services;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.CannedAccessControlList;
import com.aliyun.oss.model.PutObjectRequest;
import com.lovecube.backend.config.AppProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.InputStream;
import java.io.IOException;
import java.util.Locale;
import java.util.UUID;

@Service
public class FileStorageServiceImpl implements FileStorageService {
    
    @Autowired
    private AppProperties appProperties;
    
    @Override
    public String uploadAvatar(MultipartFile file) throws IOException {
        if (appProperties.getOss().isEnabled()) {
            return uploadToOss(file, normalizeFolder(appProperties.getOss().getAvatarFolder(), "avatars/"));
        } else {
            return uploadToLocal(file, "uploads/avatar/");
        }
    }
    
    @Override
    public String uploadPhoto(MultipartFile file) throws IOException {
        if (appProperties.getOss().isEnabled()) {
            return uploadToOss(file, normalizeFolder(appProperties.getOss().getPhotosFolder(), "photos/"));
        } else {
            return uploadToLocal(file, "uploads/photos/");
        }
    }
    
    @Override
    public boolean deleteFile(String fileUrl) {
        if (appProperties.getOss().isEnabled()) {
            return deleteFromOss(fileUrl);
        } else {
            return deleteFromLocal(fileUrl);
        }
    }
    
    /**
     * 上传到本地文件系统
     */
    private String uploadToLocal(MultipartFile file, String folder) throws IOException {
        String ext = resolveExtension(file);
        String filename = UUID.randomUUID().toString() + ext;
        
        String savePath = System.getProperty("user.dir") + "/" + folder;
        File dir = new File(savePath);
        if (!dir.exists()) dir.mkdirs();
        
        File dest = new File(dir, filename);
        file.transferTo(dest);
        
        return trimTrailingSlash(appProperties.getBaseUrl()) + "/" + trimSlash(folder) + "/" + filename;
    }
    
    /**
     * 从本地删除文件
     */
    private boolean deleteFromLocal(String fileUrl) {
        try {
            String baseUrl = trimTrailingSlash(appProperties.getBaseUrl());
            if (fileUrl.startsWith(baseUrl)) {
                String relativePath = fileUrl.substring(baseUrl.length());
                String filePath = System.getProperty("user.dir") + relativePath;
                File file = new File(filePath);
                return file.delete();
            }
            return false;
        } catch (Exception e) {
            System.err.println("从本地删除文件失败: " + e.getMessage());
            return false;
        }
    }

    private String uploadToOss(MultipartFile file, String folder) throws IOException {
        AppProperties.Oss ossConfig = appProperties.getOss();
        validateOssConfig(ossConfig);

        String ext = resolveExtension(file);
        String objectKey = folder + UUID.randomUUID() + ext;

        OSS client = new OSSClientBuilder().build(
                ossConfig.getEndpoint(),
                ossConfig.getAccessKeyId(),
                ossConfig.getAccessKeySecret()
        );
        try (InputStream inputStream = file.getInputStream()) {
            PutObjectRequest putObjectRequest = new PutObjectRequest(
                    ossConfig.getBucketName(),
                    objectKey,
                    inputStream
            );
            client.putObject(putObjectRequest);
            // Ensure uploaded images can be rendered directly by browsers.
            client.setObjectAcl(ossConfig.getBucketName(), objectKey, CannedAccessControlList.PublicRead);
        } finally {
            client.shutdown();
        }

        return buildOssFileUrl(objectKey);
    }

    private boolean deleteFromOss(String fileUrl) {
        AppProperties.Oss ossConfig = appProperties.getOss();
        try {
            validateOssConfig(ossConfig);
            String objectKey = resolveObjectKeyFromUrl(fileUrl);
            if (objectKey == null || objectKey.isBlank()) {
                return false;
            }
            OSS client = new OSSClientBuilder().build(
                    ossConfig.getEndpoint(),
                    ossConfig.getAccessKeyId(),
                    ossConfig.getAccessKeySecret()
            );
            try {
                client.deleteObject(ossConfig.getBucketName(), objectKey);
                return true;
            } finally {
                client.shutdown();
            }
        } catch (Exception e) {
            System.err.println("删除OSS文件失败: " + e.getMessage());
            return false;
        }
    }

    private String buildOssFileUrl(String objectKey) {
        AppProperties.Oss ossConfig = appProperties.getOss();
        String cdnDomain = trimTrailingSlash(ossConfig.getCdnDomain());
        if (cdnDomain != null && !cdnDomain.isBlank()) {
            if (!cdnDomain.startsWith("http://") && !cdnDomain.startsWith("https://")) {
                cdnDomain = "https://" + cdnDomain;
            }
            return cdnDomain + "/" + objectKey;
        }
        String endpoint = trimTrailingSlash(ossConfig.getEndpoint());
        return "https://" + ossConfig.getBucketName() + "." + endpoint + "/" + objectKey;
    }

    private String resolveObjectKeyFromUrl(String fileUrl) {
        if (fileUrl == null || fileUrl.isBlank()) {
            return null;
        }
        String normalized = fileUrl.trim();
        int idx = normalized.indexOf("://");
        if (idx < 0) {
            return null;
        }
        int pathStart = normalized.indexOf("/", idx + 3);
        if (pathStart < 0 || pathStart == normalized.length() - 1) {
            return null;
        }
        return normalized.substring(pathStart + 1);
    }

    private void validateOssConfig(AppProperties.Oss ossConfig) {
        if (isBlank(ossConfig.getAccessKeyId())
                || isBlank(ossConfig.getAccessKeySecret())
                || isBlank(ossConfig.getEndpoint())
                || isBlank(ossConfig.getBucketName())) {
            throw new IllegalStateException("OSS 配置不完整，请检查 access-key-id/access-key-secret/endpoint/bucket-name");
        }
    }

    private String resolveExtension(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        if (originalFilename != null) {
            int idx = originalFilename.lastIndexOf(".");
            if (idx >= 0 && idx < originalFilename.length() - 1) {
                return originalFilename.substring(idx).toLowerCase(Locale.ROOT);
            }
        }
        String contentType = file.getContentType();
        if (contentType != null) {
            if (contentType.contains("png")) return ".png";
            if (contentType.contains("gif")) return ".gif";
            if (contentType.contains("jpeg") || contentType.contains("jpg")) return ".jpg";
            if (contentType.contains("webp")) return ".webp";
        }
        return ".jpg";
    }

    private String normalizeFolder(String folder, String fallback) {
        String normalized = folder;
        if (isBlank(normalized)) normalized = fallback;
        normalized = trimSlash(normalized) + "/";
        return normalized;
    }

    private String trimTrailingSlash(String value) {
        if (value == null) return null;
        return value.replaceAll("/+$", "");
    }

    private String trimSlash(String value) {
        if (value == null) return "";
        return value.replaceAll("^/+", "").replaceAll("/+$", "");
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
} 