package com.carloplayz.archersoffhand.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;

class ArchersOffhandConfigTest {
    @Test
    void validationRemovesNullMalformedAndDuplicatePreferencesInOrder() {
        ArchersOffhandConfig config = new ArchersOffhandConfig();
        String exact = ProjectilePreferenceTokens.exactStack("{\"item\":\"minecraft:arrow\"}");
        config.projectilePreferences = new ArrayList<>(Arrays.asList(
                ProjectilePreferenceTokens.ANY_FIREWORK,
                null,
                "not-a-token",
                ProjectilePreferenceTokens.ANY_FIREWORK,
                ProjectilePreferenceTokens.NORMAL_ARROW,
                "exact:%%%",
                exact,
                exact));

        config.validate();

        assertEquals(List.of(
                ProjectilePreferenceTokens.ANY_FIREWORK,
                ProjectilePreferenceTokens.NORMAL_ARROW,
                exact), config.projectilePreferences);
    }

    @Test
    void nullPreferenceListIsReplacedAndNullFallbackDisablesImplicitSelection() {
        ArchersOffhandConfig config = new ArchersOffhandConfig();
        config.projectilePreferences = null;
        config.fallbackPolicy = null;

        config.validate();

        assertNotNull(config.projectilePreferences);
        assertTrue(config.projectilePreferences.isEmpty());
        assertEquals(ArchersOffhandConfig.FallbackPolicy.NONE, config.fallbackPolicy);
    }

    @Test
    void numericSettingsAreClampedToSafeRanges() {
        ArchersOffhandConfig config = new ArchersOffhandConfig();
        config.lowHealthThreshold = -Double.MAX_VALUE;
        config.equipDelayTicks = Integer.MIN_VALUE;
        config.restoreDelayTicks = Integer.MIN_VALUE;
        config.clickCooldownTicks = Integer.MIN_VALUE;

        config.validate();

        assertEquals(0.0, config.lowHealthThreshold);
        assertEquals(0, config.equipDelayTicks);
        assertEquals(0, config.restoreDelayTicks);
        assertEquals(0, config.clickCooldownTicks);

        config.lowHealthThreshold = Double.MAX_VALUE;
        config.equipDelayTicks = Integer.MAX_VALUE;
        config.restoreDelayTicks = Integer.MAX_VALUE;
        config.clickCooldownTicks = Integer.MAX_VALUE;

        config.validate();

        assertEquals(20.0, config.lowHealthThreshold);
        assertEquals(40, config.equipDelayTicks);
        assertEquals(40, config.restoreDelayTicks);
        assertEquals(40, config.clickCooldownTicks);
    }

    @Test
    void validationIsIdempotent() {
        ArchersOffhandConfig config = new ArchersOffhandConfig();
        config.projectilePreferences = new ArrayList<>(Arrays.asList(
                ProjectilePreferenceTokens.NORMAL_ARROW,
                ProjectilePreferenceTokens.NORMAL_ARROW,
                null));
        config.lowHealthThreshold = 100.0;
        config.equipDelayTicks = -1;

        config.validate();
        List<String> preferencesAfterFirstValidation = List.copyOf(config.projectilePreferences);
        double thresholdAfterFirstValidation = config.lowHealthThreshold;
        int delayAfterFirstValidation = config.equipDelayTicks;

        config.validate();

        assertEquals(preferencesAfterFirstValidation, config.projectilePreferences);
        assertEquals(thresholdAfterFirstValidation, config.lowHealthThreshold);
        assertEquals(delayAfterFirstValidation, config.equipDelayTicks);
    }
}
