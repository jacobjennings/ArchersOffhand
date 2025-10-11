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
    
    private static boolean isShieldOrTotem(ItemStack stack) {
        if (stack.isEmpty()) return false;
        
        return stack.isOf(Items.SHIELD) || stack.isOf(Items.TOTEM_OF_UNDYING);
    }
}