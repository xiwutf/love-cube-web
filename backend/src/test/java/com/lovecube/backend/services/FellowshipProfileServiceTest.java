package com.lovecube.backend.services;

import com.lovecube.backend.models.User;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;
class FellowshipProfileServiceTest {

    @Test
    void completionShouldChangeAfterUpdate() {
        FakeUnifiedProfileService fakeUnifiedProfileService = new FakeUnifiedProfileService();
        FellowshipProfileService fellowshipProfileService = new FellowshipProfileService(fakeUnifiedProfileService);

        User user = new User();
        user.setUserid(1L);
        user.setUsername("tester");

        Map<String, Object> before = fellowshipProfileService.getCompletion(user);
        assertThat(before.get("completed")).isEqualTo(false);

        Map<String, Object> payload = new HashMap<>();
        payload.put("nickname", "tester");
        payload.put("gender", "male");
        payload.put("birthYear", 1995);
        payload.put("city", "Shanghai");
        payload.put("occupation", "Engineer");
        payload.put("education", "Bachelor");
        payload.put("height", 175);
        payload.put("bio", "hello");
        payload.put("intention", "serious");
        payload.put("avatarUrl", "https://img/test.png");
        fellowshipProfileService.updateMyProfile(user, payload);

        Map<String, Object> after = fellowshipProfileService.getCompletion(user);
        assertThat(after.get("completed")).isEqualTo(true);
        assertThat(after.get("percent")).isEqualTo(100);
    }

    private static class FakeUnifiedProfileService extends UnifiedProfileService {
        private final AtomicBoolean updated = new AtomicBoolean(false);

        FakeUnifiedProfileService() {
            super(null, null, null, null, null, null, null, null, null, null);
        }

        @Override
        public Map<String, Object> updateFellowshipProfile(User user, Map<String, Object> payload) {
            updated.set(true);
            return Map.of();
        }

        @Override
        public Map<String, Object> buildFellowshipCompletion(User user) {
            if (updated.get()) {
                return Map.of("completed", true, "percent", 100);
            }
            return Map.of("completed", false, "percent", 0);
        }
    }
}
