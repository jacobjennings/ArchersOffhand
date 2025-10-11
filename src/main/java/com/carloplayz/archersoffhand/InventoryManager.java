package com.carloplayz.archersoffhand;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.SlotActionType;

public class InventoryManager {
    private int originalItemSlot = -1;

    public boolean moveItemToOffhand(MinecraftClient client, PlayerEntity player, int sourceSlot) {
        return moveItemToOffhandInternal(client, player, sourceSlot, true);
    }
    
    // Move item to offhand without updating the originalItemSlot (for shuffle mode)
    public boolean moveItemToOffhandNoUpdate(MinecraftClient client, PlayerEntity player, int sourceSlot) {
        return moveItemToOffhandInternal(client, player, sourceSlot, false);
    }
    
    // Internal method to move item to offhand with option to update original slot
    private boolean moveItemToOffhandInternal(MinecraftClient client, PlayerEntity player, int sourceSlot, boolean updateOriginalSlot) {
        if (client.interactionManager == null) return false;

        // Convert inventory slot index to container slot index
        // Inventory slots: 0-8=hotbar, 9-35=main inventory
        // Container slots: 36-44=hotbar, 9-35=main inventory, 45=offhand
        int containerSlot = getContainerSlotFromInventoryIndex(sourceSlot);
        
        // First, pick up the item from inventory
        client.interactionManager.clickSlot(
            player.currentScreenHandler.syncId,
            containerSlot,
            0,
            SlotActionType.PICKUP,
            player
        );
        
        // Then place it in offhand (slot 45 in player container)
        client.interactionManager.clickSlot(
            player.currentScreenHandler.syncId,
            45, // offhand slot in container
            0,
            SlotActionType.PICKUP,
            player
        );
        
        // At this point, if there was an original offhand item, it's now on the cursor
        // Place it back in the source slot (now empty)
        if (!player.currentScreenHandler.getCursorStack().isEmpty()) {
            client.interactionManager.clickSlot(
                player.currentScreenHandler.syncId,
                containerSlot,
                0,
                SlotActionType.PICKUP,
                player
            );
        }
        
        if (updateOriginalSlot) {
            originalItemSlot = sourceSlot; // Store the original inventory index
        }
        return true;
    }
    
    // Convert inventory index to container slot number
    // In PlayerInventory: 0-8 = hotbar, 9-35 = main inventory
    // In Container: 36-44 = hotbar (corresponds to inventory 0-8), 9-35 = main inventory (same), 45 = offhand
    private int getContainerSlotFromInventoryIndex(int inventoryIndex) {
        if (inventoryIndex >= 0 && inventoryIndex <= 8) {
            // Hotbar slots: inventory index 0-8 maps to container slots 36-44
            return 36 + inventoryIndex;
        } else if (inventoryIndex >= 9 && inventoryIndex <= 35) {
            // Main inventory slots: same in both systems (9-35)
            return inventoryIndex;
        } else {
            // This shouldn't happen for normal inventory scanning
            return inventoryIndex;
        }
    }

    public boolean restoreOriginalItem(MinecraftClient client, PlayerEntity player) {
        if (client.interactionManager == null || originalItemSlot == -1) return false;

        // Pick up the current offhand item (should be ammo)
        client.interactionManager.clickSlot(
            player.currentScreenHandler.syncId,
            45, // offhand slot
            0,
            SlotActionType.PICKUP,
            player
        );
        
        // Convert the stored inventory index to container slot
        int containerSlot = getContainerSlotFromInventoryIndex(originalItemSlot);
        
        // Place it back in the original slot (where original item should be)
        client.interactionManager.clickSlot(
            player.currentScreenHandler.syncId,
            containerSlot,
            0,
            SlotActionType.PICKUP,
            player
        );
        
        // Now the original item should be on cursor, place it back in offhand
        if (!player.currentScreenHandler.getCursorStack().isEmpty()) {
            client.interactionManager.clickSlot(
                player.currentScreenHandler.syncId,
                45, // offhand slot
                0,
                SlotActionType.PICKUP,
                player
            );
        }
        
        // Clear any remaining cursor stack to be safe
        if (!player.currentScreenHandler.getCursorStack().isEmpty()) {
            client.interactionManager.clickSlot(
                player.currentScreenHandler.syncId,
                -999, // Outside inventory
                0,
                SlotActionType.PICKUP,
                player
            );
        }
        
        originalItemSlot = -1; // Reset tracking
        return true;
    }

    public int getOriginalItemSlot() {
        return originalItemSlot;
    }

    public void setOriginalItemSlot(int slot) {
        this.originalItemSlot = slot;
    }
    
    // Restore from a specific slot (for shuffle mode)
    public boolean restoreFromSpecificSlot(MinecraftClient client, PlayerEntity player, int slot) {
        if (client.interactionManager == null || slot == -1) return false;

        // Pick up the current offhand item (should be ammo)
        client.interactionManager.clickSlot(
            player.currentScreenHandler.syncId,
            45, // offhand slot
            0,
            SlotActionType.PICKUP,
            player
        );
        
        // Convert the specified inventory index to container slot
        int containerSlot = getContainerSlotFromInventoryIndex(slot);
        
        // Place it back in the specified slot (where original item should be)
        client.interactionManager.clickSlot(
            player.currentScreenHandler.syncId,
            containerSlot,
            0,
            SlotActionType.PICKUP,
            player
        );
        
        // Now the original item should be on cursor, place it back in offhand
        if (!player.currentScreenHandler.getCursorStack().isEmpty()) {
            client.interactionManager.clickSlot(
                player.currentScreenHandler.syncId,
                45, // offhand slot
                0,
                SlotActionType.PICKUP,
                player
            );
        }
        
        // Clear any remaining cursor stack to be safe
        if (!player.currentScreenHandler.getCursorStack().isEmpty()) {
            client.interactionManager.clickSlot(
                player.currentScreenHandler.syncId,
                -999, // Outside inventory
                0,
                SlotActionType.PICKUP,
                player
            );
        }
        
        return true;
    }
}