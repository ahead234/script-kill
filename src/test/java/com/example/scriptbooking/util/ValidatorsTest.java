package com.example.scriptbooking.util;

import com.example.scriptbooking.exception.BusinessException;
import org.junit.jupiter.api.Test;
import org.mindrot.jbcrypt.BCrypt;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class ValidatorsTest {
    private static final String DEMO_HASH = "$2a$10$iZ8oCghoEjHJi2xPkXGOBO./w71zvGrMWzi5E7HgVOZkgA/yyWmX.";

    @Test
    void acceptsValidPlayerAndTimeRules() {
        assertEquals(3, Validators.positive(3, "人数"));
        assertEquals("13800001004", Validators.phone("13800001004"));
        assertDoesNotThrow(() -> Validators.timeRange(
                LocalDateTime.of(2026, 11, 1, 14, 0),
                LocalDateTime.of(2026, 11, 1, 18, 0)));
    }

    @Test
    void rejectsInvalidPlayerAndRatingRules() {
        assertThrows(BusinessException.class, () -> Validators.positive(0, "人数"));
        assertThrows(BusinessException.class, () -> Validators.range(6, 1, 5, "评分"));
        assertThrows(BusinessException.class, () -> Validators.phone("abc"));
        assertThrows(BusinessException.class, () -> Validators.timeRange(
                LocalDateTime.of(2026, 11, 1, 18, 0),
                LocalDateTime.of(2026, 11, 1, 14, 0)));
    }

    @Test
    void demoPasswordHashMatchesDocumentedPassword() {
        assertTrue(BCrypt.checkpw("password", DEMO_HASH));
        assertFalse(BCrypt.checkpw("wrong-password", DEMO_HASH));
    }
}
