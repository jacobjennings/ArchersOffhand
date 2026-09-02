package com.carloplayz.archersoffhand.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import org.junit.jupiter.api.Test;

class ProjectilePreferenceTokensTest {
    @Test
    void factoriesUseStablePrefixesAndPayloads() {
        assertEquals("item:minecraft:arrow", ProjectilePreferenceTokens.item("minecraft:arrow"));
        assertEquals("potion:minecraft:strong_healing", ProjectilePreferenceTokens.potion("minecraft:strong_healing"));
        assertEquals("firework:flight:2", ProjectilePreferenceTokens.fireworkFlight(2));
        assertEquals("firework:utility:1", ProjectilePreferenceTokens.fireworkUtility(1));
        assertEquals("firework:explosive:3", ProjectilePreferenceTokens.fireworkExplosive(3));
        assertEquals("firework:shape:star", ProjectilePreferenceTokens.fireworkShape("star"));
    }

    @Test
    void exactStackEncodingIsDeterministicAndUtf8Safe() {
        String json = "{\"item\":\"minecraft:tipped_arrow\",\"custom_name\":\"Sérénité/箭\"}";

        String token = ProjectilePreferenceTokens.exactStack(json);

        assertEquals(token, ProjectilePreferenceTokens.exactStack(json));
        assertTrue(token.startsWith("exact:"));
        assertFalse(token.endsWith("="), "URL-safe tokens should not contain Base64 padding");
        assertEquals(json, ProjectilePreferenceTokens.exactStackJson(token));
        assertTrue(ProjectilePreferenceTokens.isValid(token));
    }

    @Test
    void decoderRejectsNullAndMalformedTokens() {
        assertEquals("", ProjectilePreferenceTokens.exactStackJson(null));
        assertEquals("", ProjectilePreferenceTokens.exactStackJson("item:minecraft:arrow"));
        assertEquals("", ProjectilePreferenceTokens.exactStackJson("exact:%%%"));

        String malformedJson = "exact:" + Base64.getUrlEncoder().withoutPadding()
                .encodeToString("{not-json".getBytes(StandardCharsets.UTF_8));
        assertEquals("{not-json", ProjectilePreferenceTokens.exactStackJson(malformedJson));

        assertFalse(ProjectilePreferenceTokens.isValid(null));
        assertFalse(ProjectilePreferenceTokens.isValid(""));
        assertFalse(ProjectilePreferenceTokens.isValid("  \t"));
        assertFalse(ProjectilePreferenceTokens.isValid("not-a-token"));
        assertFalse(ProjectilePreferenceTokens.isValid("exact:%%%"));
        assertFalse(ProjectilePreferenceTokens.isValid(malformedJson));
    }

    @Test
    void validatesKnownTokenShapes() {
        assertTrue(ProjectilePreferenceTokens.isValid(ProjectilePreferenceTokens.ANY_PROJECTILE));
        assertTrue(ProjectilePreferenceTokens.isValid(ProjectilePreferenceTokens.ANY_ARROW));
        assertTrue(ProjectilePreferenceTokens.isValid(ProjectilePreferenceTokens.NORMAL_ARROW));
        assertTrue(ProjectilePreferenceTokens.isValid(ProjectilePreferenceTokens.SPECTRAL_ARROW));
        assertTrue(ProjectilePreferenceTokens.isValid(ProjectilePreferenceTokens.ANY_FIREWORK));
        assertTrue(ProjectilePreferenceTokens.isValid(ProjectilePreferenceTokens.item("example:custom_arrow")));
        assertTrue(ProjectilePreferenceTokens.isValid(ProjectilePreferenceTokens.potion("minecraft:healing")));
        assertTrue(ProjectilePreferenceTokens.isValid(ProjectilePreferenceTokens.fireworkShape("burst")));

        assertFalse(ProjectilePreferenceTokens.isValid("any:"));
        assertFalse(ProjectilePreferenceTokens.isValid("item:"));
        assertFalse(ProjectilePreferenceTokens.isValid("potion:"));
        assertFalse(ProjectilePreferenceTokens.isValid("firework:"));
        assertFalse(ProjectilePreferenceTokens.isValid("exact:"));
    }
}
