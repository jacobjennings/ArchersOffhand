package com.carloplayz.archersoffhand;

import java.util.List;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.FireworkExplosionComponent;
import net.minecraft.component.type.FireworksComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ArrowItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import com.carloplayz.archersoffhand.config.ConfigManager;

public class ItemFinder {

    // Find only special arrows (spectral, tipped) - never move plain arrows to offhand
    public static int findAnyArrowSlot(PlayerEntity player) {
        var inv = player.getInventory();
        boolean shouldScanHotbar = ConfigManager.CONFIG != null ? ConfigManager.CONFIG.scanHotbar : false;
        
        // If scanning hotbar, start from slot 0, otherwise start from slot 9 (skip hotbar)
        int startIndex = shouldScanHotbar ? 0 : 9;
        // Always end at 36 (main inventory + hotbar if enabled)
        int endIndex = 36; // Total inventory slots to scan (hotbar + main inventory)
        
        // Only look for special arrows (spectral, tipped) - never plain arrows
        for (int i = startIndex; i < endIndex; i++) {
            ItemStack stack = inv.getStack(i);
            if (!stack.isEmpty() && stack.getItem() instanceof ArrowItem) {
                String itemId = Registries.ITEM.getId(stack.getItem()).toString();
                if (itemId.contains("spectral") || itemId.contains("tipped")) {
                    return i; // Found a special arrow
                }
            }
        }
        
        // No special arrows found
        return -1;
    }
    
    // Simplified rocket finding - just find any rocket
    public static int findAnyRocketSlot(PlayerEntity player) {
        var inv = player.getInventory();
        boolean shouldScanHotbar = ConfigManager.CONFIG != null ? ConfigManager.CONFIG.scanHotbar : false;
        
        // If scanning hotbar, start from slot 0, otherwise start from slot 9 (skip hotbar)
        int startIndex = shouldScanHotbar ? 0 : 9;
        // Always end at 36 (main inventory + hotbar if enabled)
        int endIndex = 36; // Total inventory slots to scan (hotbar + main inventory)
        
        for (int i = startIndex; i < endIndex; i++) {
            ItemStack stack = inv.getStack(i);
            if (!stack.isEmpty() && stack.isOf(Items.FIREWORK_ROCKET)) {
                return i;
            }
        }
        
        return -1;
    }
    
    public static int findNonPlainArrowSlot(PlayerEntity player) {
        var inv = player.getInventory();
        boolean shouldScanHotbar = ConfigManager.CONFIG != null ? ConfigManager.CONFIG.scanHotbar : false;
        
        // If scanning hotbar, start from slot 0, otherwise start from slot 9 (skip hotbar)
        int startIndex = shouldScanHotbar ? 0 : 9;
        // Always end at 36 (main inventory + hotbar if enabled)
        int endIndex = 36; // Total inventory slots to scan (hotbar + main inventory)
        
        for (int i = startIndex; i < endIndex; i++) {
            ItemStack stack = inv.getStack(i);
            if (!stack.isEmpty() && stack.getItem() instanceof ArrowItem) {
                String itemId = Registries.ITEM.getId(stack.getItem()).toString();
                if (itemId.contains("spectral") || itemId.contains("tipped")) {
                    return i;
                }
            }
        }
        return -1;
    }

    public static int findRocketSlotWithExplosions(PlayerEntity player) {
        var inv = player.getInventory();
        boolean shouldScanHotbar = ConfigManager.CONFIG != null ? ConfigManager.CONFIG.scanHotbar : false;
        
        // If scanning hotbar, start from slot 0, otherwise start from slot 9 (skip hotbar)
        int startIndex = shouldScanHotbar ? 0 : 9;
        // Always end at 36 (main inventory + hotbar if enabled)
        int endIndex = 36; // Total inventory slots to scan (hotbar + main inventory)
        
        for (int i = startIndex; i < endIndex; i++) {
            ItemStack stack = inv.getStack(i);
            if (!stack.isEmpty() && stack.isOf(Items.FIREWORK_ROCKET)) {
                FireworksComponent fw = stack.get(DataComponentTypes.FIREWORKS);
                if (fw != null) {
                    // Using obfuscated method name - this is the explosions list accessor
                    List<net.minecraft.component.type.FireworkExplosionComponent> explosionList = fw.explosions();
                    if (!explosionList.isEmpty()) {
                        return i; // Found a rocket with explosion data
                    }
                }
            }
        }
        return -1;
    }

    public static int findItemSlot(PlayerEntity player, Item item) {
        var inv = player.getInventory();
        boolean shouldScanHotbar = ConfigManager.CONFIG != null ? ConfigManager.CONFIG.scanHotbar : false;
        
        // If scanning hotbar, start from slot 0, otherwise start from slot 9 (skip hotbar)
        int startIndex = shouldScanHotbar ? 0 : 9;
        // Always end at 36 (main inventory + hotbar if enabled)
        int endIndex = 36; // Total inventory slots to scan (hotbar + main inventory)
        
        for (int i = startIndex; i < endIndex; i++) {
            ItemStack stack = inv.getStack(i);
            if (!stack.isEmpty() && stack.isOf(item)) {
                return i;
            }
        }
        return -1;
    }
    
    // Keep the priority method for crossbow compatibility if needed
    public static int findArrowSlotByPriority(PlayerEntity player, List<com.carloplayz.archersoffhand.config.ArrowPreference> arrowPriority) {
        var inv = player.getInventory();
        boolean shouldScanHotbar = ConfigManager.CONFIG != null ? ConfigManager.CONFIG.scanHotbar : false;
        
        // If scanning hotbar, start from slot 0, otherwise start from slot 9 (skip hotbar)
        int startIndex = shouldScanHotbar ? 0 : 9;
        // Always end at 36 (main inventory + hotbar if enabled)
        int endIndex = 36; // Total inventory slots to scan (hotbar + main inventory)
        
        // Iterate through each priority level
        for (com.carloplayz.archersoffhand.config.ArrowPreference priority : arrowPriority) {
            // Look for this specific arrow type in inventory
            for (int i = startIndex; i < endIndex; i++) {
                ItemStack stack = inv.getStack(i);
                if (!stack.isEmpty() && stack.getItem() instanceof ArrowItem) {
                    String itemId = Registries.ITEM.getId(stack.getItem()).toString();
                    
                    // Check if this matches the priority item
                    if (itemId.equals(priority.itemId)) {
                        return i;
                    }
                }
            }
        }
        
        // If no priority arrow found, look for any arrow
        for (int i = startIndex; i < endIndex; i++) {
            ItemStack stack = inv.getStack(i);
            if (!stack.isEmpty() && stack.getItem() instanceof ArrowItem) {
                return i;
            }
        }
        
        return -1;
    }
}