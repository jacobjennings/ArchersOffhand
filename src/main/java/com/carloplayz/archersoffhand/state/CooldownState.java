package com.carloplayz.archersoffhand.state;

import com.carloplayz.archersoffhand.OffhandContext;

public class CooldownState implements IOffhandState {
    @Override
    public IOffhandState onTick(OffhandContext context) {
        // Wait until health recovers above the threshold before returning to normal
        // operations
        if (context.player.getHealth() > (float) context.config.lowHealthThreshold) {
            if (context.config.debugLogging) {
                sendDebugMessage(context.client,
                        "[Health] Health recovered above threshold (" + context.player.getHealth() + " > "
                                + (float) context.config.lowHealthThreshold + "), re-enabling ammo management");
            }
            // Return to IdleState, which will immediately transition to TrackingState next
            // tick if a weapon is held
            return new IdleState();
        }

        if (context.config.debugLogging && context.hasWeapon()) {
            sendDebugMessage(context.client, "[" + (context.isHoldingCrossbow ? "Crossbow" : "Bow")
                    + "] Holding weapon but health-based cooldown active, skipping ammo management");
        }

        return this; // Stay in cooldown
    }

    private void sendDebugMessage(net.minecraft.client.MinecraftClient client, String message) {
        if (client.player == null)
            return;
        var formattedMessage = net.minecraft.text.Text.literal("[Archer's Offhand] " + message)
                .formatted(net.minecraft.util.Formatting.GRAY);
        client.player.sendMessage(formattedMessage, false);
    }
}
