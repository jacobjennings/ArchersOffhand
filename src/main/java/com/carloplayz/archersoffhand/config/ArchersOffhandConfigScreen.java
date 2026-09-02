package com.carloplayz.archersoffhand.config;

import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.ListOption;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import dev.isxander.yacl3.api.OptionGroup;
import dev.isxander.yacl3.api.YetAnotherConfigLib;
import dev.isxander.yacl3.api.controller.BooleanControllerBuilder;
import dev.isxander.yacl3.api.controller.DoubleSliderControllerBuilder;
import dev.isxander.yacl3.api.controller.EnumControllerBuilder;
import dev.isxander.yacl3.api.controller.IntegerSliderControllerBuilder;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/** Builds the YACL screen for all persistent Archer's Offhand settings. */
public final class ArchersOffhandConfigScreen {
    private ArchersOffhandConfigScreen() {
    }

    public static Screen create(Screen parent) {
        ArchersOffhandConfig loaded = ConfigManager.CONFIG;
        if (loaded == null) {
            loaded = new ArchersOffhandConfig();
            ConfigManager.CONFIG = loaded;
        }
        loaded.validate();
        final ArchersOffhandConfig cfg = loaded;

        Map<String, Component> projectileLabels = new LinkedHashMap<>();
        for (ProjectileCatalog.Entry entry : ProjectileCatalog.create(Minecraft.getInstance())) {
            projectileLabels.putIfAbsent(entry.token(), entry.label());
        }

        // Keep saved entries available even when an exact inventory variant is
        // not currently in the player's inventory. This prevents the UI from
        // silently dropping a stable persisted token.
        for (String token : cfg.projectilePreferences) {
            if (!projectileLabels.containsKey(token)) {
                projectileLabels.put(token, labelForUnknownToken(token));
            }
        }

        Option<Boolean> enabledOption = booleanOption(
                "Enabled",
                "Master switch for automatic offhand management.",
                true,
                () -> cfg.enabled,
                value -> cfg.enabled = value);
        Option<Boolean> scanHotbarOption = booleanOption(
                "Scan hotbar",
                "Include hotbar slots when searching for compatible projectiles.",
                true,
                () -> cfg.scanHotbar,
                value -> cfg.scanHotbar = value);
        Option<Boolean> debugLoggingOption = booleanOption(
                "Debug logging",
                "Write detailed state and inventory diagnostics to the log.",
                false,
                () -> cfg.debugLogging,
                value -> cfg.debugLogging = value);

        Option<Boolean> replaceOccupiedOffhandOption = booleanOption(
                "Replace an occupied offhand",
                "Allow the mod to replace a non-empty offhand item while a ranged weapon is held.",
                true,
                () -> cfg.replaceOccupiedOffhand,
                value -> cfg.replaceOccupiedOffhand = value);
        Option<Boolean> restoreOnLowHealthOption = booleanOption(
                "Restore on low health",
                "Restore the original offhand item when health falls below the threshold.",
                true,
                () -> cfg.restoreOnLowHealth,
                value -> cfg.restoreOnLowHealth = value);
        Option<Double> lowHealthThresholdOption = Option.<Double>createBuilder()
                .name(Component.literal("Low-health threshold"))
                .description(OptionDescription.of(
                        Component.literal("Health points (half-hearts) at which the original offhand is restored.")))
                .binding(
                        6.0,
                        () -> cfg.lowHealthThreshold,
                        value -> cfg.lowHealthThreshold = value)
                .controller(option -> DoubleSliderControllerBuilder.create(option)
                        .range(0.0, 20.0)
                        .step(0.5)
                        .valueFormatter(value -> Component.literal(String.format(
                                Locale.ROOT, "%.1f hearts", value / 2.0))))
                .build();

        Option<Integer> equipDelayOption = ticksOption(
                "Equip delay",
                "Ticks to wait before moving a compatible projectile into the offhand.",
                2,
                () -> cfg.equipDelayTicks,
                value -> cfg.equipDelayTicks = value);
        Option<Integer> restoreDelayOption = ticksOption(
                "Restore delay",
                "Ticks to wait before restoring the original offhand item.",
                2,
                () -> cfg.restoreDelayTicks,
                value -> cfg.restoreDelayTicks = value);
        Option<Integer> clickCooldownOption = ticksOption(
                "Click cooldown",
                "Ticks between simulated inventory clicks while moving an item.",
                2,
                () -> cfg.clickCooldownTicks,
                value -> cfg.clickCooldownTicks = value);

        ListOption<String> projectilePreferencesOption = ListOption.<String>createBuilder()
                .name(Component.literal("Projectile preference order"))
                .description(OptionDescription.of(
                        Component.literal("Rules are evaluated from top to bottom."),
                        Component.literal("Use the up/down buttons or drag entries to reorder them."),
                        Component.literal("The catalog includes exact arrow and firework variants observed in your inventory.")))
                .initial(ProjectilePreferenceTokens.NORMAL_ARROW)
                .binding(
                        new ArrayList<>(List.of(ProjectilePreferenceTokens.NORMAL_ARROW)),
                        () -> new ArrayList<>(cfg.projectilePreferences),
                        values -> cfg.projectilePreferences = new ArrayList<>(values))
                .customController(entry -> new ProjectilePreferenceController(entry, projectileLabels))
                .minimumNumberOfEntries(0)
                .maximumNumberOfEntries(128)
                .insertEntriesAtEnd(true)
                .build();

        Option<ArchersOffhandConfig.FallbackPolicy> fallbackPolicyOption = Option.<ArchersOffhandConfig.FallbackPolicy>createBuilder()
                .name(Component.literal("Fallback policy"))
                .description(OptionDescription.of(
                        Component.literal("Applied only after every ordered preference fails."),
                        Component.literal("NONE disables the final fallback search.")))
                .binding(
                        ArchersOffhandConfig.FallbackPolicy.ANY_PROJECTILE,
                        () -> cfg.fallbackPolicy,
                        value -> cfg.fallbackPolicy = value)
                .controller(option -> EnumControllerBuilder.create(option)
                        .enumClass(ArchersOffhandConfig.FallbackPolicy.class)
                        .valueFormatter(ArchersOffhandConfigScreen::fallbackPolicyLabel))
                .build();

        OptionGroup generalGroup = OptionGroup.createBuilder()
                .name(Component.literal("Mod behavior"))
                .description(OptionDescription.of(
                        Component.literal("Choose when automatic projectile management is active.")))
                .option(enabledOption)
                .option(scanHotbarOption)
                .build();
        OptionGroup diagnosticsGroup = OptionGroup.createBuilder()
                .name(Component.literal("Diagnostics"))
                .description(OptionDescription.of(
                        Component.literal("Useful when troubleshooting inventory or timing behavior.")))
                .option(debugLoggingOption)
                .build();

        OptionGroup fallbackGroup = OptionGroup.createBuilder()
                .name(Component.literal("Fallback"))
                .description(OptionDescription.of(
                        Component.literal("Select what may be used when the ordered list has no match.")))
                .option(fallbackPolicyOption)
                .build();

        OptionGroup safetyGroup = OptionGroup.createBuilder()
                .name(Component.literal("Offhand protection"))
                .description(OptionDescription.of(
                        Component.literal("Prevent unwanted replacements and restore the original item safely.")))
                .option(replaceOccupiedOffhandOption)
                .option(restoreOnLowHealthOption)
                .option(lowHealthThresholdOption)
                .build();

        OptionGroup timingGroup = OptionGroup.createBuilder()
                .name(Component.literal("Inventory timing"))
                .description(OptionDescription.of(
                        Component.literal("Small delays make simulated inventory actions reliable across servers.")))
                .option(equipDelayOption)
                .option(restoreDelayOption)
                .option(clickCooldownOption)
                .build();

        ConfigCategory generalCategory = ConfigCategory.createBuilder()
                .name(Component.literal("General"))
                .group(generalGroup)
                .group(diagnosticsGroup)
                .build();
        ConfigCategory projectileCategory = ConfigCategory.createBuilder()
                .name(Component.literal("Projectile selection"))
                // ListOption must be a direct category group so YACL can render
                // its reorder, add, and remove controls.
                .group(projectilePreferencesOption)
                .group(fallbackGroup)
                .build();
        ConfigCategory safetyCategory = ConfigCategory.createBuilder()
                .name(Component.literal("Safety"))
                .group(safetyGroup)
                .build();
        ConfigCategory timingCategory = ConfigCategory.createBuilder()
                .name(Component.literal("Timing"))
                .group(timingGroup)
                .build();

        YetAnotherConfigLib yacl = YetAnotherConfigLib.createBuilder()
                .title(Component.literal("Archer's Offhand Settings"))
                .category(generalCategory)
                .category(projectileCategory)
                .category(safetyCategory)
                .category(timingCategory)
                .save(() -> {
                    cfg.validate();
                    ConfigManager.save();
                })
                .build();

        return yacl.generateScreen(parent);
    }

    private static Option<Boolean> booleanOption(
            String name,
            String description,
            boolean defaultValue,
            Supplier<Boolean> getter,
            Consumer<Boolean> setter) {
        return Option.<Boolean>createBuilder()
                .name(Component.literal(name))
                .description(OptionDescription.of(Component.literal(description)))
                .binding(defaultValue, getter, setter)
                .controller(option -> BooleanControllerBuilder.create(option).onOffFormatter())
                .build();
    }

    private static Option<Integer> ticksOption(
            String name,
            String description,
            int defaultValue,
            Supplier<Integer> getter,
            Consumer<Integer> setter) {
        return Option.<Integer>createBuilder()
                .name(Component.literal(name))
                .description(OptionDescription.of(Component.literal(description)))
                .binding(defaultValue, getter, setter)
                .controller(option -> IntegerSliderControllerBuilder.create(option)
                        .range(0, 40)
                        .step(1)
                        .valueFormatter(value -> Component.literal(value + " ticks")))
                .build();
    }

    private static Component fallbackPolicyLabel(ArchersOffhandConfig.FallbackPolicy policy) {
        return Component.literal(switch (policy) {
            case NONE -> "None";
            case ANY_ARROW -> "Any arrow";
            case ANY_FIREWORK -> "Any firework rocket";
            case ANY_PROJECTILE -> "Any compatible projectile";
        });
    }

    private static Component labelForUnknownToken(String token) {
        if (token.startsWith("exact:")) {
            return Component.literal("Exact projectile variant (saved)");
        }
        if (token.startsWith("item:")) {
            return Component.literal("Any " + token.substring("item:".length()) + " projectile");
        }
        if (token.startsWith("potion:")) {
            return Component.literal("Potion arrow: " + token.substring("potion:".length()));
        }
        if (token.startsWith("firework:")) {
            return Component.literal("Firework preference: " + token.substring("firework:".length()));
        }
        return Component.literal("Configured projectile: " + token);
    }
}
