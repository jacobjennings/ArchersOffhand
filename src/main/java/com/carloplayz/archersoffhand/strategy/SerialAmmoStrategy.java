package com.carloplayz.archersoffhand.strategy;

import com.carloplayz.archersoffhand.AmmoFinder;
import com.carloplayz.archersoffhand.OffhandContext;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class SerialAmmoStrategy implements IAmmoStrategy {
    private int previousOffhandCount = 0;
    private boolean isTracking = false;
    private int currentSerialIndex = 0;
    private List<Integer> serialAmmoList = new ArrayList<>();

    @Override
    public void activate(OffhandContext context) {
        ItemStack currentOffhand = context.offHandStack;
        if (!currentOffhand.isEmpty() && AmmoFinder.shouldProtectShieldTotem(context.config, currentOffhand)) {
            return;
        }

        refreshList(context);
        if (!serialAmmoList.isEmpty()) {
            currentSerialIndex = 0;
        }

        int slot = findSlot(context);

        if (slot != -1) {
            context.inventoryManager.moveItemToOffhand(context.client, context.player, slot);
            currentSerialIndex++;
            if (!serialAmmoList.isEmpty() && currentSerialIndex >= serialAmmoList.size()) {
                currentSerialIndex = 0;
            }

            ItemStack newOffhand = context.player.getOffHandStack();
            if (!newOffhand.isEmpty()) {
                previousOffhandCount = newOffhand.getCount();
                isTracking = true;
            }
        }
    }

    @Override
    public void onTick(OffhandContext context) {
        if (!isTracking)
            return;

        ItemStack currentOffhand = context.player.getOffHandStack();
        if (currentOffhand.isEmpty()) {
            isTracking = false;
            return;
        }

        int currentCount = currentOffhand.getCount();
        if (currentCount < previousOffhandCount) {
            refreshList(context);

            int slot = findSlot(context);
            if (slot != -1) {
                context.inventoryManager.moveItemToOffhandNoUpdate(context.client, context.player, slot);
                currentSerialIndex++;
                if (!serialAmmoList.isEmpty() && currentSerialIndex >= serialAmmoList.size()) {
                    currentSerialIndex = 0;
                }
                previousOffhandCount = context.player.getOffHandStack().getCount();
            }
        } else if (currentCount > previousOffhandCount) {
            previousOffhandCount = currentCount;
        }
    }

    @Override
    public void deactivate(OffhandContext context) {
        isTracking = false;
        previousOffhandCount = 0;
        currentSerialIndex = 0;
        serialAmmoList.clear();
    }

    private void refreshList(OffhandContext context) {
        if (context.isHoldingCrossbow) {
            serialAmmoList = AmmoFinder.findAllAmmoSlotsForCrossbow(context.player, context.config.crossbowAmmoType,
                    context.config);
        } else {
            serialAmmoList = AmmoFinder.findAllArrowSlots(context.player, context.config);
        }
    }

    private int findSlot(OffhandContext context) {
        int slot = getNextSlot(context);

        // If we didn't find one, list might be stale, refresh and try again
        if (slot == -1) {
            refreshList(context);
            if (!serialAmmoList.isEmpty()) {
                slot = getNextSlot(context);
            }
        }
        return slot;
    }

    private int getNextSlot(OffhandContext context) {
        if (context.isHoldingCrossbow) {
            return AmmoFinder.findSerialAmmoSlotForCrossbow(context.player, context.config.crossbowAmmoType,
                    serialAmmoList, currentSerialIndex);
        } else {
            return AmmoFinder.findSerialSpecialArrowSlot(context.player, serialAmmoList, currentSerialIndex);
        }
    }
}
