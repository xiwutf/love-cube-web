package com.lovecube.backend.utils;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UploadUrlSupportTest {

    @Test
    void localStoredUrlShouldBeBrowserAdminUploadsPath() {
        assertThat(UploadUrlSupport.toLocalStoredUrl("uploads/photos/", "a.jpg"))
                .isEqualTo("/admin/uploads/photos/a.jpg");
        assertThat(UploadUrlSupport.toLocalStoredUrl("/uploads/avatar", "b.png"))
                .isEqualTo("/admin/uploads/avatar/b.png");
    }

    @Test
    void relativeUploadsPathShouldGainAdminPrefix() {
        assertThat(UploadUrlSupport.toPublicApiUrl("/uploads/photos/a.jpg"))
                .isEqualTo("/admin/uploads/photos/a.jpg");
        assertThat(UploadUrlSupport.toPublicApiUrl("/admin/uploads/photos/a.jpg"))
                .isEqualTo("/admin/uploads/photos/a.jpg");
    }

    @Test
    void localhostAndLegacyAbsoluteUploadUrlsShouldBecomeSiteRelative() {
        assertThat(UploadUrlSupport.toPublicApiUrl("http://localhost:8090/admin/uploads/photos/a.jpg"))
                .isEqualTo("/admin/uploads/photos/a.jpg");
        assertThat(UploadUrlSupport.toPublicApiUrl("http://127.0.0.1:8090/uploads/avatar/b.png"))
                .isEqualTo("/admin/uploads/avatar/b.png");
        assertThat(UploadUrlSupport.toPublicApiUrl("http://example.com:8090/admin/uploads/photos/a.jpg"))
                .isEqualTo("/admin/uploads/photos/a.jpg");
    }

    @Test
    void publicOssUrlShouldStayUnchanged() {
        String oss = "https://love-cube-files.oss-cn-beijing.aliyuncs.com/photos/a.jpg";
        assertThat(UploadUrlSupport.toPublicApiUrl(oss)).isEqualTo(oss);
    }

    @Test
    void filesystemPathShouldStripAdminPrefix() {
        assertThat(UploadUrlSupport.toFilesystemRelativePath("http://localhost:8090/admin/uploads/photos/a.jpg"))
                .isEqualTo("/uploads/photos/a.jpg");
        assertThat(UploadUrlSupport.toFilesystemRelativePath("/admin/uploads/photos/a.jpg"))
                .isEqualTo("/uploads/photos/a.jpg");
        assertThat(UploadUrlSupport.toFilesystemRelativePath("/uploads/photos/a.jpg"))
                .isEqualTo("/uploads/photos/a.jpg");
        assertThat(UploadUrlSupport.toFilesystemRelativePath(ossUrl())).isNull();
    }

    private String ossUrl() {
        return "https://love-cube-files.oss-cn-beijing.aliyuncs.com/photos/a.jpg";
    }
}
