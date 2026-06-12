package com.lovecube.backend.notification;

import com.lovecube.backend.entity.UserNotificationChannelPref;
import com.lovecube.backend.repository.UserNotificationChannelPrefRepository;
import com.lovecube.backend.repository.UserRepository;
import com.lovecube.backend.services.UserNotificationChannelPrefService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserNotificationChannelPrefServiceTest {

    @Mock
    private UserNotificationChannelPrefRepository prefRepository;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserNotificationChannelPrefService service;

    @Test
    void update_rejectsPushplusEnabledWithoutToken() {
        UserNotificationChannelPref pref = new UserNotificationChannelPref();
        pref.setUserId(7L);
        when(prefRepository.findByUserId(7L)).thenReturn(Optional.of(pref));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                service.update(7L, Map.of(
                        "pushplusEnabled", true,
                        "pushplusToken", ""
                ))
        );

        assertEquals("开启 PushPlus 前请填写 Token", ex.getMessage());
        verify(prefRepository, never()).save(any());
    }
}
