package com.lovecube.backend.notification;

import com.lovecube.backend.entity.NotificationDispatchRecord;
import com.lovecube.backend.entity.UserNotification;
import com.lovecube.backend.entity.UserNotificationChannelPref;
import com.lovecube.backend.models.User;
import com.lovecube.backend.repository.NotificationDispatchRecordRepository;
import com.lovecube.backend.repository.UserNotificationChannelPrefRepository;
import com.lovecube.backend.repository.UserNotificationRepository;
import com.lovecube.backend.repository.UserNotificationSettingRepository;
import com.lovecube.backend.repository.UserRepository;
import com.lovecube.backend.repository.UserSocialBindingRepository;
import com.lovecube.backend.services.NotificationDispatchService;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationDispatchBoundaryTest {

    private static final Long USER_ID = 100L;
    private static final Long NOTIFICATION_ID = 200L;

    @Mock
    private UserNotificationSettingRepository settingRepository;
    @Mock
    private UserSocialBindingRepository socialBindingRepository;
    @Mock
    private UserNotificationChannelPrefRepository channelPrefRepository;
    @Mock
    private NotificationDispatchRecordRepository dispatchRecordRepository;
    @Mock
    private UserNotificationRepository userNotificationRepository;
    @Mock
    private UserRepository userRepository;

    private NotificationDispatchService dispatchService;
    private TestMailSender testMailSender;
    private StubRestTemplate stubRestTemplate;
    private final AtomicLong recordSeq = new AtomicLong(1);

    @BeforeEach
    void setUp() {
        testMailSender = new TestMailSender();
        stubRestTemplate = new StubRestTemplate();
        dispatchService = new NotificationDispatchService(
                settingRepository,
                socialBindingRepository,
                channelPrefRepository,
                dispatchRecordRepository,
                userNotificationRepository,
                userRepository,
                testMailSender
        );
        ReflectionTestUtils.setField(dispatchService, "mailEnabled", false);
        ReflectionTestUtils.setField(dispatchService, "mailFrom", "noreply@lovecube.test");
        ReflectionTestUtils.setField(dispatchService, "mailFromName", "Love Cube");
        ReflectionTestUtils.setField(dispatchService, "pushplusUrl", "https://www.pushplus.plus/send");
        ReflectionTestUtils.setField(dispatchService, "restTemplate", stubRestTemplate);

        lenient().when(dispatchRecordRepository.save(any(NotificationDispatchRecord.class))).thenAnswer(inv -> {
            NotificationDispatchRecord r = inv.getArgument(0);
            if (r.getId() == null) {
                r.setId(recordSeq.getAndIncrement());
            }
            return r;
        });
    }

    @Test
    void mailDisabled_skipsEmailWithoutThrowing() {
        stubNotification(NotificationCatalog.TYPE_CONTENT_LIKED);
        stubUserWithEmail("user@example.com");
        stubChannelPref(true, false, null);

        assertDoesNotThrow(() -> dispatchService.scheduleExternalDispatch(NOTIFICATION_ID));

        NotificationDispatchRecord email = captureRecord(NotificationDispatchService.CHANNEL_EMAIL);
        assertEquals(NotificationDispatchService.DISPATCH_SKIPPED, email.getStatus());
        assertEquals("邮件服务未配置", email.getErrorMessage());
        assertEquals(0, testMailSender.sendCount);
    }

    @Test
    void mailSenderNull_skipsEmailWithoutThrowing() {
        ReflectionTestUtils.setField(dispatchService, "mailEnabled", true);
        ReflectionTestUtils.setField(dispatchService, "mailSender", null);

        stubNotification(NotificationCatalog.TYPE_CONTENT_LIKED);
        stubUserWithEmail("user@example.com");
        stubChannelPref(true, false, null);

        assertDoesNotThrow(() -> dispatchService.scheduleExternalDispatch(NOTIFICATION_ID));

        NotificationDispatchRecord email = captureRecord(NotificationDispatchService.CHANNEL_EMAIL);
        assertEquals(NotificationDispatchService.DISPATCH_SKIPPED, email.getStatus());
        assertEquals("邮件服务未配置", email.getErrorMessage());
    }

    @Test
    void emailNotEnabled_writesSkippedRecord() {
        stubNotification(NotificationCatalog.TYPE_CONTENT_LIKED);
        stubUserWithEmail("user@example.com");
        stubChannelPref(false, false, null);

        dispatchService.scheduleExternalDispatch(NOTIFICATION_ID);

        NotificationDispatchRecord email = captureRecord(NotificationDispatchService.CHANNEL_EMAIL);
        assertEquals(NotificationDispatchService.DISPATCH_SKIPPED, email.getStatus());
        assertEquals("邮件通知未开启", email.getErrorMessage());
    }

    @Test
    void pushplusNotEnabled_writesSkippedRecord() {
        stubNotification(NotificationCatalog.TYPE_CONTENT_LIKED);
        stubChannelPref(false, false, null);

        dispatchService.scheduleExternalDispatch(NOTIFICATION_ID);

        NotificationDispatchRecord push = captureRecord(NotificationDispatchService.CHANNEL_PUSHPLUS);
        assertEquals(NotificationDispatchService.DISPATCH_SKIPPED, push.getStatus());
        assertEquals("PushPlus 未开启", push.getErrorMessage());
    }

    @Test
    void pushplusEnabledButEmptyToken_writesSkippedRecord() {
        stubNotification(NotificationCatalog.TYPE_CONTENT_LIKED);
        UserNotificationChannelPref pref = stubChannelPref(false, true, "   ");

        dispatchService.scheduleExternalDispatch(NOTIFICATION_ID);

        NotificationDispatchRecord push = captureRecord(NotificationDispatchService.CHANNEL_PUSHPLUS);
        assertEquals(NotificationDispatchService.DISPATCH_SKIPPED, push.getStatus());
        assertEquals("未配置 PushPlus Token", push.getErrorMessage());
        assertTrue(pref.getPushplusToken() == null || pref.getPushplusToken().isBlank());
    }

    @Test
    void pushplusHttpFailure_writesFailedWithMessage() {
        stubNotification(NotificationCatalog.TYPE_CONTENT_LIKED);
        stubChannelPref(false, true, "token-abc");

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("code", 400);
        body.put("msg", "token无效");
        stubRestTemplate.handler = req -> ResponseEntity.ok(body);

        dispatchService.scheduleExternalDispatch(NOTIFICATION_ID);

        NotificationDispatchRecord push = captureRecord(NotificationDispatchService.CHANNEL_PUSHPLUS);
        assertEquals(NotificationDispatchService.DISPATCH_FAILED, push.getStatus());
        assertEquals("token无效", push.getErrorMessage());
    }

    @Test
    void pushplusHttpException_writesFailedWithMessage() {
        stubNotification(NotificationCatalog.TYPE_CONTENT_LIKED);
        stubChannelPref(false, true, "token-abc");
        stubRestTemplate.handler = req -> {
            throw new RuntimeException("connection reset");
        };

        dispatchService.scheduleExternalDispatch(NOTIFICATION_ID);

        NotificationDispatchRecord push = captureRecord(NotificationDispatchService.CHANNEL_PUSHPLUS);
        assertEquals(NotificationDispatchService.DISPATCH_FAILED, push.getStatus());
        assertEquals("connection reset", push.getErrorMessage());
    }

    @Test
    void profileViewed_writesNoExternalDispatchRecords() {
        stubNotification(NotificationCatalog.TYPE_PROFILE_VIEWED);

        dispatchService.scheduleExternalDispatch(NOTIFICATION_ID);

        verify(dispatchRecordRepository, never()).save(any(NotificationDispatchRecord.class));
    }

    @Test
    void mailSendFailure_writesFailedWithoutThrowing() {
        ReflectionTestUtils.setField(dispatchService, "mailEnabled", true);
        stubNotification(NotificationCatalog.TYPE_CONTENT_LIKED);
        stubUserWithEmail("user@example.com");
        stubChannelPref(true, false, null);
        testMailSender.error = new RuntimeException("SMTP down");

        assertDoesNotThrow(() -> dispatchService.scheduleExternalDispatch(NOTIFICATION_ID));

        NotificationDispatchRecord email = captureRecord(NotificationDispatchService.CHANNEL_EMAIL);
        assertEquals(NotificationDispatchService.DISPATCH_FAILED, email.getStatus());
        assertEquals("SMTP down", email.getErrorMessage());
    }

    private void stubNotification(String type) {
        UserNotification n = new UserNotification();
        n.setId(NOTIFICATION_ID);
        n.setUserId(USER_ID);
        n.setType(type);
        n.setTitle("标题");
        n.setContent("内容");
        when(userNotificationRepository.findById(NOTIFICATION_ID)).thenReturn(Optional.of(n));
    }

    private void stubUserWithEmail(String email) {
        User user = new User();
        user.setUserid(USER_ID);
        user.setEmail(email);
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
    }

    private UserNotificationChannelPref stubChannelPref(boolean emailOn, boolean pushOn, String token) {
        UserNotificationChannelPref pref = new UserNotificationChannelPref();
        pref.setUserId(USER_ID);
        pref.setEmailEnabled(emailOn);
        pref.setPushplusEnabled(pushOn);
        pref.setPushplusToken(token);
        when(channelPrefRepository.findByUserId(USER_ID)).thenReturn(Optional.of(pref));
        return pref;
    }

    private NotificationDispatchRecord captureRecord(String channel) {
        ArgumentCaptor<NotificationDispatchRecord> cap = ArgumentCaptor.forClass(NotificationDispatchRecord.class);
        verify(dispatchRecordRepository, org.mockito.Mockito.atLeastOnce()).save(cap.capture());
        return cap.getAllValues().stream()
                .filter(r -> channel.equals(r.getChannel()))
                .reduce((first, second) -> second)
                .orElseThrow(() -> new AssertionError("no record for channel " + channel));
    }

    private static final class TestMailSender implements JavaMailSender {
        int sendCount;
        RuntimeException error;

        @Override
        public void send(SimpleMailMessage simpleMessage) {
            if (error != null) {
                throw error;
            }
            sendCount++;
        }

        @Override
        public void send(SimpleMailMessage... simpleMessages) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void send(MimeMessage mimeMessage) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void send(MimeMessage... mimeMessages) {
            throw new UnsupportedOperationException();
        }

        @Override
        public MimeMessage createMimeMessage() {
            throw new UnsupportedOperationException();
        }

        @Override
        public MimeMessage createMimeMessage(InputStream contentStream) {
            throw new UnsupportedOperationException();
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static final class StubRestTemplate extends RestTemplate {
        Function<HttpEntity<?>, ResponseEntity<Map>> handler = req -> ResponseEntity.ok(Map.of("code", 200));

        @Override
        public <T> ResponseEntity<T> postForEntity(String url, Object request, Class<T> responseType, Object... uriVariables) {
            ResponseEntity<Map> resp = handler.apply((HttpEntity<?>) request);
            return (ResponseEntity<T>) resp;
        }
    }
}
