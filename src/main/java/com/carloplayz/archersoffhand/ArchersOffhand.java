package com.carloplayz.archersoffhand;

import com.carloplayz.archersoffhand.config.ConfigManager;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ArchersOffhand implements ClientModInitializer {
    public static final String MOD_ID = "archersoffhand";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitializeClient() {
        LOGGER.info("[Archer's Offhand] Initializing...");
        // load config (ensure defaults exist)
        ConfigManager.init();
        // initialize keybinds
        KeyBindingManager.initialize();
        // register keybind handler
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            KeyBindingManager.onClientTick(client);
        });
        // register handler
        OffhandHandler.register();
    }
}