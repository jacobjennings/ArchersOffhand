package com.carloplayz.archersoffhand;

import com.carloplayz.archersoffhand.config.ArchersOffhandConfig;
import com.carloplayz.archersoffhand.config.ConfigManager;
import com.carloplayz.archersoffhand.config.ArchersOffhandConfig.CrossbowAmmoType;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

public class KeyBindingManager {
    private static KeyBinding toggleCrossbowAmmo;
    private static KeyBinding toggleAmmoMode;
    private static KeyBinding openConfigMenu;

    public static void initialize() {
        KeyBinding.Category category = KeyBinding.Category.create(Identifier.of(ArchersOffhand.MOD_ID, "archersoffhand"));

        // Keybind to toggle between arrows/rockets/auto for crossbows
        toggleCrossbowAmmo = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.archersoffhand.toggle_crossbow_ammo",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_C,
                category
        ));

        // Keybind to toggle between ammo modes (REGULAR, SHUFFLE, SERIAL)
        toggleAmmoMode = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.archersoffhand.toggle_ammo_mode",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_V,
                category
        ));

        // Keybind to open config menu
        openConfigMenu = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.archersoffhand.open_config",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_O,
                category
        ));
    }

    public static void onClientTick(MinecraftClient client) {
        while (toggleCrossbowAmmo.wasPressed()) {
            toggleCrossbowAmmoType(client);
        }

        while (toggleAmmoMode.wasPressed()) {
            toggleAmmoMode(client);
        }

        while (openConfigMenu.wasPressed()) {
            openConfigMenu(client);
        }
    }

    private static void toggleCrossbowAmmoType(MinecraftClient client) {
        if (client.player == null) return;

        ArchersOffhandConfig config = ConfigManager.CONFIG;
        if (config == null) return;

        // Cycle through ammo types: ARROWS -> ROCKETS -> AUTO -> ARROWS...
        CrossbowAmmoType current = config.crossbowAmmoType;
        CrossbowAmmoType next;
        
        switch (current) {
            case ARROWS:
                next = CrossbowAmmoType.ROCKETS;
                break;
            case ROCKETS:
                next = CrossbowAmmoType.AUTO;
                break;
            case AUTO:
            default:
                next = CrossbowAmmoType.ARROWS;
                break;
        }

        config.crossbowAmmoType = next;
        ConfigManager.save();

        // Send colorful action bar message to player
        String ammoTypeName = getAmmoTypeName(next);
        Text message = Text.translatable("message.archersoffhand.crossbow_ammo_set", ammoTypeName).formatted(net.minecraft.util.Formatting.AQUA);
        client.player.sendMessage(message, true);
    }

    private static void toggleAmmoMode(MinecraftClient client) {
        if (client.player == null) return;

        ArchersOffhandConfig config = ConfigManager.CONFIG;
        if (config == null) return;

        // Cycle through ammo modes: REGULAR -> SHUFFLE -> SERIAL -> REGULAR...
        ArchersOffhandConfig.AmmoSwitchMode current = config.ammoMode;
        ArchersOffhandConfig.AmmoSwitchMode next;
        
        switch (current) {
            case REGULAR:
                next = ArchersOffhandConfig.AmmoSwitchMode.SHUFFLE;
                break;
            case SHUFFLE:
                next = ArchersOffhandConfig.AmmoSwitchMode.SERIAL;
                break;
            case SERIAL:
            default:
                next = ArchersOffhandConfig.AmmoSwitchMode.REGULAR;
                break;
        }

        config.ammoMode = next;
        ConfigManager.save();

        // Send colorful action bar message to player
        String modeName = getAmmoModeName(next);
        Text message = Text.translatable("message.archersoffhand.ammo_mode_set", modeName).formatted(net.minecraft.util.Formatting.GOLD);
        client.player.sendMessage(message, true);
    }

    private static void openConfigMenu(MinecraftClient client) {
        if (client.player == null || client.currentScreen != null) return;

        // Open the config menu
        client.execute(() -> {
            client.setScreen(com.carloplayz.archersoffhand.config.ArchersOffhandConfigScreen.create(client.currentScreen));
        });
    }

    private static String getAmmoTypeName(CrossbowAmmoType type) {
        switch (type) {
            case ARROWS:
                return "Arrows";
            case ROCKETS:
                return "Rockets";
            case AUTO:
                return "Auto (Rockets→Arrows)";
            default:
                return type.toString();
        }
    }

    private static String getAmmoModeName(ArchersOffhandConfig.AmmoSwitchMode mode) {
        switch (mode) {
            case REGULAR:
                return "Regular";
            case SHUFFLE:
                return "Shuffle";
            case SERIAL:
                return "Serial";
            default:
                return mode.toString();
        }
    }
}
