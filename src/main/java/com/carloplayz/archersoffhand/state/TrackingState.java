package com.carloplayz.archersoffhand.state;

import com.carloplayz.archersoffhand.OffhandContext;
import com.carloplayz.archersoffhand.config.ArchersOffhandConfig.AmmoSwitchMode;
import com.carloplayz.archersoffhand.strategy.IAmmoStrategy;
import com.carloplayz.archersoffhand.strategy.RegularAmmoStrategy;
import com.carloplayz.archersoffhand.strategy.SerialAmmoStrategy;
import com.carloplayz.archersoffhand.strategy.ShuffleAmmoStrategy;

public class TrackingState implements IOffhandState {
    private final IAmmoStrategy ammoStrategy;
    private final boolean isHoldingCrossbow;
    private int ticksWithoutWeapon = 0;

    public TrackingState(OffhandContext context) {
        this.isHoldingCrossbow = context.isHoldingCrossbow;

        if (context.config.ammoMode == AmmoSwitchMode.SHUFFLE) {
            this.ammoStrategy = new ShuffleAmmoStrategy();
        } else if (context.config.ammoMode == AmmoSwitchMode.SERIAL) {
            this.ammoStrategy = new SerialAmmoStrategy();
        } else {
            this.ammoStrategy = new RegularAmmoStrategy();
        }

        if (context.config.debugLogging) {
            sendDebugMessage(context.client,
                    "[" + (isHoldingCrossbow ? "Crossbow" : "Bow") + "] Started tracking, moving ammo to offhand");
        }

        this.ammoStrategy.activate(context);
    }

    @Override
    public IOffhandState onTick(OffhandContext context) {
        // Check for weapon switch (restoration)
        if (!context.hasWeapon()) {
            ticksWithoutWeapon++;
            if (ticksWithoutWeapon >= context.config.getUnequipDelayTicks()) {
                if (context.config.debugLogging) {
                    sendDebugMessage(context.client,
                            "[Weapon Switch] No longer holding bow/crossbow, restoring original items");
                }
                context.inventoryManager.restoreOriginalItem(context.client, context.player);
                ammoStrategy.deactivate(context);
                return new IdleState();
            }
        } else {
            ticksWithoutWeapon = 0; // Reset if they pull it back out within the window
        }

        // Check for low health failsafe
        if (context.config.restoreOffhandOnLowHealth
                && context.player.getHealth() <= (float) context.config.lowHealthThreshold) {
            if (context.config.debugLogging) {
                sendDebugMessage(context.client, "[Health] Health dropped below threshold ("
                        + context.player.getHealth() + "/20), restoring original offhand");
            }
            context.inventoryManager.restoreOriginalItem(context.client, context.player);
            ammoStrategy.deactivate(context);
            return new CooldownState();
        }

        // Delegate continuous tracking to strategy
        ammoStrategy.onTick(context);

        return this;
    }

    private void sendDebugMessage(net.minecraft.client.MinecraftClient client, String message) {
        if (client.player == null)
            return;
        var formattedMessage = net.minecraft.text.Text.literal("[Archer's Offhand] " + message)
                .formatted(net.minecraft.util.Formatting.GRAY);
        client.player.sendMessage(formattedMessage, false);
    }
}
