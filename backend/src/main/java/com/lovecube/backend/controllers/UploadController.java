package com.lovecube.backend.controllers;

import com.lovecube.backend.config.AppProperties;
import com.lovecube.backend.models.User;
import com.lovecube.backend.repository.UserRepository;
import com.lovecube.backend.services.FileStorageService;
import com.lovecube.backend.utils.JwtUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.*;

@RestController
@RequestMapping("/api/upload")
public class UploadController
{
    @Autowired
    private AppProperties appProperties;
    
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FileStorageService fileStorageService;
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final String IMAGES_DIR = "uploads/images/";

    /** 通用图片上传，前端 H5 统一调用此接口 */
    @PostMapping("/image")
    public ResponseEntity<?> uploadImage(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("文件为空");
        }
        try {
            String originalFilename = file.getOriginalFilename();
            String ext = originalFilename != null && originalFilename.contains(".")
                ? originalFilename.substring(originalFilename.lastIndexOf("."))
                : ".jpg";
            if (!isValidImageExtension(ext)) {
                return ResponseEntity.badRequest().body("不支持的文件类型");
            }
            String filename = UUID.randomUUID().toString() + ext;
            String savePath = System.getProperty("user.dir") + "/" + IMAGES_DIR;
            File dir = new File(savePath);
            if (!dir.exists()) dir.mkdirs();
            file.transferTo(new File(dir, filename));
            String url = appProperties.getBaseUrl() + "/uploads/images/" + filename;
            return ResponseEntity.ok(Map.of("url", url));
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "上传失败: " + e.getMessage()));
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
                return ResponseEntity.badRequest().body("不支持的文件类型，请上传jpg、jpeg、png、gif格式的图片");
            }
            String avatarUrl = fileStorageService.uploadAvatar(file);

            Map<String, Object> result = new HashMap<>();
            result.put("url", avatarUrl);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "上传失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @PutMapping("/me")
    public ResponseEntity<?> updateUserInfo(@RequestHeader("Authorization") String authHeader,
                                            @RequestBody Map<String, Object> updates) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("未提供或格式错误的 token");
        }

        String token = authHeader.substring(7);
        String openid = JwtUtil.getOpenIdFromToken(token);
        if (openid == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("token 无效");
        }
        
        User user = userRepository.findByOpenid(openid);

        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("用户不存在");
        }

        if (updates.containsKey("nickname")) {
            user.setUsername((String) updates.get("nickname"));
        }
        if (updates.containsKey("gender")) {
            String genderStr = (String) updates.get("gender");
            if ("男".equals(genderStr)) user.setGender(1);
            else if ("女".equals(genderStr)) user.setGender(2);
            else user.setGender(0);
        }
        if (updates.containsKey("location")) {
            user.setLocation((String) updates.get("location"));
        }
        if (updates.containsKey("age")) {
            Object ageObj = updates.get("age");
            if (ageObj instanceof Integer) {
                user.setAge((Integer) ageObj);
            } else if (ageObj instanceof String) {
                user.setAge(Integer.parseInt((String) ageObj));
            }
        }
        if (updates.containsKey("profilePhoto")) {
            user.setProfilePhoto((String) updates.get("profilePhoto"));
        }

        userRepository.save(user);
        return ResponseEntity.ok("资料更新成功");
    }

    /**
     * 上传生活照片
     */
    @PostMapping("/photos")
    public ResponseEntity<?> uploadPhoto(@RequestParam("file") MultipartFile file,
                                        @RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("未提供或格式错误的 token");
        }

        String token = authHeader.substring(7);
        String openid = JwtUtil.getOpenIdFromToken(token);
        if (openid == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("token 无效");
        }

        User user = userRepository.findByOpenid(openid);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("用户不存在");
        }

        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("文件为空");
        }

        try {
            String ext = resolveSafeImageExtension(file);
            
            // 验证文件类型
            if (!isValidImageExtension(ext)) {
                return ResponseEntity.badRequest().body("不支持的文件类型，请上传jpg、jpeg、png、gif格式的图片");
            }
            
            String photoUrl = fileStorageService.uploadPhoto(file);

            Map<String, Object> result = new HashMap<>();
            result.put("url", photoUrl);
            result.put("message", "照片上传成功");
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "上传失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * 批量上传生活照片
     */
    @PostMapping("/photos/batch")
    public ResponseEntity<?> uploadPhotos(@RequestParam("files") MultipartFile[] files,
                                         @RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("未提供或格式错误的 token");
        }

        String token = authHeader.substring(7);
        String openid = JwtUtil.getOpenIdFromToken(token);
        if (openid == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("token 无效");
        }

        User user = userRepository.findByOpenid(openid);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("用户不存在");
        }

        if (files == null || files.length == 0) {
            return ResponseEntity.badRequest().body("没有选择文件");
        }

        List<String> uploadedUrls = new ArrayList<>();
        List<String> failedFiles = new ArrayList<>();

        for (MultipartFile file : files) {
            if (file.isEmpty()) {
                continue;
            }

            try {
                String ext = resolveSafeImageExtension(file);
                
                if (!isValidImageExtension(ext)) {
                    failedFiles.add(displayName(file) + " (不支持的文件类型)");
                    continue;
                }
                
                String photoUrl = fileStorageService.uploadPhoto(file);
                uploadedUrls.add(photoUrl);

            } catch (Exception e) {
                failedFiles.add(displayName(file) + " (上传失败: " + e.getMessage() + ")");
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("uploadedUrls", uploadedUrls);
        result.put("uploadedCount", uploadedUrls.size());
        result.put("failedFiles", failedFiles);
        result.put("failedCount", failedFiles.size());
        
        if (uploadedUrls.size() > 0) {
            result.put("message", "成功上传 " + uploadedUrls.size() + " 张照片");
        }
        
        return ResponseEntity.ok(result);
    }

    /**
     * 保存用户生活照片到用户资料
     */
    @PostMapping("/photos/save")
    public ResponseEntity<?> saveUserPhotos(@RequestBody Map<String, Object> request,
                                           @RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("未提供或格式错误的 token");
        }

        String token = authHeader.substring(7);
        String openid = JwtUtil.getOpenIdFromToken(token);
        if (openid == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("token 无效");
        }

        User user = userRepository.findByOpenid(openid);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("用户不存在");
        }

        try {
            @SuppressWarnings("unchecked")
            List<String> photoUrls = (List<String>) request.get("photos");
            
            if (photoUrls == null) {
                return ResponseEntity.badRequest().body("照片列表不能为空");
            }

            for (String url : photoUrls) {
                if (!isValidPhotoUrl(url)) {
                    return ResponseEntity.badRequest().body("包含无效的照片URL: " + url);
                }
            }

            String photosJson = objectMapper.writeValueAsString(photoUrls);
            user.setPhotos(photosJson);
            userRepository.save(user);

            Map<String, Object> result = new HashMap<>();
            result.put("message", "生活照片保存成功");
            result.put("photosCount", photoUrls.size());
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "保存失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * 删除生活照片
     */
    @DeleteMapping("/photos")
    public ResponseEntity<?> deletePhoto(@RequestParam String photoUrl,
                                        @RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("未提供或格式错误的 token");
        }

        String token = authHeader.substring(7);
        String openid = JwtUtil.getOpenIdFromToken(token);
        if (openid == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("token 无效");
        }

        User user = userRepository.findByOpenid(openid);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("用户不存在");
        }

        try {
            // 获取用户当前的照片列表
            List<String> currentPhotos = parsePhotosJson(user.getPhotos());
            
            if (!currentPhotos.contains(photoUrl)) {
                return ResponseEntity.badRequest().body("照片不存在于用户资料中");
            }

            // 从列表中移除照片
            currentPhotos.remove(photoUrl);
            
            // 更新用户资料
            String photosJson = objectMapper.writeValueAsString(currentPhotos);
            user.setPhotos(photosJson);
            userRepository.save(user);

            // 尝试删除存储中的实际文件（OSS 或本地）
            fileStorageService.deleteFile(photoUrl);

            Map<String, Object> result = new HashMap<>();
            result.put("message", "照片删除成功");
            result.put("remainingPhotosCount", currentPhotos.size());
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "删除失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * 获取用户的生活照片列表
     */
    @GetMapping("/photos")
    public ResponseEntity<?> getUserPhotos(@RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("未提供或格式错误的 token");
        }

        String token = authHeader.substring(7);
        String openid = JwtUtil.getOpenIdFromToken(token);
        if (openid == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("token 无效");
        }

        User user = userRepository.findByOpenid(openid);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("用户不存在");
        }

        List<String> photos = parsePhotosJson(user.getPhotos());
        
        Map<String, Object> result = new HashMap<>();
        result.put("photos", photos);
        result.put("photosCount", photos.size());
        return ResponseEntity.ok(result);
    }

    /**
     * 存储诊断：用于快速确认当前是否启用 OSS 及关键配置状态（不返回密钥）
     */
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
            if (oss == null || oss.getEndpoint() == null || oss.getEndpoint().isBlank()) {
                warnings.add("OSS endpoint 未配置");
            }
            if (oss == null || oss.getBucketName() == null || oss.getBucketName().isBlank()) {
                warnings.add("OSS bucket-name 未配置");
            }
            if (oss == null || oss.getAccessKeyId() == null || oss.getAccessKeyId().isBlank()) {
                warnings.add("OSS access-key-id 未配置");
            }
            if (oss == null || oss.getAccessKeySecret() == null || oss.getAccessKeySecret().isBlank()) {
                warnings.add("OSS access-key-secret 未配置");
            }
        }
        result.put("warnings", warnings);

        return ResponseEntity.ok(result);
    }

    // 工具方法
    private boolean isValidImageExtension(String extension) {
        String[] validExtensions = {".jpg", ".jpeg", ".png", ".gif", ".JPG", ".JPEG", ".PNG", ".GIF"};
        return Arrays.asList(validExtensions).contains(extension);
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
        if (name == null || name.isBlank()) return "未命名文件";
        return name;
    }

    private boolean isValidPhotoUrl(String url) {
        if (url == null || url.isBlank()) {
            return false;
        }

        String trimmed = url.trim();
        String localPrefix = trimTrailingSlash(appProperties.getBaseUrl()) + appProperties.getPhotosPath();
        if (trimmed.startsWith(localPrefix)) {
            return true;
        }

        AppProperties.Oss oss = appProperties.getOss();
        if (!oss.isEnabled()) {
            return false;
        }

        String photosFolder = normalizeFolder(oss.getPhotosFolder(), "photos/");
        String cdnDomain = trimTrailingSlash(oss.getCdnDomain());
        if (cdnDomain != null && !cdnDomain.isBlank()) {
            if (!cdnDomain.startsWith("http://") && !cdnDomain.startsWith("https://")) {
                cdnDomain = "https://" + cdnDomain;
            }
            String cdnPrefix = cdnDomain + "/" + photosFolder;
            if (trimmed.startsWith(cdnPrefix)) {
                return true;
            }
        }

        String endpoint = trimTrailingSlash(oss.getEndpoint());
        if (oss.getBucketName() != null && !oss.getBucketName().isBlank() && endpoint != null && !endpoint.isBlank()) {
            String ossPrefix = "https://" + oss.getBucketName() + "." + endpoint + "/" + photosFolder;
            return trimmed.startsWith(ossPrefix);
        }
        return false;
    }

    private String normalizeFolder(String folder, String fallback) {
        String normalized = folder;
        if (normalized == null || normalized.isBlank()) normalized = fallback;
        return normalized.replaceAll("^/+", "").replaceAll("/+$", "") + "/";
    }

    private String trimTrailingSlash(String value) {
        if (value == null) return "";
        return value.replaceAll("/+$", "");
    }

    private List<String> parsePhotosJson(String photosJson) {
        if (photosJson == null || photosJson.trim().isEmpty()) {
            return new ArrayList<>();
        }
        try {
            return objectMapper.readValue(photosJson, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            System.err.println("解析照片JSON失败: " + e.getMessage());
            return new ArrayList<>();
        }
    }

}
