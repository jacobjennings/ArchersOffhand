package com.carloplayz.archersoffhand.config;

import java.util.ArrayList;
import java.util.List;

/** Persistent user settings. Public fields keep the on-disk JSON readable. */
public final class ArchersOffhandConfig {
    public boolean enabled = true;
    public boolean scanHotbar = true;
    public boolean replaceOccupiedOffhand = true;
    public boolean restoreOnLowHealth = true;
    public double lowHealthThreshold = 6.0;
    public int equipDelayTicks = 2;
    public int restoreDelayTicks = 2;
    public int clickCooldownTicks = 2;
    public boolean debugLogging = false;

    /** First matching rule wins. Later entries are user-controlled fallbacks. */
    public List<String> projectilePreferences = new ArrayList<>(List.of(
            ProjectilePreferenceTokens.NORMAL_ARROW));

    /** Applied only after every ordered preference fails. */
    public FallbackPolicy fallbackPolicy = FallbackPolicy.ANY_PROJECTILE;

    public enum FallbackPolicy {
        NONE,
        ANY_ARROW,
        ANY_FIREWORK,
        ANY_PROJECTILE
    }

    public void validate() {
        if (projectilePreferences == null) {
            projectilePreferences = new ArrayList<>();
        } else {
            projectilePreferences = new ArrayList<>(projectilePreferences.stream()
                    .filter(ProjectilePreferenceTokens::isValid)
                    .distinct()
                    .toList());
        }
        if (fallbackPolicy == null) {
            fallbackPolicy = FallbackPolicy.NONE;
        }
        lowHealthThreshold = Math.max(0.0, Math.min(20.0, lowHealthThreshold));
        equipDelayTicks = clampTicks(equipDelayTicks);
        restoreDelayTicks = clampTicks(restoreDelayTicks);
        clickCooldownTicks = clampTicks(clickCooldownTicks);
    }

    private static int clampTicks(int value) {
        return Math.max(0, Math.min(40, value));
    }
}
