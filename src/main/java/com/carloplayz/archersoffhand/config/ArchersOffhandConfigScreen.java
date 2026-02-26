package com.carloplayz.archersoffhand.config;

import dev.isxander.yacl3.api.*;
import dev.isxander.yacl3.api.controller.*;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

/**
 * YACL config screen builder. Create and open this from the mod menu or a keybind.
 */
public class ArchersOffhandConfigScreen {
    public static Screen create(Screen parent) {
        ArchersOffhandConfig cfg = ConfigManager.CONFIG;

        // Create the option list
        Option<Boolean> enabledOption = Option.createBuilder(Boolean.class)
                .name(Text.literal("Enabled"))
                .description(OptionDescription.of(Text.literal("Whether the mod is active")))
                .binding(
                    true, // default value
                    () -> cfg.enabled,
                    val -> cfg.enabled = val
                )
                .controller(BooleanControllerBuilder::create)
                .build();

        Option<Integer> actionDelayOption = Option.createBuilder(Integer.class)
                .name(Text.literal("Action delay (ticks)"))
                .description(OptionDescription.of(Text.literal("Delay in ticks between actions to prevent rapid spam")))
                .binding(
                    8, // default value
                    () -> cfg.actionDelayTicks,
                    val -> cfg.actionDelayTicks = val
                )
                .controller(opt -> IntegerSliderControllerBuilder.create(opt).range(0, 40).step(1))
                .build();

        Option<Double> lowHealthThresholdOption = Option.createBuilder(Double.class)
                .name(Text.literal("Low health threshold"))
                .description(OptionDescription.of(Text.literal("Health threshold below which original offhand is restored")))
                .binding(
                    6.0, // default value
                    () -> cfg.lowHealthThreshold,
                    val -> cfg.lowHealthThreshold = val
                )
                .controller(opt -> DoubleSliderControllerBuilder.create(opt).range(0.0, 20.0).step(0.5))
                .build();

        Option<Boolean> restoreOnLowHealthOption = Option.createBuilder(Boolean.class)
                .name(Text.literal("Restore offhand on low health"))
                .description(OptionDescription.of(Text.literal("Restore original offhand item when health drops below threshold")))
                .binding(
                    true, // default value
                    () -> cfg.restoreOffhandOnLowHealth,
                    val -> cfg.restoreOffhandOnLowHealth = val
                )
                .controller(BooleanControllerBuilder::create)
                .build();

        Option<Boolean> allowReplaceShieldTotemOption = Option.createBuilder(Boolean.class)
                .name(Text.literal("Allow replace shield/totem"))
                .description(OptionDescription.of(Text.literal("Allow replacing shields or totems in offhand")))
                .binding(
                    true, // default value
                    () -> cfg.allowReplaceShieldTotem,
                    val -> cfg.allowReplaceShieldTotem = val
                )
                .controller(BooleanControllerBuilder::create)
                .build();

        // Advanced options
        Option<Boolean> debugLoggingOption = Option.createBuilder(Boolean.class)
                .name(Text.literal("Debug logging"))
                .description(OptionDescription.of(Text.literal("Enable detailed logging for debugging")))
                .binding(
                    false, // default value
                    () -> cfg.debugLogging,
                    val -> cfg.debugLogging = val
                )
                .controller(BooleanControllerBuilder::create)
                .build();

        Option<Boolean> scanHotbarOption = Option.createBuilder(Boolean.class)
                .name(Text.literal("Scan Hotbar"))
                .description(OptionDescription.of(
                    Text.literal("Whether to include hotbar slots when searching for arrows/rockets"),
                    Text.literal("When disabled (default), only searches main inventory (slots 9-35)")
                ))
                .binding(
                    false, // default value: do not scan hotbar
                    () -> cfg.scanHotbar,
                    val -> cfg.scanHotbar = val
                )
                .controller(BooleanControllerBuilder::create)
                .build();

        // Create categories
        Option<ArchersOffhandConfig.CrossbowAmmoType> crossbowAmmoTypeOption = Option.createBuilder(ArchersOffhandConfig.CrossbowAmmoType.class)
                .name(Text.literal("Crossbow Ammo Type"))
                .description(OptionDescription.of(
                    Text.literal("Type of ammo to use with crossbow"),
                    Text.literal("ARROWS: Use arrows, ROCKETS: Use firework rockets, AUTO: Use rockets first, arrows as fallback")
                ))
                .binding(
                    ArchersOffhandConfig.CrossbowAmmoType.AUTO, // default
                    () -> cfg.crossbowAmmoType,
                    val -> cfg.crossbowAmmoType = val
                )
                .controller(opt -> EnumControllerBuilder.create(opt).enumClass(ArchersOffhandConfig.CrossbowAmmoType.class))
                .build();

        ConfigCategory generalCategory = ConfigCategory.createBuilder()
                .name(Text.literal("General"))
                .option(enabledOption)
                .option(actionDelayOption)
                .option(lowHealthThresholdOption)
                .option(restoreOnLowHealthOption)
                .option(allowReplaceShieldTotemOption)
                .option(crossbowAmmoTypeOption)
                .build();



        ConfigCategory advancedCategory = ConfigCategory.createBuilder()
                .name(Text.literal("Advanced"))
                .option(debugLoggingOption)
                .option(scanHotbarOption)
                .build();

        // Create the GUI
        YetAnotherConfigLib yacl = YetAnotherConfigLib.createBuilder()
                .title(Text.literal("Archer's Offhand Settings"))
                .category(generalCategory)
                .category(advancedCategory)
                .save(ConfigManager::save)
                .build();

        return yacl.generateScreen(parent);
    }
}
