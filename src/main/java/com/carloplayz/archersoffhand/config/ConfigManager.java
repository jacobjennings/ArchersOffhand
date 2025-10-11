package com.carloplayz.archersoffhand.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.file.Path;

/**
 * Simple JSON config manager using Gson. Stores file at config/archersoffhand.json
 */
public class ConfigManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String FILE_NAME = "archersoffhand.json";
    public static ArchersOffhandConfig CONFIG;

    public static void init() {
        try {
            Path configDir = FabricLoader.getInstance().getConfigDir();
            File file = configDir.resolve(FILE_NAME).toFile();
            if (!file.exists()) {
                CONFIG = new ArchersOffhandConfig();
                save();
                return;
            }

            try (FileReader fr = new FileReader(file)) {
                CONFIG = GSON.fromJson(fr, ArchersOffhandConfig.class);
                if (CONFIG == null) CONFIG = new ArchersOffhandConfig();
            }
        } catch (Exception e) {
            e.printStackTrace();
            CONFIG = new ArchersOffhandConfig();
        }
    }

    public static void save() {
        try {
            Path configDir = FabricLoader.getInstance().getConfigDir();
            File file = configDir.resolve(FILE_NAME).toFile();
            try (FileWriter fw = new FileWriter(file)) {
                fw.write(GSON.toJson(CONFIG));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}