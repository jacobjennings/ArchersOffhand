package com.carloplayz.archersoffhand;

import com.carloplayz.archersoffhand.config.ArchersOffhandConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * Handles serial mode logic for the Archer's Offhand mod.
 */
public class SerialHandler {
    private int previousOffhandCount = 0;
    private boolean offhandSetForSerial = false;
    private int serialOriginalOffhandSlot = -1;
    private int currentSerialIndex = 0;
    private List<Integer> serialAmmoList = new ArrayList<>();

    public void resetSerialTracking() {
        offhandSetForSerial = false;
        serialOriginalOffhandSlot = -1;
        currentSerialIndex = 0;
        serialAmmoList.clear();
    }

    public void initializeSerialTracking(MinecraftClient client, PlayerEntity player, ArchersOffhandConfig cfg, boolean isHoldingCrossbow, boolean updateOriginalSlot) {
        // Capture the original slot where the offhand item was placed
        serialOriginalOffhandSlot = updateOriginalSlot ? 
            OffhandHandler.getOriginalItemSlotStatic() : serialOriginalOffhandSlot;
        
        ItemStack currentOffhand = player.getOffHandStack();
        if (!currentOffhand.isEmpty()) {
            previousOffhandCount = currentOffhand.getCount();
            offhandSetForSerial = true;
            
            // Update the serial ammo list based on weapon type
            if (isHoldingCrossbow) {
                serialAmmoList = ConfigHandler.findAllAmmoSlotsForCrossbow(player, cfg.crossbowAmmoType);
            } else {
                serialAmmoList = ItemFinder.findAllArrowSlots(player);
            }
            
            // If we have items and the current index is out of bounds, reset it
            if (!serialAmmoList.isEmpty() && currentSerialIndex >= serialAmmoList.size()) {
                currentSerialIndex = 0;
            } else if (serialAmmoList.isEmpty()) {
                currentSerialIndex = 0; // Reset if no items available
            }
        }
    }

    public boolean handleAmmoConsumption(MinecraftClient client, PlayerEntity player, ArchersOffhandConfig cfg, boolean isHoldingCrossbow) {
        ItemStack currentOffhand = player.getOffHandStack();
        if (!currentOffhand.isEmpty()) {
            int currentCount = currentOffhand.getCount();
            // If the stack count has decreased, move to next serial item
            if (currentCount < previousOffhandCount) {
                if (cfg.debugLogging) {
                    OffhandHandler.sendDebugMessage(client, "[Serial] Ammo count decreased (" + previousOffhandCount + " -> " + currentCount + "), moving to next serial ammo (index: " + (currentSerialIndex + 1) + ")");
                }
                // Update the serial ammo list to account for any changes in inventory
                if (isHoldingCrossbow) {
                    serialAmmoList = ConfigHandler.findAllAmmoSlotsForCrossbow(player, cfg.crossbowAmmoType);
                } else {
                    serialAmmoList = ItemFinder.findAllArrowSlots(player);
                }
                // Perform serial selection without updating the original item slot
                // This will automatically increment the index when an item is found
                if (isHoldingCrossbow) {
                    OffhandHandler.moveAmmoToOffhandForCrossbowNoUpdate(client, player, cfg);
                } else {
                    OffhandHandler.moveArrowsToOffhandNoUpdate(client, player, cfg);
                }
                // Update the count to the new stack count
                previousOffhandCount = player.getOffHandStack().getCount();
                
                return true;
            } else if (currentCount > previousOffhandCount) {
                // If the count increased, it might be due to player restocking, so update our tracking
                previousOffhandCount = currentCount;
            }
        } else {
            // If the offhand is now empty, reset the serial tracking
            offhandSetForSerial = false;
        }
        
        return false;
    }

    public boolean isTrackingSerial() {
        return offhandSetForSerial;
    }

    public List<Integer> getSerialAmmoList() {
        return new ArrayList<>(serialAmmoList);
    }

    public void setSerialAmmoList(List<Integer> list) {
        this.serialAmmoList = new ArrayList<>(list);
    }

    public int getCurrentSerialIndex() {
        return currentSerialIndex;
    }

    public void setCurrentSerialIndex(int index) {
        this.currentSerialIndex = index;
    }

    public int getSerialOriginalOffhandSlot() {
        return serialOriginalOffhandSlot;
    }

    public int getPreviousOffhandCount() {
        return previousOffhandCount;
    }

    public void setPreviousOffhandCount(int count) {
        this.previousOffhandCount = count;
    }
}