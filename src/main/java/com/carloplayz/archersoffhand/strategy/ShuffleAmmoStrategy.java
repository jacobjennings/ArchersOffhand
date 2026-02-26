package com.carloplayz.archersoffhand.strategy;

import com.carloplayz.archersoffhand.AmmoFinder;
import com.carloplayz.archersoffhand.OffhandContext;
import net.minecraft.item.ItemStack;

public class ShuffleAmmoStrategy implements IAmmoStrategy {
    private int previousOffhandCount = 0;
    private boolean isTracking = false;
    private int swapDelayCounter = 0;
    private int inventoryScanCounter = 0;

    @Override
    public void activate(OffhandContext context) {
        ItemStack currentOffhand = context.offHandStack;
        if (!currentOffhand.isEmpty() && AmmoFinder.shouldProtectShieldTotem(context.config, currentOffhand)) {
            return;
        }

        int slot = findSlot(context);

        if (slot != -1) {
            context.inventoryManager.moveItemToOffhand(context.client, context.player, slot);
            ItemStack newOffhand = context.player.getOffHandStack();
            if (!newOffhand.isEmpty()) {
                previousOffhandCount = newOffhand.getCount();
                isTracking = true;
            }
        }
    }

    @Override
    public void onTick(OffhandContext context) {
        if (!isTracking) {
            inventoryScanCounter++;
            if (inventoryScanCounter >= context.config.inventoryScanDelayTicks) {
                activate(context);
                inventoryScanCounter = 0;
            }
            return;
        }

        ItemStack currentOffhand = context.player.getOffHandStack();
        if (currentOffhand.isEmpty()) {
            isTracking = false;
            return;
        }

        int currentCount = currentOffhand.getCount();
        if (currentCount < previousOffhandCount) {
            swapDelayCounter++;
            if (swapDelayCounter >= context.config.swapDelayTicks) {
                // Ammo consumed and delay passed, reshuffle
                int slot = findSlot(context);
                if (slot != -1) {
                    // False means don't overwrite the originalItemSlot
                    context.inventoryManager.moveItemToOffhandNoUpdate(context.client, context.player, slot);
                    previousOffhandCount = context.player.getOffHandStack().getCount();
                }
                swapDelayCounter = 0; // reset for next shot
            }
        } else if (currentCount > previousOffhandCount) {
            previousOffhandCount = currentCount;
            swapDelayCounter = 0; // reset
        } else {
            swapDelayCounter = 0; // reset if they grabbed something else and we didn't trigger
        }
    }

    @Override
    public void deactivate(OffhandContext context) {
        isTracking = false;
        previousOffhandCount = 0;
        swapDelayCounter = 0;
        inventoryScanCounter = 0;
    }

    private int findSlot(OffhandContext context) {
        if (context.isHoldingCrossbow) {
            return AmmoFinder.findRandomAmmoSlotForCrossbow(context.player, context.config.crossbowAmmoType,
                    context.config);
        } else {
            return AmmoFinder.findRandomSpecialArrowSlot(context.player, context.config);
        }
    }
}
