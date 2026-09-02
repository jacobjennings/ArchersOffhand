package com.carloplayz.archersoffhand;

import com.mojang.blaze3d.platform.InputConstants;
import com.carloplayz.archersoffhand.config.ArchersOffhandConfigScreen;
import com.carloplayz.archersoffhand.config.ConfigManager;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

/** Client key mappings and their end-of-tick actions. */
public final class KeyBindingManager {
    private static final KeyMapping.Category CATEGORY = KeyMapping.Category.register(
            Identifier.fromNamespaceAndPath(ArchersOffhand.MOD_ID, "keys"));

    private static KeyMapping toggleEnabled;
    private static KeyMapping cyclePrimaryPreference;
    private static KeyMapping openConfigMenu;

    private KeyBindingManager() {
    }

    public static void initialize() {
        // The gameplay actions are intentionally unbound by default. Players can
        // choose keys in Minecraft's Controls screen without stealing defaults.
        toggleEnabled = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.archersoffhand.toggle_enabled",
                InputConstants.Type.KEYSYM,
                InputConstants.UNKNOWN.getValue(),
                CATEGORY));

        cyclePrimaryPreference = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.archersoffhand.cycle_primary_preference",
                InputConstants.Type.KEYSYM,
                InputConstants.UNKNOWN.getValue(),
                CATEGORY));

        // Preserve the existing convenient O shortcut for the configuration UI.
        openConfigMenu = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.archersoffhand.open_config",
                InputConstants.Type.KEYSYM,
                InputConstants.KEY_O,
                CATEGORY));
    }

    public static void onClientTick(Minecraft client) {
        while (toggleEnabled.consumeClick()) {
            toggleEnabled(client);
        }

        while (cyclePrimaryPreference.consumeClick()) {
            // ConfigManager owns the configured preference order and its save /
            // feedback behavior. Keep this key binding as a thin delegate.
            ConfigManager.cyclePrimaryPreference(client);
        }

        while (openConfigMenu.consumeClick()) {
            openConfigMenu(client);
        }
    }

    private static void toggleEnabled(Minecraft client) {
        if (ConfigManager.CONFIG == null) {
            return;
        }

        ConfigManager.CONFIG.enabled = !ConfigManager.CONFIG.enabled;
        ConfigManager.save();

        if (client.player != null) {
            String messageKey = ConfigManager.CONFIG.enabled
                    ? "message.archersoffhand.enabled"
                    : "message.archersoffhand.disabled";
            client.player.sendOverlayMessage(Component.translatable(messageKey));
        }
    }

    private static void openConfigMenu(Minecraft client) {
        if (client.screen != null) {
            return;
        }

        Screen parent = client.screen;
        client.setScreen(ArchersOffhandConfigScreen.create(parent));
    }
}
