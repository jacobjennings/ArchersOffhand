package com.carloplayz.archersoffhand.config;

import com.google.gson.JsonParser;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/** Stable, human-independent tokens persisted by the ordered preference UI. */
public final class ProjectilePreferenceTokens {
    public static final String ANY_PROJECTILE = "any:projectile";
    public static final String ANY_ARROW = "any:arrow";
    public static final String ANY_TIPPED_ARROW = "any:tipped_arrow";
    public static final String NORMAL_ARROW = "item:minecraft:arrow";
    public static final String SPECTRAL_ARROW = "item:minecraft:spectral_arrow";
    public static final String ANY_FIREWORK = "any:firework";

    private ProjectilePreferenceTokens() {
    }

    public static String item(String itemId) {
        return "item:" + itemId;
    }

    public static String potion(String potionId) {
        return "potion:" + potionId;
    }

    public static String fireworkFlight(int flightDuration) {
        return "firework:flight:" + flightDuration;
    }

    public static String fireworkUtility(int flightDuration) {
        return "firework:utility:" + flightDuration;
    }

    public static String fireworkExplosive(int flightDuration) {
        return "firework:explosive:" + flightDuration;
    }

    public static String fireworkShape(String shape) {
        return "firework:shape:" + shape;
    }

    public static String exactStack(String json) {
        return "exact:" + Base64.getUrlEncoder().withoutPadding()
                .encodeToString(json.getBytes(StandardCharsets.UTF_8));
    }

    public static String exactStackJson(String token) {
        if (token == null || !token.startsWith("exact:")) {
            return "";
        }
        try {
            return new String(Base64.getUrlDecoder().decode(token.substring(6)), StandardCharsets.UTF_8);
        } catch (IllegalArgumentException ignored) {
            return "";
        }
    }

    public static boolean isValid(String token) {
        if (token == null || token.isBlank()) {
            return false;
        }
        if (token.equals(ANY_PROJECTILE)
                || token.equals(ANY_ARROW)
                || token.equals(ANY_TIPPED_ARROW)
                || token.equals(ANY_FIREWORK)) {
            return true;
        }
        if (token.startsWith("item:")) {
            return hasNamespacedPayload(token, "item:");
        }
        if (token.startsWith("potion:")) {
            return hasNamespacedPayload(token, "potion:");
        }
        if (token.startsWith("firework:")) {
            String[] parts = token.split(":", 3);
            return parts.length == 3 && !parts[1].isBlank() && !parts[2].isBlank();
        }
        if (token.startsWith("exact:")) {
            String json = exactStackJson(token);
            if (json.isEmpty()) {
                return false;
            }
            try {
                return JsonParser.parseString(json).isJsonObject();
            } catch (RuntimeException ignored) {
                return false;
            }
        }
        return false;
    }

    private static boolean hasNamespacedPayload(String token, String prefix) {
        String payload = token.substring(prefix.length());
        int separator = payload.indexOf(':');
        return separator > 0 && separator < payload.length() - 1;
    }
}
