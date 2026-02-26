package com.carloplayz.archersoffhand.config;

import java.util.Random;

/**
 * POJO-style config. Will be serialized to/from JSON by ConfigManager.
 * Keep all fields public for simple Gson serialization.
 */
public class ArchersOffhandConfig {
    public boolean enabled = true;
    public int equipDelayTicks = 2;
    public int unequipDelayTicks = 2;
    public int swapDelayTicks = 2;
    public int inventoryScanDelayTicks = 4;
    public int itemMovementDelayTicks = 2;
    public double lowHealthThreshold = 6.0;
    public boolean restoreOffhandOnLowHealth = true;
    public boolean allowReplaceShieldTotem = true;

    public static enum DelayPreset {
        PERFORMANCE, SPEED, BALANCED, ADAPTIVE, CUSTOM
    }

    public AmmoSwitchMode ammoMode = AmmoSwitchMode.REGULAR;
    public CrossbowAmmoType crossbowAmmoType = CrossbowAmmoType.AUTO;

    public DelayPreset delayPreset = DelayPreset.BALANCED;

    public boolean debugLogging = false;
    public boolean scanHotbar = false; // when false, only scans main inventory (9-35)

    private static final transient Random random = new Random();

    public int getBaseEquipDelayTicks(DelayPreset preset) {
        switch (preset) {
            case PERFORMANCE:
                return 3;
            case SPEED:
                return 0;
            case BALANCED:
                return 2;
            case ADAPTIVE:
                return 2;
            case CUSTOM:
            default:
                return equipDelayTicks;
        }
    }

    public int getEquipDelayTicks() {
        int base = getBaseEquipDelayTicks(delayPreset);
        if (delayPreset == DelayPreset.ADAPTIVE)
            return Math.max(0, base + random.nextInt(7) - 3);
        return base;
    }

    public int getBaseUnequipDelayTicks(DelayPreset preset) {
        switch (preset) {
            case PERFORMANCE:
                return 3;
            case SPEED:
                return 0;
            case BALANCED:
                return 2;
            case ADAPTIVE:
                return 2;
            case CUSTOM:
            default:
                return unequipDelayTicks;
        }
    }

    public int getUnequipDelayTicks() {
        int base = getBaseUnequipDelayTicks(delayPreset);
        if (delayPreset == DelayPreset.ADAPTIVE)
            return Math.max(0, base + random.nextInt(7) - 3);
        return base;
    }

    public int getBaseSwapDelayTicks(DelayPreset preset) {
        switch (preset) {
            case PERFORMANCE:
                return 3;
            case SPEED:
                return 0;
            case BALANCED:
                return 2;
            case ADAPTIVE:
                return 2;
            case CUSTOM:
            default:
                return swapDelayTicks;
        }
    }

    public int getSwapDelayTicks() {
        int base = getBaseSwapDelayTicks(delayPreset);
        if (delayPreset == DelayPreset.ADAPTIVE)
            return Math.max(0, base + random.nextInt(7) - 3);
        return base;
    }

    public int getBaseInventoryScanDelayTicks(DelayPreset preset) {
        switch (preset) {
            case PERFORMANCE:
                return 6;
            case SPEED:
                return 2;
            case BALANCED:
                return 4;
            case ADAPTIVE:
                return 4;
            case CUSTOM:
            default:
                return inventoryScanDelayTicks;
        }
    }

    public int getInventoryScanDelayTicks() {
        int base = getBaseInventoryScanDelayTicks(delayPreset);
        if (delayPreset == DelayPreset.ADAPTIVE)
            return Math.max(0, base + random.nextInt(7) - 3);
        return base;
    }

    public int getBaseItemMovementDelayTicks(DelayPreset preset) {
        switch (preset) {
            case PERFORMANCE:
                return 3;
            case SPEED:
                return 0;
            case BALANCED:
                return 2;
            case ADAPTIVE:
                return 2;
            case CUSTOM:
            default:
                return itemMovementDelayTicks;
        }
    }

    public int getItemMovementDelayTicks() {
        int base = getBaseItemMovementDelayTicks(delayPreset);
        if (delayPreset == DelayPreset.ADAPTIVE)
            return Math.max(0, base + random.nextInt(7) - 3);
        return base;
    }

    public enum AmmoSwitchMode {
        REGULAR, SHUFFLE, SERIAL
    }

    public enum CrossbowAmmoType {
        ARROWS, ROCKETS, AUTO
    }
}
