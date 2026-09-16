package com.lovecube.backend.utils;

import java.net.URI;

/**
 * 本地上传对外 URL：返回站点相对路径 {@code /admin/uploads/...}。
 * 磁盘相对路径仍是 {@code /uploads/...}（Spring context-path=/admin）。
 */
public final class UploadUrlSupport {

    public static final String CONTEXT_PATH = "/admin";
    public static final String BROWSER_UPLOAD_PREFIX = CONTEXT_PATH + "/uploads/";
    public static final String FILE_UPLOAD_PREFIX = "/uploads/";

    private UploadUrlSupport() {
    }

    public static String toLocalStoredUrl(String folder, String filename) {
        return toBrowserUploadPath("/" + trimSlash(folder) + "/" + filename);
    }

    /**
     * 把 localhost / 相对 /uploads 收成浏览器可请求的 /admin/uploads；OSS 公网 URL 原样返回。
     */
    public static String toPublicApiUrl(String fileUrl) {
        if (fileUrl == null || fileUrl.isBlank()) {
            return fileUrl;
        }
        String trimmed = fileUrl.trim();
        if (!looksLikeHttpUrl(trimmed)) {
            return toBrowserUploadPath(trimmed);
        }
        try {
            URI uri = URI.create(trimmed);
            String path = uri.getPath();
            if (path != null && isUploadPath(path)) {
                return toBrowserUploadPath(path);
            }
            return trimmed;
        } catch (IllegalArgumentException ex) {
            return trimmed;
        }
    }

    public static String toBrowserUploadPath(String path) {
        if (path == null || path.isBlank()) {
            return path;
        }
        String normalized = path.trim().replace('\\', '/');
        if (normalized.startsWith("admin/uploads/")) {
            normalized = "/" + normalized;
        }
        if (normalized.startsWith("/admin/api/uploads/")) {
            normalized = CONTEXT_PATH + FILE_UPLOAD_PREFIX + normalized.substring("/admin/api/uploads/".length());
        }
        if (normalized.startsWith("/admin/uploads/")) {
            return normalized;
        }
        if (normalized.startsWith("/uploads/")) {
            return CONTEXT_PATH + normalized;
        }
        if (normalized.startsWith("uploads/")) {
            return CONTEXT_PATH + "/" + normalized;
        }
        return normalized;
    }

    public static String toFilesystemRelativePath(String fileUrl) {
        if (fileUrl == null || fileUrl.isBlank()) {
            return null;
        }
        String publicUrl = toPublicApiUrl(fileUrl.trim());
        if (publicUrl == null || looksLikeHttpUrl(publicUrl)) {
            return null;
        }
        String path = publicUrl;
        if (path.startsWith("/admin/api/uploads/")) {
            path = FILE_UPLOAD_PREFIX + path.substring("/admin/api/uploads/".length());
        }
        if (path.startsWith(BROWSER_UPLOAD_PREFIX)) {
            path = path.substring(CONTEXT_PATH.length());
        }
        if (!path.startsWith(FILE_UPLOAD_PREFIX)) {
            return null;
        }
        return path;
    }

    public static boolean isLocalUploadUrl(String url) {
        return toFilesystemRelativePath(url) != null;
    }

    private static boolean isUploadPath(String path) {
        return path.startsWith(BROWSER_UPLOAD_PREFIX)
                || path.startsWith(FILE_UPLOAD_PREFIX)
                || path.startsWith("/admin/api/uploads/");
    }

    private static boolean looksLikeHttpUrl(String value) {
        String lower = value.toLowerCase();
        return lower.startsWith("http://") || lower.startsWith("https://");
    }

    private static String trimSlash(String value) {
        if (value == null) {
            return "";
        }
        return value.replaceAll("^/+", "").replaceAll("/+$", "");
    }
}
