package com.lovecube.backend.notification;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * 轻量防绕过扫描：在 {@code mvn test} 时打印告警，不失败构建。
 * <p>规则：{@code UserNotificationRepository} 不得出现在 {@code src/main/java} 中
 * 除 {@code NotificationService} 与接口自身以外的源码里。</p>
 */
class NotificationRepositoryBypassGuardTest {

    @Test
    void warnIfUserNotificationRepositoryUsedOutsideNotificationService() throws IOException {
        Path mainJava = resolveMainJavaRoot();
        if (mainJava == null || !Files.isDirectory(mainJava)) {
            System.err.println("[NOTIFICATION-GUARD] WARN: src/main/java not found, skip scan (cwd="
                + Paths.get("").toAbsolutePath() + ")");
            return;
        }
        List<String> warnings = new ArrayList<>();
        try (Stream<Path> walk = Files.walk(mainJava)) {
            walk.filter(p -> p.toString().endsWith(".java"))
                .forEach(p -> scanRepositoryReference(p, warnings));
        }
        for (String w : warnings) {
            System.err.println(w);
        }
        if (warnings.isEmpty()) {
            System.out.println("[NOTIFICATION-GUARD] OK: UserNotificationRepository only in NotificationService / repository interface");
        } else {
            System.err.println("[NOTIFICATION-GUARD] " + warnings.size()
                + " warning(s); route persistence through NotificationService");
        }
    }

    private static void scanRepositoryReference(Path path, List<String> warnings) {
        String rel = path.toString().replace('\\', '/');
        if (rel.endsWith("/NotificationService.java") || rel.endsWith("/UserNotificationRepository.java")) {
            return;
        }
        try {
            String text = Files.readString(path);
            if (!text.contains("UserNotificationRepository")) {
                return;
            }
            warnings.add("[NOTIFICATION-GUARD] WARN: UserNotificationRepository referenced outside NotificationService: "
                + path.toAbsolutePath().normalize());
        } catch (IOException e) {
            warnings.add("[NOTIFICATION-GUARD] WARN: could not read " + path + ": " + e.getMessage());
        }
    }

    private static Path resolveMainJavaRoot() {
        Path cwd = Paths.get("").toAbsolutePath().normalize();
        Path a = cwd.resolve("src/main/java");
        if (Files.isDirectory(a)) {
            return a;
        }
        Path b = cwd.resolve("backend/src/main/java");
        if (Files.isDirectory(b)) {
            return b;
        }
        return null;
    }
}
