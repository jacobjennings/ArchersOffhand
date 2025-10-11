package com.carloplayz.archersoffhand;

import java.util.List;
import java.util.Random;
import java.util.ArrayList;

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
    
    // Find all special arrow slots for shuffle mode
    public static List<Integer> findAllArrowSlots(PlayerEntity player) {
        var inv = player.getInventory();
        boolean shouldScanHotbar = ConfigManager.CONFIG != null ? ConfigManager.CONFIG.scanHotbar : false;
        List<Integer> arrowSlots = new ArrayList<>();
        
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
                    arrowSlots.add(i); // Add special arrow slot
                }
            }
        }
        
        return arrowSlots; // Return all special arrow slots
    }
    
    // Find a random arrow slot for shuffle mode
    public static int findRandomArrowSlot(PlayerEntity player) {
        List<Integer> arrowSlots = findAllArrowSlots(player);
        if (arrowSlots.isEmpty()) {
            return -1; // No arrows found
        }
        Random random = new Random();
        int randomIndex = random.nextInt(arrowSlots.size());
        return arrowSlots.get(randomIndex);
    }
    
    // Find next arrow slot for serial mode based on provided list and index
    public static int findSerialArrowSlot(PlayerEntity player, List<Integer> availableSlots, int currentIndex) {
        if (availableSlots.isEmpty()) {
            return -1; // No arrows found
        }
        // Use modulo to cycle through the available slots
        int index = currentIndex % availableSlots.size();
        return availableSlots.get(index);
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
    
    // Find all rocket slots for shuffle mode
    public static List<Integer> findAllRocketSlots(PlayerEntity player) {
        var inv = player.getInventory();
        boolean shouldScanHotbar = ConfigManager.CONFIG != null ? ConfigManager.CONFIG.scanHotbar : false;
        List<Integer> rocketSlots = new ArrayList<>();
        
        // If scanning hotbar, start from slot 0, otherwise start from slot 9 (skip hotbar)
        int startIndex = shouldScanHotbar ? 0 : 9;
        // Always end at 36 (main inventory + hotbar if enabled)
        int endIndex = 36; // Total inventory slots to scan (hotbar + main inventory)
        
        for (int i = startIndex; i < endIndex; i++) {
            ItemStack stack = inv.getStack(i);
            if (!stack.isEmpty() && stack.isOf(Items.FIREWORK_ROCKET)) {
                rocketSlots.add(i);
            }
        }
        
        return rocketSlots;
    }
    
    // Find a random rocket slot for shuffle mode
    public static int findRandomRocketSlot(PlayerEntity player) {
        List<Integer> rocketSlots = findAllRocketSlots(player);
        if (rocketSlots.isEmpty()) {
            return -1; // No rockets found
        }
        Random random = new Random();
        int randomIndex = random.nextInt(rocketSlots.size());
        return rocketSlots.get(randomIndex);
    }
    
    // Find next rocket slot for serial mode based on provided list and index
    public static int findSerialRocketSlot(PlayerEntity player, List<Integer> availableSlots, int currentIndex) {
        if (availableSlots.isEmpty()) {
            return -1; // No rockets found
        }
        // Use modulo to cycle through the available slots
        int index = currentIndex % availableSlots.size();
        return availableSlots.get(index);
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

    // Find all explosive rockets for shuffle mode
    public static List<Integer> findAllExplosiveRocketSlots(PlayerEntity player) {
        var inv = player.getInventory();
        boolean shouldScanHotbar = ConfigManager.CONFIG != null ? ConfigManager.CONFIG.scanHotbar : false;
        List<Integer> explosiveRocketSlots = new ArrayList<>();
        
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
                        explosiveRocketSlots.add(i); // Found a rocket with explosion data
                    }
                }
            }
        }
        return explosiveRocketSlots;
    }
    
    // Find a random explosive rocket slot for shuffle mode
    public static int findRandomExplosiveRocketSlot(PlayerEntity player) {
        List<Integer> explosiveRocketSlots = findAllExplosiveRocketSlots(player);
        if (explosiveRocketSlots.isEmpty()) {
            return -1; // No explosive rockets found
        }
        Random random = new Random();
        int randomIndex = random.nextInt(explosiveRocketSlots.size());
        return explosiveRocketSlots.get(randomIndex);
    }
    
    // Find next explosive rocket slot for serial mode based on provided list and index
    public static int findSerialExplosiveRocketSlot(PlayerEntity player, List<Integer> availableSlots, int currentIndex) {
        if (availableSlots.isEmpty()) {
            return -1; // No explosive rockets found
        }
        // Use modulo to cycle through the available slots
        int index = currentIndex % availableSlots.size();
        return availableSlots.get(index);
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
    
    // Find random special arrow slot for shuffle mode (for bows - only special arrows)
    public static int findRandomSpecialArrowSlot(PlayerEntity player) {
        List<Integer> arrowSlots = findAllArrowSlots(player);
        if (arrowSlots.isEmpty()) {
            return -1; // No arrows found
        }
        Random random = new Random();
        int randomIndex = random.nextInt(arrowSlots.size());
        return arrowSlots.get(randomIndex);
    }
    
    // Find next special arrow slot for serial mode based on provided list and index
    public static int findSerialSpecialArrowSlot(PlayerEntity player, List<Integer> availableSlots, int currentIndex) {
        if (availableSlots.isEmpty()) {
            return -1; // No arrows found
        }
        // Use modulo to cycle through the available slots
        int index = currentIndex % availableSlots.size();
        return availableSlots.get(index);
    }
}