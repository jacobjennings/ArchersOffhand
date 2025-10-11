package com.carloplayz.archersoffhand.config;

import java.util.ArrayList;
import java.util.List;

/**
 * POJO-style config. Will be serialized to/from JSON by ConfigManager.
 * Keep all fields public for simple GSON (or other) serialization.
 */
public class ArchersOffhandConfig {
    // General
    public boolean enabled = true;
    public int actionDelayTicks = 8; // default 8 ticks (0.4s)
    public int cooldownTicks = 10; // configurable cooldown to prevent scanning every tick
    public int scanIntervalTicks = 5; // default scan interval
    public int cacheTTL = 20; // cache time-to-live in ticks
    public double lowHealthThreshold = 6.0; // hearts = 3.0 * 2? using raw health
    public boolean restoreOffhandOnLowHealth = true; // config.revertOnLowHealth (boolean)
    public boolean allowReplaceShieldTotem = true; // whether to allow replacing shields or totems
    public boolean allowPlainAmmo = true; // whether to allow plain arrows/rockets
    public OperationMode operationMode = OperationMode.NORMAL;
    public AmmoSwitchMode ammoMode = AmmoSwitchMode.REGULAR; // ammo switching mode
    public CrossbowAmmoType crossbowAmmoType = CrossbowAmmoType.AUTO; // whether to use arrows or rockets with crossbow

    // Ammo priority (by preference object, top = highest priority)
    public List<ArrowPreference> arrowPriority = new ArrayList<>();
    public List<RocketPreference> rocketPriority = new ArrayList<>();

    // Advanced
    public boolean debugLogging = false;
    public boolean scanHotbar = false; // whether to include hotbar in scanning (default: false)

    public ArchersOffhandConfig() {
        // sensible defaults
        arrowPriority.add(new ArrowPreference("minecraft:tipped_arrow", "Tipped Arrow", "Tipped"));
        arrowPriority.add(new ArrowPreference("minecraft:spectral_arrow", "Spectral Arrow", "Special"));
        arrowPriority.add(new ArrowPreference("minecraft:arrow", "Arrow", "Basic"));

        // rocket preferences: by flight duration (higher better) and explosive flag
        rocketPriority.add(new RocketPreference(3, true));
        rocketPriority.add(new RocketPreference(2, false));
        rocketPriority.add(new RocketPreference(1, false));
    }

    public enum OperationMode { NORMAL, SHUFFLE, SERIAL }
    
    public enum AmmoSwitchMode { REGULAR, SHUFFLE, SERIAL }
    
    public enum CrossbowAmmoType { ARROWS, ROCKETS, AUTO }

    public static class RocketPreference {
        public int flight = 1;
        public boolean explosive = false;

        public RocketPreference() {}
        public RocketPreference(int flight, boolean explosive) {
            this.flight = flight;
            this.explosive = explosive;
        }
    }
}