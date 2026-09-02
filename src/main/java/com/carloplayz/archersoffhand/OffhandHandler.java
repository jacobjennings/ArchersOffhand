package com.carloplayz.archersoffhand;

import com.carloplayz.archersoffhand.config.ArchersOffhandConfig;
import com.carloplayz.archersoffhand.config.ConfigManager;
import java.util.Optional;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;

/** Crossbow-selection state machine with conservative, reversible inventory transactions. */
public final class OffhandHandler {
    private static final InventoryManager INVENTORY = new InventoryManager();
    private static Session session;
    private static LocalPlayer trackedPlayer;
    private static ClientLevel trackedLevel;
    private static boolean selectionHandled;
    private static int equipTicks;
    private static int restoreTicks;
    private static int clickCooldown;

    private OffhandHandler() {
    }

    public static void tick(Minecraft minecraft) {
        LocalPlayer player = minecraft.player;
        if (player == null || minecraft.level == null) {
            resetLocalState();
            return;
        }
        if (player != trackedPlayer || minecraft.level != trackedLevel) {
            resetLocalState();
            trackedPlayer = player;
            trackedLevel = minecraft.level;
        }

        ArchersOffhandConfig config = ConfigManager.CONFIG;
        if (clickCooldown > 0) {
            clickCooldown--;
        }

        boolean holdingCrossbow = player.getMainHandItem().getItem() instanceof CrossbowItem;
        if (!config.enabled || !holdingCrossbow
                || (config.restoreOnLowHealth && player.getHealth() <= (float) config.lowHealthThreshold)) {
            equipTicks = 0;
            selectionHandled = false;
            if (session != null) {
                restoreTicks++;
                if (restoreTicks >= config.restoreDelayTicks && clickCooldown == 0) {
                    tryRestore(minecraft, player, config);
                }
            } else {
                restoreTicks = 0;
            }
            return;
        }

        restoreTicks = 0;
        if (minecraft.screen != null) {
            return;
        }
        if (session != null) {
            maintainManagedAmmo(minecraft, player, config);
            return;
        }
        if (selectionHandled) {
            return;
        }
        equipTicks++;
        if (equipTicks >= config.equipDelayTicks && clickCooldown == 0) {
            selectionHandled = tryEquip(minecraft, player, config);
        }
    }

    private static boolean tryEquip(Minecraft minecraft, LocalPlayer player, ArchersOffhandConfig config) {
        Optional<AmmoFinder.Match> candidate = AmmoFinder.findBest(
                player.getInventory(), config, minecraft.level.registryAccess(), -1);
        if (candidate.isEmpty()) {
            debug(player, config, "No projectile matched the configured preferences");
            return true;
        }

        ItemStack originalOffhand = player.getOffhandItem().copy();
        int offhandRank = ProjectileMatcher.rank(originalOffhand, config, minecraft.level.registryAccess());
        if (offhandRank <= candidate.get().rank()) {
            debug(player, config, "The offhand already contains the highest-priority available projectile");
            return true;
        }
        if (!originalOffhand.isEmpty() && !config.replaceOccupiedOffhand) {
            debug(player, config, "Offhand replacement is disabled");
            return true;
        }
        if (!INVENTORY.swapInventorySlotWithOffhand(minecraft, player, candidate.get().inventorySlot())) {
            return false;
        }

        session = new Session(
                originalOffhand,
                candidate.get().inventorySlot(),
                candidate.get().stack().copyWithCount(1));
        clickCooldown = config.clickCooldownTicks;
        debug(player, config, "Equipped " + candidate.get().stack().getHoverName().getString());
        return true;
    }

    private static void maintainManagedAmmo(Minecraft minecraft, LocalPlayer player, ArchersOffhandConfig config) {
        if (clickCooldown > 0) {
            return;
        }
        ItemStack offhand = player.getOffhandItem();
        if (!offhand.isEmpty() && !ItemStack.isSameItemSameComponents(offhand, session.managedAmmo())) {
            debug(player, config, "Offhand changed manually; leaving it untouched");
            session = null;
            selectionHandled = true;
            return;
        }

        Optional<AmmoFinder.Match> candidate = AmmoFinder.findBest(
                player.getInventory(), config, minecraft.level.registryAccess(), session.restoreSlot());
        if (candidate.isEmpty()) {
            return;
        }
        int currentRank = ProjectileMatcher.rank(offhand, config, minecraft.level.registryAccess());
        if (!offhand.isEmpty() && currentRank <= candidate.get().rank()) {
            return;
        }
        if (INVENTORY.swapInventorySlotWithOffhand(minecraft, player, candidate.get().inventorySlot())) {
            session = session.withManagedAmmo(candidate.get().stack().copyWithCount(1));
            clickCooldown = config.clickCooldownTicks;
            debug(player, config, "Loaded " + candidate.get().stack().getHoverName().getString());
        }
    }

    private static void tryRestore(Minecraft minecraft, LocalPlayer player, ArchersOffhandConfig config) {
        ItemStack offhand = player.getOffhandItem();
        if (!offhand.isEmpty() && !ItemStack.isSameItemSameComponents(offhand, session.managedAmmo())) {
            debug(player, config, "Offhand changed manually; original item remains in the inventory");
            session = null;
            restoreTicks = 0;
            return;
        }

        int restoreSlot = findOriginalItem(player.getInventory(), session.originalOffhand(), session.restoreSlot());
        if (restoreSlot < 0) {
            player.sendOverlayMessage(Component.translatable("message.archersoffhand.restore_blocked"));
            debug(player, config, "Could not find a safe slot for restoring the original offhand");
            return;
        }
        if (INVENTORY.swapInventorySlotWithOffhand(minecraft, player, restoreSlot)) {
            debug(player, config, "Restored the original offhand");
            session = null;
            restoreTicks = 0;
            clickCooldown = config.clickCooldownTicks;
        }
    }

    static int findOriginalItem(Inventory inventory, ItemStack original, int preferredSlot) {
        if (slotMatches(inventory, preferredSlot, original)) {
            return preferredSlot;
        }
        for (int slot = 0; slot < Inventory.INVENTORY_SIZE; slot++) {
            if (slotMatches(inventory, slot, original)) {
                return slot;
            }
        }
        return -1;
    }

    private static boolean slotMatches(Inventory inventory, int slot, ItemStack original) {
        if (slot < 0 || slot >= Inventory.INVENTORY_SIZE) {
            return false;
        }
        ItemStack candidate = inventory.getItem(slot);
        return original.isEmpty() ? candidate.isEmpty() : ItemStack.matches(candidate, original);
    }

    private static void debug(LocalPlayer player, ArchersOffhandConfig config, String message) {
        if (config.debugLogging) {
            ArchersOffhand.LOGGER.info(message);
            player.sendSystemMessage(Component.literal("[Archer's Offhand] " + message));
        }
    }

    private static void resetLocalState() {
        session = null;
        trackedPlayer = null;
        trackedLevel = null;
        selectionHandled = false;
        equipTicks = 0;
        restoreTicks = 0;
        clickCooldown = 0;
    }

    private record Session(ItemStack originalOffhand, int restoreSlot, ItemStack managedAmmo) {
        Session withManagedAmmo(ItemStack value) {
            return new Session(originalOffhand, restoreSlot, value);
        }
    }
}
