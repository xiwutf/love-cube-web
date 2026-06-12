package com.lovecube.backend.services;

import com.lovecube.backend.entity.PlatformEvent;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PlatformEventLifecycleTest {

    @Test
    void resolveEnd_defaultsToThreeHoursAfterStartWhenMissing() {
        PlatformEvent event = new PlatformEvent();
        event.setEventTime(LocalDateTime.of(2026, 6, 19, 14, 0));

        assertEquals(
                LocalDateTime.of(2026, 6, 19, 17, 0),
                PlatformEventLifecycle.resolveEnd(event)
        );
    }

    @Test
    void resolveParticipationStatus_beforeStartWhenCheckedIn() {
        PlatformEvent event = new PlatformEvent();
        event.setEventTime(LocalDateTime.of(2026, 6, 19, 14, 0));
        event.setEndTime(LocalDateTime.of(2026, 6, 19, 17, 30));
        LocalDateTime now = LocalDateTime.of(2026, 6, 12, 15, 0);

        assertEquals(
                "checked_in_waiting",
                PlatformEventLifecycle.resolveParticipationStatus(true, event, now, false)
        );
    }

    @Test
    void isEnded_usesEndTimeNotStartTime() {
        PlatformEvent event = new PlatformEvent();
        event.setEventTime(LocalDateTime.of(2026, 6, 19, 14, 0));
        event.setEndTime(LocalDateTime.of(2026, 6, 19, 17, 30));
        LocalDateTime during = LocalDateTime.of(2026, 6, 19, 15, 0);
        LocalDateTime after = LocalDateTime.of(2026, 6, 19, 18, 0);

        assertFalse(PlatformEventLifecycle.isEnded(event, during));
        assertTrue(PlatformEventLifecycle.isEnded(event, after));
    }
}
