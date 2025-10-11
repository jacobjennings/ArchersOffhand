package com.carloplayz.archersoffhand;

import com.carloplayz.archersoffhand.config.ArchersOffhandConfig;
import com.carloplayz.archersoffhand.config.ConfigManager;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;

import java.util.ArrayList;
import java.util.List;

public class OffhandHandler {
    private static boolean hasBowOrCrossbow = false;
    private static boolean healthRestoredRecently = false; // Flag to prevent immediate reactivation after health-based restoration
    private static int actionDelayCounter = 0;
    private static final InventoryManager inventoryManager = new InventoryManager();
    
    // Variables for tracking offhand stack size in shuffle mode
    private static int previousOffhandCount = 0;
    private static boolean offhandSetForShuffle = false; // Track if we've set the initial offhand in shuffle mode
    private static int shuffleOriginalOffhandSlot = -1; // Track the original offhand slot for restoration in shuffle mode
    
    // Serial handler instance
    private static final SerialHandler serialHandler = new SerialHandler();

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
    // Static method to allow SerialHandler to access the original item slot
    protected static int getOriginalItemSlotStatic() {
        return inventoryManager.getOriginalItemSlot();
    }

    // Static method to allow SerialHandler to access the original item slot
    protected static void moveArrowsToOffhandNoUpdate(MinecraftClient client, PlayerEntity player, ArchersOffhandConfig cfg) {
        moveArrowsToOffhandInternal(client, player, cfg, false); // Call with updateOriginalSlot = false
    }

    // Static method to allow SerialHandler to access the original item slot
    protected static void moveAmmoToOffhandForCrossbowNoUpdate(MinecraftClient client, PlayerEntity player, ArchersOffhandConfig cfg) {
        moveAmmoToOffhandForCrossbowInternal(client, player, cfg, false); // Call with updateOriginalSlot = false
    }

    protected static void sendDebugMessage(MinecraftClient client, String message) {
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
            offhandSetForShuffle = false; // Reset shuffle tracking
            shuffleOriginalOffhandSlot = -1; // Reset shuffle original offhand slot tracking
            serialHandler.resetSerialTracking(); // Reset serial tracking
            healthRestoredRecently = true; // Set flag to prevent immediate reactivation
            return; // Return early to avoid re-activating after restoration
        }

        // Only proceed with normal operations if health-based restoration hasn't happened recently
        if (!healthRestoredRecently) {
            if (isHoldingBow || isHoldingCrossbow) {
                // If not already tracking bow/crossbow, start tracking and move ammo
                if (!hasBowOrCrossbow) {
                    hasBowOrCrossbow = true;
                    offhandSetForShuffle = false; // Reset shuffle tracking when first starting
                    shuffleOriginalOffhandSlot = -1; // Reset shuffle original slot tracking
                    serialHandler.resetSerialTracking(); // Reset serial tracking when first starting
                    
                    if (cfg.debugLogging) {
                        sendDebugMessage(client, "[" + (isHoldingCrossbow ? "Crossbow" : "Bow") + "] Started tracking, moving ammo to offhand");
                    }
                    if (isHoldingCrossbow) {
                        moveAmmoToOffhandForCrossbow(client, player, cfg);
                        // If in shuffle mode, note the initial stack count and the original offhand slot
                        if (cfg.ammoMode == ArchersOffhandConfig.AmmoSwitchMode.SHUFFLE) {
                            // Capture the original slot where the offhand item was placed
                            shuffleOriginalOffhandSlot = inventoryManager.getOriginalItemSlot();
                            ItemStack currentOffhand = player.getOffHandStack();
                            if (!currentOffhand.isEmpty()) {
                                previousOffhandCount = currentOffhand.getCount();
                                offhandSetForShuffle = true;
                            }
                        }
                        // If in serial mode, note the initial stack count and the original offhand slot
                        else if (cfg.ammoMode == ArchersOffhandConfig.AmmoSwitchMode.SERIAL) {
                            serialHandler.initializeSerialTracking(client, player, cfg, true, true);
                        }
                    } else {
                        moveArrowsToOffhand(client, player, cfg);
                        // If in shuffle mode, note the initial stack count and the original offhand slot
                        if (cfg.ammoMode == ArchersOffhandConfig.AmmoSwitchMode.SHUFFLE) {
                            // Capture the original slot where the offhand item was placed
                            shuffleOriginalOffhandSlot = inventoryManager.getOriginalItemSlot();
                            ItemStack currentOffhand = player.getOffHandStack();
                            if (!currentOffhand.isEmpty()) {
                                previousOffhandCount = currentOffhand.getCount();
                                offhandSetForShuffle = true;
                            }
                        }
                        // If in serial mode, note the initial stack count and the original offhand slot
                        else if (cfg.ammoMode == ArchersOffhandConfig.AmmoSwitchMode.SERIAL) {
                            serialHandler.initializeSerialTracking(client, player, cfg, false, true);
                        }
                    }
                } else {
                    // Check if we're in shuffle mode and should reshuffle due to ammo consumption
                    if (cfg.ammoMode == ArchersOffhandConfig.AmmoSwitchMode.SHUFFLE && offhandSetForShuffle) {
                        ItemStack currentOffhand = player.getOffHandStack();
                        if (!currentOffhand.isEmpty()) {
                            int currentCount = currentOffhand.getCount();
                            // If the stack count has decreased, reshuffle
                            if (currentCount < previousOffhandCount) {
                                if (cfg.debugLogging) {
                                    sendDebugMessage(client, "[Shuffle] Ammo count decreased (" + previousOffhandCount + " -> " + currentCount + "), reshuffling ammo");
                                }
                                // Perform reshuffle without updating the original item slot
                                if (isHoldingCrossbow) {
                                    moveAmmoToOffhandForCrossbowNoUpdate(client, player, cfg);
                                } else {
                                    moveArrowsToOffhandNoUpdate(client, player, cfg);
                                }
                                // Update the count to the new stack count
                                previousOffhandCount = player.getOffHandStack().getCount();
                            } else if (currentCount > previousOffhandCount) {
                                // If the count increased, it might be due to player restocking, so update our tracking
                                previousOffhandCount = currentCount;
                            }
                        } else {
                            // If the offhand is now empty, reset the shuffle tracking
                            offhandSetForShuffle = false;
                        }
                    }
                    // Check if we're in serial mode and should re-serial due to ammo consumption
                    else if (cfg.ammoMode == ArchersOffhandConfig.AmmoSwitchMode.SERIAL && serialHandler.isTrackingSerial()) {
                        serialHandler.handleAmmoConsumption(client, player, cfg, isHoldingCrossbow);
                    }
                }
            } else {
                // If was holding bow/crossbow but not anymore, restore original items
                if (hasBowOrCrossbow) {
                    if (cfg.debugLogging) {
                        sendDebugMessage(client, "[Weapon Switch] No longer holding bow/crossbow, restoring original items");
                    }
                    hasBowOrCrossbow = false;
                    offhandSetForShuffle = false; // Reset shuffle tracking
                    shuffleOriginalOffhandSlot = -1; // Reset shuffle original offhand slot tracking
                    serialHandler.resetSerialTracking(); // Reset serial tracking
                    restoreOriginalItems(client, player, cfg);
                }
            }
        } else if (cfg.debugLogging && (isHoldingBow || isHoldingCrossbow)) {
            // Log when we're skipping ammo management due to recent health restoration
            sendDebugMessage(client, "[" + (isHoldingCrossbow ? "Crossbow" : "Bow") + "] Holding weapon but health-based cooldown active, skipping ammo management");
        }
    }

    private static void moveArrowsToOffhand(MinecraftClient client, PlayerEntity player, ArchersOffhandConfig cfg) {
        moveArrowsToOffhandInternal(client, player, cfg, true); // Call with updateOriginalSlot = true
    }
    

    
    private static void moveArrowsToOffhandInternal(MinecraftClient client, PlayerEntity player, ArchersOffhandConfig cfg, boolean updateOriginalSlot) {
        ItemStack currentOffhand = player.getOffHandStack();
        
        // Check shield/totem protection
        if (!currentOffhand.isEmpty() && ConfigHandler.shouldProtectShieldTotem(cfg, currentOffhand)) {
            if (cfg.debugLogging) {
                sendDebugMessage(client, "[Bow] Shield/Totem protection: not replacing " + currentOffhand.getItem().getName().getString() + " in offhand");
            }
            return; // Don't move anything if shield/totem protection is active
        }

        int arrowSlot = -1;
        
        // Use appropriate method based on ammo mode
        switch (cfg.ammoMode) {
            case SHUFFLE:
                // Find random special arrow (tipped or spectral) for bow in shuffle mode
                arrowSlot = ItemFinder.findRandomSpecialArrowSlot(player);
                break;
            case SERIAL:
                // For serial mode, select next arrow in sequence from the tracked list
                List<Integer> currentSerialList = serialHandler.getSerialAmmoList();
                int currentIdx = serialHandler.getCurrentSerialIndex();
                
                arrowSlot = ItemFinder.findSerialSpecialArrowSlot(player, currentSerialList, currentIdx);
                
                // If we couldn't find ammo from the stored list (maybe inventory changed), refresh the list and try again
                if (arrowSlot == -1) {
                    List<Integer> refreshedList = ItemFinder.findAllArrowSlots(player); // Refresh the list with current available arrows
                    serialHandler.setSerialAmmoList(refreshedList); // Update serial handler with refreshed list
                    if (!refreshedList.isEmpty()) {
                        // Try again with the refreshed list
                        arrowSlot = ItemFinder.findSerialSpecialArrowSlot(player, refreshedList, currentIdx);
                    }
                }
                
                if (arrowSlot != -1) {
                    // Always increment the serial index when we find and move an item, regardless of updateOriginalSlot
                    int newIndex = currentIdx + 1;
                    // If we've gone past the end of the list, loop back to the beginning
                    if (!currentSerialList.isEmpty() && newIndex >= currentSerialList.size()) {
                        newIndex = 0;
                    }
                    serialHandler.setCurrentSerialIndex(newIndex);
                }
                break;
            case REGULAR:
            default:
                // Only use special arrows (tipped or spectral) for bows, never plain arrows
                arrowSlot = ItemFinder.findAnyArrowSlot(player);
                break;
        }
        
        if (arrowSlot != -1) {
            if (updateOriginalSlot) {
                inventoryManager.moveItemToOffhand(client, player, arrowSlot);
            } else {
                inventoryManager.moveItemToOffhandNoUpdate(client, player, arrowSlot);
            }
            
            if (cfg.debugLogging) {
                // Show detailed message with the specific arrow type found
                ItemStack arrowStack = player.getInventory().getStack(arrowSlot);
                String arrowName = arrowStack.getItem().getName().getString();
                String modeName = cfg.ammoMode.toString().toLowerCase();
                String updateType = updateOriginalSlot ? "" : " (no slot update)";
                sendDebugMessage(client, "[Bow - " + modeName + updateType + "] Moved " + arrowName + " to offhand, original item to slot " + arrowSlot);
            }
        } else {
            if (cfg.debugLogging) {
                sendDebugMessage(client, "[Bow] No arrows found in inventory");
            }
        }
    }

    private static void moveAmmoToOffhandForCrossbow(MinecraftClient client, PlayerEntity player, ArchersOffhandConfig cfg) {
        moveAmmoToOffhandForCrossbowInternal(client, player, cfg, true); // Call with updateOriginalSlot = true
    }


    
    private static void moveAmmoToOffhandForCrossbowInternal(MinecraftClient client, PlayerEntity player, ArchersOffhandConfig cfg, boolean updateOriginalSlot) {
        ItemStack currentOffhand = player.getOffHandStack();
        
        // Check shield/totem protection
        if (!currentOffhand.isEmpty() && ConfigHandler.shouldProtectShieldTotem(cfg, currentOffhand)) {
            if (cfg.debugLogging) {
                sendDebugMessage(client, "[Crossbow] Shield/Totem protection: not replacing " + currentOffhand.getItem().getName().getString() + " in offhand");
            }
            return; // Don't move anything if shield/totem protection is active
        }

        int ammoSlot = -1;
        
        // Use appropriate method based on ammo mode
        switch (cfg.ammoMode) {
            case SHUFFLE:
                ammoSlot = ConfigHandler.findRandomAmmoSlotForCrossbow(player, cfg.crossbowAmmoType);
                break;
            case SERIAL:
                // For serial mode, select next ammo in sequence from the tracked list
                List<Integer> currentSerialList = serialHandler.getSerialAmmoList();
                int currentIdx = serialHandler.getCurrentSerialIndex();
                
                ammoSlot = ConfigHandler.findSerialAmmoSlotForCrossbow(player, cfg.crossbowAmmoType, currentSerialList, currentIdx);
                
                // If we couldn't find ammo from the stored list (maybe inventory changed), refresh the list and try again
                if (ammoSlot == -1) {
                    List<Integer> refreshedList = ConfigHandler.findAllAmmoSlotsForCrossbow(player, cfg.crossbowAmmoType); // Refresh the list with current available ammo
                    serialHandler.setSerialAmmoList(refreshedList); // Update serial handler with refreshed list
                    if (!refreshedList.isEmpty()) {
                        // Try again with the refreshed list
                        ammoSlot = ConfigHandler.findSerialAmmoSlotForCrossbow(player, cfg.crossbowAmmoType, refreshedList, currentIdx);
                    }
                }
                
                if (ammoSlot != -1) {
                    // Always increment the serial index when we find and move an item, regardless of updateOriginalSlot
                    int newIndex = currentIdx + 1;
                    // If we've gone past the end of the list, loop back to the beginning
                    if (!currentSerialList.isEmpty() && newIndex >= currentSerialList.size()) {
                        newIndex = 0;
                    }
                    serialHandler.setCurrentSerialIndex(newIndex);
                }
                break;
            case REGULAR:
            default:
                // For crossbows: 
                // - ROCKETS mode: ONLY use explosive rockets (with firework stars), no plain rockets
                // - ARROWS mode: ONLY use special arrows (tipped/spectral), never plain arrows
                // - AUTO mode: explosive rockets first, then special arrows as fallback (never plain ammo)
                ammoSlot = ConfigHandler.findAmmoSlotForCrossbow(player, cfg.crossbowAmmoType);
                break;
        }
        
        if (ammoSlot != -1) {
            // Determine what type of ammo we're moving for messaging
            String itemTranslationKey = player.getInventory().getStack(ammoSlot).getItem().getTranslationKey();
            String ammoType = itemTranslationKey.contains("firework_rocket") ? "rockets" : "arrows";
            
            if (updateOriginalSlot) {
                inventoryManager.moveItemToOffhand(client, player, ammoSlot);
            } else {
                inventoryManager.moveItemToOffhandNoUpdate(client, player, ammoSlot);
            }
            
            if (cfg.debugLogging) {
                String modeName = cfg.ammoMode.toString().toLowerCase();
                String updateType = updateOriginalSlot ? "" : " (no slot update)";
                sendDebugMessage(client, "[Crossbow - " + modeName + updateType + "] Moved " + ammoType + " to offhand, original item to slot " + ammoSlot);
            }
        } else {
            if (cfg.debugLogging) {
                sendDebugMessage(client, "[Crossbow] No rockets or arrows found in inventory");
            }
        }
    }

    private static void restoreOriginalItems(MinecraftClient client, PlayerEntity player, ArchersOffhandConfig cfg) {
        if (cfg.ammoMode == ArchersOffhandConfig.AmmoSwitchMode.SHUFFLE && shuffleOriginalOffhandSlot != -1) {
            // In shuffle mode, use the specifically tracked original offhand slot
            // We need to manually restore from the specific slot
            restoreFromSpecificSlot(client, player, shuffleOriginalOffhandSlot);
            
            if (cfg.debugLogging) {
                sendDebugMessage(client, "[Restoration - Shuffle] Restored original offhand item from slot " + shuffleOriginalOffhandSlot);
            }
        } else if (cfg.ammoMode == ArchersOffhandConfig.AmmoSwitchMode.SERIAL && serialHandler.getSerialOriginalOffhandSlot() != -1) {
            // In serial mode, use the specifically tracked original offhand slot
            // We need to manually restore from the specific slot
            restoreFromSpecificSlot(client, player, serialHandler.getSerialOriginalOffhandSlot());
            
            if (cfg.debugLogging) {
                sendDebugMessage(client, "[Restoration - Serial] Restored original offhand item from slot " + serialHandler.getSerialOriginalOffhandSlot());
            }
        } else {
            // For regular mode, use the existing restoration method
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
    
    // Helper method to restore from a specific slot (for shuffle mode)
    private static void restoreFromSpecificSlot(MinecraftClient client, PlayerEntity player, int slot) {
        inventoryManager.restoreFromSpecificSlot(client, player, slot);
    }
}