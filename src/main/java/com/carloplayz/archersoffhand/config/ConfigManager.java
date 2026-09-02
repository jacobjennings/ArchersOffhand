package com.carloplayz.archersoffhand.config;

import com.carloplayz.archersoffhand.ArchersOffhand;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Collections;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

/** Loads and atomically saves {@code config/archersoffhand.json}. */
public final class ConfigManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("archersoffhand.json");
    public static ArchersOffhandConfig CONFIG = new ArchersOffhandConfig();

    private ConfigManager() {
    }

    public static void init() {
        if (Files.isRegularFile(FILE)) {
            try (Reader reader = Files.newBufferedReader(FILE)) {
                ArchersOffhandConfig loaded = GSON.fromJson(reader, ArchersOffhandConfig.class);
                if (loaded != null) {
                    CONFIG = loaded;
                }
            } catch (Exception exception) {
                ArchersOffhand.LOGGER.error("Could not read {}; using defaults", FILE, exception);
            }
        }
        CONFIG.validate();
        save();
    }

    public static void save() {
        CONFIG.validate();
        Path temporary = FILE.resolveSibling(FILE.getFileName() + ".tmp");
        try {
            Files.createDirectories(FILE.getParent());
            try (Writer writer = Files.newBufferedWriter(temporary)) {
                GSON.toJson(CONFIG, writer);
            }
            try {
                Files.move(temporary, FILE, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (IOException unsupportedAtomicMove) {
                Files.move(temporary, FILE, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException exception) {
            ArchersOffhand.LOGGER.error("Could not save {}", FILE, exception);
        }
    }

    public static void cyclePrimaryPreference(Minecraft minecraft) {
        if (CONFIG.projectilePreferences.size() > 1) {
            Collections.rotate(CONFIG.projectilePreferences, -1);
            save();
        }
        Component label = CONFIG.projectilePreferences.isEmpty()
                ? Component.translatable("archersoffhand.preference.none")
                : ProjectileCatalog.labelForToken(CONFIG.projectilePreferences.getFirst());
        if (minecraft.player != null) {
            minecraft.player.sendOverlayMessage(
                    Component.translatable("message.archersoffhand.preference", label));
        }
    }
}
