package com.lovecube.backend.notification;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NotificationCatalogExternalChannelTest {

    @Test
    void profileViewed_skipsExternalChannels() {
        assertTrue(NotificationCatalog.forceSkipExternalChannels(NotificationCatalog.TYPE_PROFILE_VIEWED));
    }

    @Test
    void normalInteraction_allowsExternalChannels() {
        assertFalse(NotificationCatalog.forceSkipExternalChannels(NotificationCatalog.TYPE_CONTENT_LIKED));
    }
}
