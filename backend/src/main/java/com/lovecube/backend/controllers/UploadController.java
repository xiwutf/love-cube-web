package com.lovecube.backend.controllers;

import com.lovecube.backend.config.AppProperties;
import com.lovecube.backend.models.User;
import com.lovecube.backend.services.FileStorageService;
import com.lovecube.backend.services.UnifiedProfileService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

@RestController
@RequestMapping("/api/upload")
public class UploadController {
    private final AppProperties appProperties;
    private final FileStorageService fileStorageService;
    private final UnifiedProfileService unifiedProfileService;

    public UploadController(
            AppProperties appProperties,
            FileStorageService fileStorageService,
            UnifiedProfileService unifiedProfileService
    ) {
        this.appProperties = appProperties;
        this.fileStorageService = fileStorageService;
        this.unifiedProfileService = unifiedProfileService;
    }

    @PostMapping("/image")
    public ResponseEntity<?> uploadImage(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("文件为空");
        }
        try {
            String ext = resolveSafeImageExtension(file);
            if (!isValidImageExtension(ext)) {
                return ResponseEntity.badRequest().body("不支持的文件类型");
            }
            String url = fileStorageService.uploadPhoto(file);
            return ResponseEntity.ok(Map.of("url", url));
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "上传失败: " + e.getMessage()));
        }
    }

    @PostMapping("/verify")
    public ResponseEntity<?> uploadVerifyPhoto(
            @RequestParam("file") MultipartFile file,
            @RequestHeader("Authorization") String authHeader
    ) {
        try {
            unifiedProfileService.requireCurrentUser(authHeader);
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body("文件为空");
            }
            String ext = resolveSafeImageExtension(file);
            if (!isValidImageExtension(ext)) {
                return ResponseEntity.badRequest().body("不支持的文件类型，请上传 jpg/jpeg/png");
            }
            String url = fileStorageService.uploadVerifyPhoto(file);
            return ResponseEntity.ok(Map.of("url", url));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "上传失败: " + e.getMessage()));
        }
    }

    @PostMapping("/avatar")
    public ResponseEntity<?> uploadAvatar(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("文件为空");
        }
        try {
            String ext = resolveSafeImageExtension(file);
            if (!isValidImageExtension(ext)) {
                return ResponseEntity.badRequest().body("不支持的文件类型，请上传 jpg/jpeg/png/gif");
            }
            String avatarUrl = fileStorageService.uploadAvatar(file);
            return ResponseEntity.ok(Map.of("url", avatarUrl));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "上传失败: " + e.getMessage()));
        }
    }

    @PutMapping("/me")
    public ResponseEntity<?> updateUserInfo(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody Map<String, Object> updates
    ) {
        try {
            User user = unifiedProfileService.requireCurrentUser(authHeader);
            unifiedProfileService.updateLegacyProfile(user, updates);
            return ResponseEntity.ok("资料更新成功");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("资料更新失败: " + e.getMessage());
        }
    }

    @PostMapping("/photos")
    public ResponseEntity<?> uploadPhoto(
            @RequestParam("file") MultipartFile file,
            @RequestHeader("Authorization") String authHeader
    ) {
        try {
            unifiedProfileService.requireCurrentUser(authHeader);
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body("文件为空");
            }
            String ext = resolveSafeImageExtension(file);
            if (!isValidImageExtension(ext)) {
                return ResponseEntity.badRequest().body("不支持的文件类型，请上传 jpg/jpeg/png/gif");
            }
            String photoUrl = fileStorageService.uploadPhoto(file);
            return ResponseEntity.ok(Map.of("url", photoUrl, "message", "照片上传成功"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "上传失败: " + e.getMessage()));
        }
    }

    @PostMapping("/photos/batch")
    public ResponseEntity<?> uploadPhotos(
            @RequestParam("files") MultipartFile[] files,
            @RequestHeader("Authorization") String authHeader
    ) {
        try {
            unifiedProfileService.requireCurrentUser(authHeader);
            if (files == null || files.length == 0) {
                return ResponseEntity.badRequest().body("没有选择文件");
            }

            List<String> uploadedUrls = new ArrayList<>();
            List<String> failedFiles = new ArrayList<>();
            for (MultipartFile file : files) {
                if (file.isEmpty()) continue;
                try {
                    String ext = resolveSafeImageExtension(file);
                    if (!isValidImageExtension(ext)) {
                        failedFiles.add(displayName(file) + " (不支持的文件类型)");
                        continue;
                    }
                    uploadedUrls.add(fileStorageService.uploadPhoto(file));
                } catch (Exception e) {
                    failedFiles.add(displayName(file) + " (上传失败: " + e.getMessage() + ")");
                }
            }

            Map<String, Object> result = new HashMap<>();
            result.put("uploadedUrls", uploadedUrls);
            result.put("uploadedCount", uploadedUrls.size());
            result.put("failedFiles", failedFiles);
            result.put("failedCount", failedFiles.size());
            if (!uploadedUrls.isEmpty()) {
                result.put("message", "成功上传 " + uploadedUrls.size() + " 张照片");
            }
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        }
    }

    @PostMapping("/photos/save")
    public ResponseEntity<?> saveUserPhotos(
            @RequestBody Map<String, Object> request,
            @RequestHeader("Authorization") String authHeader
    ) {
        try {
            User user = unifiedProfileService.requireCurrentUser(authHeader);
            @SuppressWarnings("unchecked")
            List<String> photoUrls = (List<String>) request.get("photos");
            if (photoUrls == null) {
                return ResponseEntity.badRequest().body("照片列表不能为空");
            }
            for (String url : photoUrls) {
                if (!isValidPhotoUrl(url)) {
                    return ResponseEntity.badRequest().body("包含无效的照片 URL: " + url);
                }
            }
            unifiedProfileService.replaceUserPhotos(user.getUserid(), photoUrls);
            return ResponseEntity.ok(Map.of("message", "生活照片保存成功", "photosCount", photoUrls.size()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "保存失败: " + e.getMessage()));
        }
    }

    @DeleteMapping("/photos")
    public ResponseEntity<?> deletePhoto(
            @RequestParam String photoUrl,
            @RequestHeader("Authorization") String authHeader
    ) {
        try {
            User user = unifiedProfileService.requireCurrentUser(authHeader);
            List<String> current = new ArrayList<>(unifiedProfileService.getUserPhotos(user.getUserid()));
            if (!current.contains(photoUrl)) {
                return ResponseEntity.badRequest().body("照片不存在于用户资料中");
            }
            current.remove(photoUrl);
            unifiedProfileService.replaceUserPhotos(user.getUserid(), current);
            fileStorageService.deleteFile(photoUrl);
            return ResponseEntity.ok(Map.of("message", "照片删除成功", "remainingPhotosCount", current.size()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "删除失败: " + e.getMessage()));
        }
    }

    @GetMapping("/photos")
    public ResponseEntity<?> getUserPhotos(@RequestHeader("Authorization") String authHeader) {
        try {
            User user = unifiedProfileService.requireCurrentUser(authHeader);
            List<String> photos = unifiedProfileService.getUserPhotos(user.getUserid());
            return ResponseEntity.ok(Map.of("photos", photos, "photosCount", photos.size()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        }
    }

    @GetMapping("/storage-status")
    public ResponseEntity<?> getStorageStatus() {
        AppProperties.Oss oss = appProperties.getOss();
        boolean ossEnabled = oss != null && oss.isEnabled();

        Map<String, Object> result = new HashMap<>();
        result.put("mode", ossEnabled ? "oss" : "local");
        result.put("ossEnabled", ossEnabled);

        Map<String, Object> appCfg = new HashMap<>();
        appCfg.put("baseUrl", appProperties.getBaseUrl());
        appCfg.put("avatarPath", appProperties.getAvatarPath());
        appCfg.put("photosPath", appProperties.getPhotosPath());
        result.put("app", appCfg);

        Map<String, Object> ossCfg = new HashMap<>();
        ossCfg.put("endpoint", oss == null ? "" : oss.getEndpoint());
        ossCfg.put("bucketName", oss == null ? "" : oss.getBucketName());
        ossCfg.put("cdnDomain", oss == null ? "" : oss.getCdnDomain());
        ossCfg.put("avatarFolder", oss == null ? "" : oss.getAvatarFolder());
        ossCfg.put("photosFolder", oss == null ? "" : oss.getPhotosFolder());
        ossCfg.put("accessKeyIdConfigured", oss != null && oss.getAccessKeyId() != null && !oss.getAccessKeyId().isBlank());
        ossCfg.put("accessKeySecretConfigured", oss != null && oss.getAccessKeySecret() != null && !oss.getAccessKeySecret().isBlank());
        result.put("oss", ossCfg);

        List<String> warnings = new ArrayList<>();
        if (ossEnabled) {
            if (oss == null || oss.getEndpoint() == null || oss.getEndpoint().isBlank()) warnings.add("OSS endpoint 未配置");
            if (oss == null || oss.getBucketName() == null || oss.getBucketName().isBlank()) warnings.add("OSS bucket-name 未配置");
            if (oss == null || oss.getAccessKeyId() == null || oss.getAccessKeyId().isBlank()) warnings.add("OSS access-key-id 未配置");
            if (oss == null || oss.getAccessKeySecret() == null || oss.getAccessKeySecret().isBlank()) warnings.add("OSS access-key-secret 未配置");
        }
        result.put("warnings", warnings);
        return ResponseEntity.ok(result);
    }

    private boolean isValidImageExtension(String extension) {
        return Arrays.asList(".jpg", ".jpeg", ".png", ".gif", ".JPG", ".JPEG", ".PNG", ".GIF").contains(extension);
    }

    private String resolveSafeImageExtension(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        if (originalFilename != null) {
            int idx = originalFilename.lastIndexOf(".");
            if (idx >= 0 && idx < originalFilename.length() - 1) {
                return originalFilename.substring(idx);
            }
        }
        String contentType = file.getContentType();
        if (contentType != null) {
            if (contentType.contains("png")) return ".png";
            if (contentType.contains("gif")) return ".gif";
            if (contentType.contains("jpeg") || contentType.contains("jpg")) return ".jpg";
        }
        return ".jpg";
    }

    private String displayName(MultipartFile file) {
        String name = file.getOriginalFilename();
        return (name == null || name.isBlank()) ? "未命名文件" : name;
    }

    private boolean isValidPhotoUrl(String url) {
        if (url == null || url.isBlank()) return false;
        String trimmed = url.trim();
        String localPrefix = trimTrailingSlash(appProperties.getBaseUrl()) + appProperties.getPhotosPath();
        if (trimmed.startsWith(localPrefix)) return true;

        AppProperties.Oss oss = appProperties.getOss();
        if (oss == null || !oss.isEnabled()) return false;

        String photosFolder = normalizeFolder(oss.getPhotosFolder(), "photos/");
        String cdnDomain = trimTrailingSlash(oss.getCdnDomain());
        if (cdnDomain != null && !cdnDomain.isBlank()) {
            if (!cdnDomain.startsWith("http://") && !cdnDomain.startsWith("https://")) {
                cdnDomain = "https://" + cdnDomain;
            }
            if (trimmed.startsWith(cdnDomain + "/" + photosFolder)) return true;
        }

        String endpoint = trimTrailingSlash(oss.getEndpoint());
        if (oss.getBucketName() != null && !oss.getBucketName().isBlank() && endpoint != null && !endpoint.isBlank()) {
            return trimmed.startsWith("https://" + oss.getBucketName() + "." + endpoint + "/" + photosFolder);
        }
        return false;
    }

    private String normalizeFolder(String folder, String fallback) {
        String normalized = (folder == null || folder.isBlank()) ? fallback : folder;
        return normalized.replaceAll("^/+", "").replaceAll("/+$", "") + "/";
    }

    private String trimTrailingSlash(String value) {
        if (value == null) return "";
        return value.replaceAll("/+$", "");
    }
}
