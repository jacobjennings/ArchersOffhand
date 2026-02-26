package com.carloplayz.archersoffhand.strategy;

import com.carloplayz.archersoffhand.AmmoFinder;
import com.carloplayz.archersoffhand.OffhandContext;
import net.minecraft.item.ItemStack;

public class RegularAmmoStrategy implements IAmmoStrategy {
    private int inventoryScanCounter = 0;

    @Override
    public void activate(OffhandContext context) {
        ItemStack currentOffhand = context.offHandStack;
        if (!currentOffhand.isEmpty() && AmmoFinder.shouldProtectShieldTotem(context.config, currentOffhand)) {
            if (context.config.debugLogging) {
                sendDebugMessage(context.client,
                        "[" + (context.isHoldingCrossbow ? "Crossbow" : "Bow")
                                + "] Shield/Totem protection: not replacing "
                                + currentOffhand.getItem().getName().getString() + " in offhand");
            }
            return;
        }

        int slot = -1;
        if (context.isHoldingCrossbow) {
            slot = AmmoFinder.findAmmoSlotForCrossbow(context.player, context.config.crossbowAmmoType, context.config);
        } else {
            slot = AmmoFinder.findAnyArrowSlot(context.player, context.config);
        }

        if (slot != -1) {
            context.inventoryManager.moveItemToOffhand(context.client, context.player, slot);
            if (context.config.debugLogging) {
                String ammoType = context.player.getInventory().getStack(slot).getItem().getTranslationKey()
                        .contains("firework_rocket") ? "rockets" : "arrows";
                sendDebugMessage(context.client, "[" + (context.isHoldingCrossbow ? "Crossbow" : "Bow")
                        + " - regular] Moved " + ammoType + " to offhand, original item to slot " + slot);
            }
        } else {
            if (context.config.debugLogging) {
                sendDebugMessage(context.client,
                        "[" + (context.isHoldingCrossbow ? "Crossbow" : "Bow") + "] No valid ammo found in inventory");
            }
        }
    }

    @Override
    public void onTick(OffhandContext context) {
        if (context.player.getOffHandStack().isEmpty()) {
            inventoryScanCounter++;
            if (inventoryScanCounter >= context.config.getInventoryScanDelayTicks()) {
                activate(context);
                inventoryScanCounter = 0;
            }
        } else {
            inventoryScanCounter = 0;
        }
    }

    @Override
    public void deactivate(OffhandContext context) {
        inventoryScanCounter = 0;
    }

    private void sendDebugMessage(net.minecraft.client.MinecraftClient client, String message) {
        if (client.player == null)
            return;
        client.player.sendMessage(net.minecraft.text.Text.literal("[Archer's Offhand] " + message)
                .formatted(net.minecraft.util.Formatting.GRAY), false);
    }
}
