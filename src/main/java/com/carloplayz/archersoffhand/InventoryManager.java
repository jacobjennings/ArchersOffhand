package com.carloplayz.archersoffhand;

import java.util.OptionalInt;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/** Sends one vanilla SWAP click; no cursor-held intermediate state is created. */
public final class InventoryManager {
    public boolean swapInventorySlotWithOffhand(Minecraft minecraft, LocalPlayer player, int inventorySlot) {
        if (minecraft.gameMode == null || minecraft.gui.screen() != null
                || inventorySlot < 0 || inventorySlot >= Inventory.INVENTORY_SIZE) {
            return false;
        }
        AbstractContainerMenu menu = player.containerMenu;
        if (menu != player.inventoryMenu || menu.containerId != InventoryMenu.CONTAINER_ID
                || !menu.getCarried().isEmpty()) {
            return false;
        }
        OptionalInt menuSlot = findMenuSlot(menu, player.getInventory(), inventorySlot);
        if (menuSlot.isEmpty()) {
            ArchersOffhand.LOGGER.debug("Inventory slot {} is not present in the active menu", inventorySlot);
            return false;
        }
        ItemStack sourceBefore = player.getInventory().getItem(inventorySlot).copy();
        ItemStack offhandBefore = player.getOffhandItem().copy();
        minecraft.gameMode.handleContainerInput(
                menu.containerId,
                menuSlot.getAsInt(),
                Inventory.SLOT_OFFHAND,
                ContainerInput.SWAP,
                player);

        // handleContainerInput applies the click locally before sending it. Only claim
        // ownership when the predicted result is exactly the atomic swap we requested.
        return ItemStack.matches(player.getOffhandItem(), sourceBefore)
                && ItemStack.matches(player.getInventory().getItem(inventorySlot), offhandBefore);
    }

    private OptionalInt findMenuSlot(AbstractContainerMenu menu, Inventory inventory, int inventorySlot) {
        for (Slot slot : menu.slots) {
            if (slot.container == inventory && slot.getContainerSlot() == inventorySlot) {
                return OptionalInt.of(slot.index);
            }
        }
        return OptionalInt.empty();
    }
}
