package com.carloplayz.archersoffhand;

import com.carloplayz.archersoffhand.config.ArchersOffhandConfig;
import com.carloplayz.archersoffhand.config.ConfigManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

public class ConfigHandler {
    
    public static boolean shouldProtectShieldTotem(ArchersOffhandConfig cfg, ItemStack offhandStack) {
        return !cfg.allowReplaceShieldTotem && isShieldOrTotem(offhandStack);
    }
    
    public static String getExpectedAmmoName(ArchersOffhandConfig.CrossbowAmmoType ammoType) {
        switch (ammoType) {
            case ROCKETS:
                return "rockets";
            case ARROWS:
                return "arrows";
            case AUTO:
            default:
                return "rockets or arrows";
        }
    }
    
    public static int findAmmoSlotForCrossbow(PlayerEntity player, ArchersOffhandConfig.CrossbowAmmoType ammoType) {
        switch (ammoType) {
            case ROCKETS:
                // ONLY look for explosive rockets (with firework stars), no fallback to plain rockets
                return ItemFinder.findRocketSlotWithExplosions(player);
            case ARROWS:
                // Look for any arrow
                return ItemFinder.findAnyArrowSlot(player);
            case AUTO:
            default:
                // Try explosive rockets first, then arrows as fallback (no plain rockets)
                int autoRocket = ItemFinder.findRocketSlotWithExplosions(player);
                if (autoRocket != -1) {
                    return autoRocket;
                } else {
                    return ItemFinder.findAnyArrowSlot(player);
                }
        }
    }
    
    // Find all ammo slots for crossbow in serial mode
    public static java.util.List<Integer> findAllAmmoSlotsForCrossbow(PlayerEntity player, ArchersOffhandConfig.CrossbowAmmoType ammoType) {
        switch (ammoType) {
            case ROCKETS:
                // ONLY look for explosive rockets (with firework stars), no plain rockets
                return ItemFinder.findAllExplosiveRocketSlots(player);
            case ARROWS:
                // Look for any special arrow (tipped, spectral), never plain arrows
                return ItemFinder.findAllArrowSlots(player);
            case AUTO:
            default:
                // Return either explosive rockets (if available) or special arrows as fallback, matching regular AUTO behavior
                java.util.List<Integer> explosiveRockets = ItemFinder.findAllExplosiveRocketSlots(player);
                if (!explosiveRockets.isEmpty()) {
                    return explosiveRockets;  // Return rockets if available
                } else {
                    return ItemFinder.findAllArrowSlots(player); // Return arrows as fallback
                }
        }
    }
    
    // Find ammo slot for crossbow in shuffle mode
    public static int findRandomAmmoSlotForCrossbow(PlayerEntity player, ArchersOffhandConfig.CrossbowAmmoType ammoType) {
        switch (ammoType) {
            case ROCKETS:
                // ONLY look for explosive rockets (with firework stars), no fallback to plain rockets
                return ItemFinder.findRandomExplosiveRocketSlot(player);
            case ARROWS:
                // Look for any special arrow (tipped, spectral), never plain arrows
                return ItemFinder.findRandomSpecialArrowSlot(player);
            case AUTO:
            default:
                // Try explosive rockets first, then special arrows as fallback (never plain arrows)
                int autoRocket = ItemFinder.findRandomExplosiveRocketSlot(player);
                if (autoRocket != -1) {
                    return autoRocket;
                } else {
                    return ItemFinder.findRandomSpecialArrowSlot(player);
                }
        }
    }
    
    // Find ammo slot for crossbow in serial mode based on provided list and index
    public static int findSerialAmmoSlotForCrossbow(PlayerEntity player, ArchersOffhandConfig.CrossbowAmmoType ammoType, java.util.List<Integer> availableSlots, int currentIndex) {
        if (availableSlots.isEmpty()) {
            return -1;
        }
        
        switch (ammoType) {
            case ROCKETS:
                // ONLY use available slots that contain explosive rockets
                return ItemFinder.findSerialExplosiveRocketSlot(player, availableSlots, currentIndex);
            case ARROWS:
                // ONLY use available slots that contain special arrows (tipped, spectral)
                return ItemFinder.findSerialSpecialArrowSlot(player, availableSlots, currentIndex);
            case AUTO:
            default:
                // For AUTO mode in serial, we need to ensure we prioritize explosive rockets first
                // before falling back to special arrows. So we return the indexed slot from the 
                // combined list that was created in findAllAmmoSlotsForCrossbow
                if (availableSlots.isEmpty()) {
                    return -1;
                }
                int slotIndex = currentIndex % availableSlots.size();
                return availableSlots.get(slotIndex);
        }
    }
    
    private static boolean isShieldOrTotem(ItemStack stack) {
        if (stack.isEmpty()) return false;
        
        return stack.isOf(Items.SHIELD) || stack.isOf(Items.TOTEM_OF_UNDYING);
    }
}