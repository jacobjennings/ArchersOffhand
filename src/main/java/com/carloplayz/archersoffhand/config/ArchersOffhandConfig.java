package com.carloplayz.archersoffhand.config;

/**
 * POJO-style config. Will be serialized to/from JSON by ConfigManager.
 * Keep all fields public for simple Gson serialization.
 */
public class ArchersOffhandConfig {
    public boolean enabled = true;
    public int actionDelayTicks = 8; // default 8 ticks (0.4s)
    public double lowHealthThreshold = 6.0;
    public boolean restoreOffhandOnLowHealth = true;
    public boolean allowReplaceShieldTotem = true;

    public AmmoSwitchMode ammoMode = AmmoSwitchMode.REGULAR;
    public CrossbowAmmoType crossbowAmmoType = CrossbowAmmoType.AUTO;

    public boolean debugLogging = false;
    public boolean scanHotbar = false; // when false, only scans main inventory (9-35)

    public enum AmmoSwitchMode { REGULAR, SHUFFLE, SERIAL }
    public enum CrossbowAmmoType { ARROWS, ROCKETS, AUTO }
}
