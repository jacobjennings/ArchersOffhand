package com.carloplayz.archersoffhand;

import com.carloplayz.archersoffhand.config.ArchersOffhandConfig;
import com.carloplayz.archersoffhand.config.ConfigManager;
import com.carloplayz.archersoffhand.state.IOffhandState;
import com.carloplayz.archersoffhand.state.IdleState;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;

public class OffhandHandler {
    private static IOffhandState currentState = new IdleState();
    private static final InventoryManager inventoryManager = new InventoryManager();

    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            try {
                tick(client);
            } catch (Throwable t) {
                ArchersOffhand.LOGGER
                        .error("[System] Unexpected error in OffhandHandler tick - Please report this issue", t);
            }
        });
    }

    private static void tick(MinecraftClient client) {
        if (!client.isOnThread())
            return;

        PlayerEntity player = client.player;
        if (player == null || client.world == null)
            return;

        ArchersOffhandConfig cfg = ConfigManager.CONFIG;
        if (cfg == null || !cfg.enabled)
            return;

        OffhandContext context = new OffhandContext(client, player, cfg, inventoryManager);
        currentState = currentState.onTick(context);
    }
}
