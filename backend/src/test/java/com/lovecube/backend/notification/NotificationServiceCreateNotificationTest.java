package com.lovecube.backend.notification;

import com.lovecube.backend.entity.UserNotification;
import com.lovecube.backend.repository.NotificationDispatchRecordRepository;
import com.lovecube.backend.repository.UserNotificationChannelPrefRepository;
import com.lovecube.backend.repository.UserNotificationRepository;
import com.lovecube.backend.repository.UserNotificationSettingRepository;
import com.lovecube.backend.repository.UserRepository;
import com.lovecube.backend.repository.UserSocialBindingRepository;
import com.lovecube.backend.services.NotificationDispatchService;
import com.lovecube.backend.services.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationServiceCreateNotificationTest {

    @Mock
    private UserNotificationRepository userNotificationRepository;
    @Mock
    private UserNotificationSettingRepository settingRepository;
    @Mock
    private UserRepository userRepository;

    private TrackingDispatchService trackingDispatch;
    private NotificationService notificationService;

    @BeforeEach
    void setUp() {
        trackingDispatch = new TrackingDispatchService();
        notificationService = new NotificationService(
                userNotificationRepository,
                settingRepository,
                trackingDispatch,
                userRepository
        );
        when(userNotificationRepository.save(any(UserNotification.class))).thenAnswer(inv -> {
            UserNotification n = inv.getArgument(0);
            n.setId(42L);
            return n;
        });
        when(settingRepository.findByUserIdAndType(any(), any())).thenReturn(java.util.Optional.empty());
    }

    @Test
    void createNotification_persistsBeforeSchedulingExternalDispatch() {
        UserNotification saved = notificationService.createNotification(
                1L,
                NotificationCatalog.TYPE_CONTENT_LIKED,
                "有人喜欢了你",
                "点击查看",
                "/fellowship/messages",
                "USER",
                "9"
        );

        assertNotNull(saved);
        assertEquals(42L, saved.getId());
        assertEquals(42L, trackingDispatch.scheduledId);

        InOrder order = inOrder(userNotificationRepository);
        order.verify(userNotificationRepository).save(any(UserNotification.class));
    }

    @Test
    void createNotification_returnsSavedEvenIfDispatchSchedulerThrows() {
        trackingDispatch.throwOnSchedule = new RuntimeException("executor rejected");

        UserNotification saved = notificationService.createNotification(
                1L,
                NotificationCatalog.TYPE_CONTENT_LIKED,
                "t",
                "c",
                null,
                "USER",
                "1"
        );

        assertNotNull(saved);
        assertEquals(42L, saved.getId());
    }

    /** 可跟踪的投递服务子类，避免 Java 25 下 mock 具体类失败。 */
    private static final class TrackingDispatchService extends NotificationDispatchService {
        Long scheduledId;
        RuntimeException throwOnSchedule;

        TrackingDispatchService() {
            super(
                    mock(UserNotificationSettingRepository.class),
                    mock(UserSocialBindingRepository.class),
                    mock(UserNotificationChannelPrefRepository.class),
                    mock(NotificationDispatchRecordRepository.class),
                    mock(UserNotificationRepository.class),
                    mock(UserRepository.class),
                    null
            );
        }

        @Override
        public void scheduleExternalDispatch(Long notificationId) {
            if (throwOnSchedule != null) {
                throw throwOnSchedule;
            }
            scheduledId = notificationId;
        }

        @Override
        public void applyPushInMemory(UserNotification notification) {
            // no-op for test
        }
    }
}
