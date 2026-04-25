package com.lovecube.backend.services;

import com.lovecube.backend.entity.FellowshipProfile;
import com.lovecube.backend.models.User;
import com.lovecube.backend.repository.FellowshipProfileRepository;
import com.lovecube.backend.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FellowshipProfileServiceTest {

    @Mock
    private FellowshipProfileRepository fellowshipProfileRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private FellowshipProfileService fellowshipProfileService;

    @Test
    void completionShouldChangeAfterUpdate() {
        User user = new User();
        user.setUserid(1L);
        user.setUsername("tester");

        FellowshipProfile profile = new FellowshipProfile();
        profile.setUserId(1L);
        profile.setProfileStatus("INCOMPLETE");
        profile.setReviewStatus("PENDING");

        when(fellowshipProfileRepository.findByUserId(1L)).thenReturn(Optional.of(profile));
        when(fellowshipProfileRepository.save(any(FellowshipProfile.class))).thenAnswer(inv -> inv.getArgument(0));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

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
}

