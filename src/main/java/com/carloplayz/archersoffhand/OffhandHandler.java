package com.carloplayz.archersoffhand;

import com.carloplayz.archersoffhand.config.ArchersOffhandConfig;
import com.carloplayz.archersoffhand.config.ConfigManager;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;

public class OffhandHandler {
    private static boolean hasBowOrCrossbow = false;
    private static boolean healthRestoredRecently = false; // Flag to prevent immediate reactivation after health-based restoration
    private static int actionDelayCounter = 0;
    private static final InventoryManager inventoryManager = new InventoryManager();

    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            try {
                tick(client);
            } catch (Throwable t) {
                ArchersOffhand.LOGGER.error("[System] Unexpected error in OffhandHandler tick - Please report this issue", t);
            }
        });
    }
    
    // Helper method to send debug messages to in-game chat (client-side only)
    private static void sendDebugMessage(MinecraftClient client, String message) {
        if (client.player == null) return;
        
        var formattedMessage = net.minecraft.text.Text.literal("[Archer's Offhand] " + message)
            .formatted(net.minecraft.util.Formatting.GRAY);
        client.player.sendMessage(formattedMessage, false); // false = not action bar
    }

    private static void tick(MinecraftClient client) {
        if (!client.isOnThread()) return;
        
        PlayerEntity player = client.player;
        if (player == null || player.getWorld() == null) return;

        ArchersOffhandConfig cfg = ConfigManager.CONFIG;
        if (cfg == null || !cfg.enabled) return;

        // Handle action delay
        actionDelayCounter++;
        if (actionDelayCounter < cfg.actionDelayTicks) {
            return;
        }
        actionDelayCounter = 0; // Reset counter

        ItemStack mainHandStack = player.getMainHandStack();
        boolean isHoldingBow = mainHandStack.getItem() instanceof BowItem;
        boolean isHoldingCrossbow = mainHandStack.getItem() instanceof CrossbowItem;

        // Check if we should resume normal operation after health-based restoration
        if (healthRestoredRecently && player.getHealth() > (float)cfg.lowHealthThreshold) {
            if (cfg.debugLogging) {
                sendDebugMessage(client, "[Health] Health recovered above threshold (" + player.getHealth() + " > " + (float)cfg.lowHealthThreshold + "), re-enabling ammo management");
            }
            healthRestoredRecently = false; // Reset the flag when health recovers
        }

        // Check for health-based restoration
        if (cfg.restoreOffhandOnLowHealth && hasBowOrCrossbow && !healthRestoredRecently && player.getHealth() <= (float)cfg.lowHealthThreshold) {
            if (cfg.debugLogging) {
                sendDebugMessage(client, "[Health] Health dropped below threshold (" + player.getHealth() + "/20), restoring original offhand");
            }
            restoreOriginalItems(client, player, cfg);
            hasBowOrCrossbow = false; // Reset the tracking since items are restored
            healthRestoredRecently = true; // Set flag to prevent immediate reactivation
            return; // Return early to avoid re-activating after restoration
        }

        // Only proceed with normal operations if health-based restoration hasn't happened recently
        if (!healthRestoredRecently) {
            if (isHoldingBow || isHoldingCrossbow) {
                // If not already tracking bow/crossbow, start tracking and move ammo
                if (!hasBowOrCrossbow) {
                    hasBowOrCrossbow = true;
                    if (cfg.debugLogging) {
                        sendDebugMessage(client, "[" + (isHoldingCrossbow ? "Crossbow" : "Bow") + "] Started tracking, moving ammo to offhand");
                    }
                    if (isHoldingCrossbow) {
                        moveAmmoToOffhandForCrossbow(client, player, cfg);
                    } else {
                        moveArrowsToOffhand(client, player, cfg);
                    }
                }
            } else {
                // If was holding bow/crossbow but not anymore, restore original items
                if (hasBowOrCrossbow) {
                    if (cfg.debugLogging) {
                        sendDebugMessage(client, "[Weapon Switch] No longer holding bow/crossbow, restoring original items");
                    }
                    hasBowOrCrossbow = false;
                    restoreOriginalItems(client, player, cfg);
                }
            }
        } else if (cfg.debugLogging && (isHoldingBow || isHoldingCrossbow)) {
            // Log when we're skipping ammo management due to recent health restoration
            sendDebugMessage(client, "[" + (isHoldingCrossbow ? "Crossbow" : "Bow") + "] Holding weapon but health-based cooldown active, skipping ammo management");
        }
    }

    private static void moveArrowsToOffhand(MinecraftClient client, PlayerEntity player, ArchersOffhandConfig cfg) {
        ItemStack currentOffhand = player.getOffHandStack();
        
        // Check shield/totem protection
        if (!currentOffhand.isEmpty() && ConfigHandler.shouldProtectShieldTotem(cfg, currentOffhand)) {
            if (cfg.debugLogging) {
                sendDebugMessage(client, "[Bow] Shield/Totem protection: not replacing " + currentOffhand.getItem().getName().getString() + " in offhand");
            }
            return; // Don't move anything if shield/totem protection is active
        }

        // Look for any arrow (simplified - no priority system)
        int arrowSlot = ItemFinder.findAnyArrowSlot(player);
        
        if (arrowSlot != -1) {
            inventoryManager.moveItemToOffhand(client, player, arrowSlot);
            
            if (cfg.debugLogging) {
                // Show detailed message with the specific arrow type found
                ItemStack arrowStack = player.getInventory().getStack(arrowSlot);
                String arrowName = arrowStack.getItem().getName().getString();
                sendDebugMessage(client, "[Bow] Moved " + arrowName + " to offhand, original item to slot " + arrowSlot);
            }
        } else {
            if (cfg.debugLogging) {
                sendDebugMessage(client, "[Bow] No arrows found in inventory");
            }
        }
    }

    private static void moveAmmoToOffhandForCrossbow(MinecraftClient client, PlayerEntity player, ArchersOffhandConfig cfg) {
        ItemStack currentOffhand = player.getOffHandStack();
        
        // Check shield/totem protection
        if (!currentOffhand.isEmpty() && ConfigHandler.shouldProtectShieldTotem(cfg, currentOffhand)) {
            if (cfg.debugLogging) {
                sendDebugMessage(client, "[Crossbow] Shield/Totem protection: not replacing " + currentOffhand.getItem().getName().getString() + " in offhand");
            }
            return; // Don't move anything if shield/totem protection is active
        }

        // For crossbows, first try to find rockets, then fall back to arrows
        int ammoSlot = ConfigHandler.findAmmoSlotForCrossbow(player, cfg.crossbowAmmoType);
        
        if (ammoSlot != -1) {
            // Determine what type of ammo we're moving for messaging
            String itemTranslationKey = player.getInventory().getStack(ammoSlot).getItem().getTranslationKey();
            String ammoType = itemTranslationKey.contains("firework_rocket") ? "rockets" : "arrows";
            
            inventoryManager.moveItemToOffhand(client, player, ammoSlot);
            
            if (cfg.debugLogging) {
                sendDebugMessage(client, "[Crossbow] Moved " + ammoType + " to offhand, original item to slot " + ammoSlot);
            }
        } else {
            if (cfg.debugLogging) {
                sendDebugMessage(client, "[Crossbow] No rockets or arrows found in inventory");
            }
        }
    }

    private static void restoreOriginalItems(MinecraftClient client, PlayerEntity player, ArchersOffhandConfig cfg) {
        // Capture the original slot before restoration happens
        int originalSlot = inventoryManager.getOriginalItemSlot();
        
        inventoryManager.restoreOriginalItem(client, player);
        
        if (cfg.debugLogging) {
            if (originalSlot != -1) {
                sendDebugMessage(client, "[Restoration] Restored original offhand item and ammo back to inventory slot " + originalSlot);
            } else {
                sendDebugMessage(client, "[Restoration] Restored original offhand item (no previous item to restore)");
            }
        }
    }
}