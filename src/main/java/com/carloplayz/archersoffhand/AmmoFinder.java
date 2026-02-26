package com.carloplayz.archersoffhand;

import com.carloplayz.archersoffhand.config.ArchersOffhandConfig;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.FireworksComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ArrowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class AmmoFinder {

    public static boolean shouldProtectShieldTotem(ArchersOffhandConfig cfg, ItemStack stack) {
        if (stack.isEmpty())
            return false;
        boolean isShieldOrTotem = stack.isOf(Items.SHIELD) || stack.isOf(Items.TOTEM_OF_UNDYING);
        return !cfg.allowReplaceShieldTotem && isShieldOrTotem;
    }

    // ----- ARROWS -----

    public static int findAnyArrowSlot(PlayerEntity player, ArchersOffhandConfig config) {
        var inv = player.getInventory();
        int startIndex = config.scanHotbar ? 0 : 9;
        int endIndex = 36;

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

    public static List<Integer> findAllArrowSlots(PlayerEntity player, ArchersOffhandConfig config) {
        var inv = player.getInventory();
        List<Integer> arrowSlots = new ArrayList<>();
        int startIndex = config.scanHotbar ? 0 : 9;
        int endIndex = 36;

        for (int i = startIndex; i < endIndex; i++) {
            ItemStack stack = inv.getStack(i);
            if (!stack.isEmpty() && stack.getItem() instanceof ArrowItem) {
                String itemId = Registries.ITEM.getId(stack.getItem()).toString();
                if (itemId.contains("spectral") || itemId.contains("tipped")) {
                    arrowSlots.add(i);
                }
            }
        }
        return arrowSlots;
    }

    public static int findRandomSpecialArrowSlot(PlayerEntity player, ArchersOffhandConfig config) {
        List<Integer> slots = findAllArrowSlots(player, config);
        if (slots.isEmpty())
            return -1;
        return slots.get(new Random().nextInt(slots.size()));
    }

    public static int findSerialSpecialArrowSlot(PlayerEntity player, List<Integer> availableSlots, int currentIndex) {
        if (availableSlots.isEmpty())
            return -1;
        int startingIndex = currentIndex % availableSlots.size();
        int currentIndexToCheck = startingIndex;

        do {
            int slot = availableSlots.get(currentIndexToCheck);
            ItemStack stack = player.getInventory().getStack(slot);
            if (!stack.isEmpty() && stack.getItem() instanceof ArrowItem) {
                String itemId = Registries.ITEM.getId(stack.getItem()).toString();
                if (itemId.contains("spectral") || itemId.contains("tipped"))
                    return slot;
            }
            currentIndexToCheck = (currentIndexToCheck + 1) % availableSlots.size();
        } while (currentIndexToCheck != startingIndex);

        return -1;
    }

    // ----- CROSSBOW ROCKETS & AUTO -----

    public static int findRocketSlotWithExplosions(PlayerEntity player, ArchersOffhandConfig config) {
        var inv = player.getInventory();
        int startIndex = config.scanHotbar ? 0 : 9;
        int endIndex = 36;

        for (int i = startIndex; i < endIndex; i++) {
            ItemStack stack = inv.getStack(i);
            if (!stack.isEmpty() && stack.isOf(Items.FIREWORK_ROCKET)) {
                FireworksComponent fw = stack.get(DataComponentTypes.FIREWORKS);
                if (fw != null && !fw.comp_2392().isEmpty())
                    return i;
            }
        }
        return -1;
    }

    public static List<Integer> findAllExplosiveRocketSlots(PlayerEntity player, ArchersOffhandConfig config) {
        var inv = player.getInventory();
        List<Integer> slots = new ArrayList<>();
        int startIndex = config.scanHotbar ? 0 : 9;
        int endIndex = 36;

        for (int i = startIndex; i < endIndex; i++) {
            ItemStack stack = inv.getStack(i);
            if (!stack.isEmpty() && stack.isOf(Items.FIREWORK_ROCKET)) {
                FireworksComponent fw = stack.get(DataComponentTypes.FIREWORKS);
                if (fw != null && !fw.comp_2392().isEmpty())
                    slots.add(i);
            }
        }
        return slots;
    }

    public static int findRandomExplosiveRocketSlot(PlayerEntity player, ArchersOffhandConfig config) {
        List<Integer> slots = findAllExplosiveRocketSlots(player, config);
        if (slots.isEmpty())
            return -1;
        return slots.get(new Random().nextInt(slots.size()));
    }

    public static int findSerialExplosiveRocketSlot(PlayerEntity player, List<Integer> availableSlots,
            int currentIndex) {
        if (availableSlots.isEmpty())
            return -1;
        int startingIndex = currentIndex % availableSlots.size();
        int currentIndexToCheck = startingIndex;

        do {
            int slot = availableSlots.get(currentIndexToCheck);
            ItemStack stack = player.getInventory().getStack(slot);
            if (!stack.isEmpty() && stack.isOf(Items.FIREWORK_ROCKET)) {
                FireworksComponent fw = stack.get(DataComponentTypes.FIREWORKS);
                if (fw != null && !fw.comp_2392().isEmpty())
                    return slot;
            }
            currentIndexToCheck = (currentIndexToCheck + 1) % availableSlots.size();
        } while (currentIndexToCheck != startingIndex);

        return -1;
    }

    // ----- CROSSBOW GENERAL LOGIC -----

    public static int findAmmoSlotForCrossbow(PlayerEntity player, ArchersOffhandConfig.CrossbowAmmoType ammoType,
            ArchersOffhandConfig config) {
        var a = AmmoFinder.findRocketSlotWithExplosions(player, config);
        if (ammoType == ArchersOffhandConfig.CrossbowAmmoType.ROCKETS)
            return a;
        var b = AmmoFinder.findAnyArrowSlot(player, config);
        if (ammoType == ArchersOffhandConfig.CrossbowAmmoType.ARROWS)
            return b;
        return a != -1 ? a : b;
    }

    public static List<Integer> findAllAmmoSlotsForCrossbow(PlayerEntity player,
            ArchersOffhandConfig.CrossbowAmmoType ammoType, ArchersOffhandConfig config) {
        var a = AmmoFinder.findAllExplosiveRocketSlots(player, config);
        if (ammoType == ArchersOffhandConfig.CrossbowAmmoType.ROCKETS)
            return a;
        var b = AmmoFinder.findAllArrowSlots(player, config);
        if (ammoType == ArchersOffhandConfig.CrossbowAmmoType.ARROWS)
            return b;
        return !a.isEmpty() ? a : b;
    }

    public static int findRandomAmmoSlotForCrossbow(PlayerEntity player, ArchersOffhandConfig.CrossbowAmmoType ammoType,
            ArchersOffhandConfig config) {
        var a = AmmoFinder.findRandomExplosiveRocketSlot(player, config);
        if (ammoType == ArchersOffhandConfig.CrossbowAmmoType.ROCKETS)
            return a;
        var b = AmmoFinder.findRandomSpecialArrowSlot(player, config);
        if (ammoType == ArchersOffhandConfig.CrossbowAmmoType.ARROWS)
            return b;
        return a != -1 ? a : b;
    }

    public static int findSerialAmmoSlotForCrossbow(PlayerEntity player, ArchersOffhandConfig.CrossbowAmmoType ammoType,
            List<Integer> availableSlots, int currentIndex) {
        if (availableSlots.isEmpty())
            return -1;
        if (ammoType == ArchersOffhandConfig.CrossbowAmmoType.ROCKETS)
            return findSerialExplosiveRocketSlot(player, availableSlots, currentIndex);
        if (ammoType == ArchersOffhandConfig.CrossbowAmmoType.ARROWS)
            return findSerialSpecialArrowSlot(player, availableSlots, currentIndex);
        return availableSlots.get(currentIndex % availableSlots.size());
    }
}
