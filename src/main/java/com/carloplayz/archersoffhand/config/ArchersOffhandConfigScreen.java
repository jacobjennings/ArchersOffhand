package com.carloplayz.archersoffhand.config;

import dev.isxander.yacl3.api.*;
import dev.isxander.yacl3.api.controller.*;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

/**
 * YACL config screen builder. Create and open this from the mod menu or a
 * keybind.
 */
public class ArchersOffhandConfigScreen {
        public static Screen create(Screen parent) {
                ArchersOffhandConfig cfg = ConfigManager.CONFIG;

                // --- GENERAL OPTIONS ---
                Option<Boolean> enabledOption = Option.<Boolean>createBuilder()
                                .name(Text.literal("Enabled"))
                                .description(OptionDescription.of(Text.literal("Whether the mod is active")))
                                .binding(
                                                true, // default value
                                                () -> cfg.enabled,
                                                val -> cfg.enabled = val)
                                .controller(BooleanControllerBuilder::create)
                                .build();

                OptionGroup masterGroup = OptionGroup.createBuilder()
                                .name(Text.literal("Master Control"))
                                .option(enabledOption)
                                .build();

                // --- BEHAVIOR OPTIONS ---
                Option<ArchersOffhandConfig.AmmoSwitchMode> ammoSwitchModeOption = Option.<ArchersOffhandConfig.AmmoSwitchMode>createBuilder()
                                .name(Text.literal("Ammo Switch Mode"))
                                .description(OptionDescription.of(
                                                Text.literal("How ammo is selected from the inventory"),
                                                Text.literal("REGULAR: Find first available.\nSHUFFLE: Random.\nSERIAL: Cycle in order.")))
                                .binding(
                                                ArchersOffhandConfig.AmmoSwitchMode.REGULAR,
                                                () -> cfg.ammoMode,
                                                val -> cfg.ammoMode = val)
                                .controller(
                                                opt -> EnumControllerBuilder.create(opt)
                                                                .enumClass(ArchersOffhandConfig.AmmoSwitchMode.class))
                                .build();

                Option<ArchersOffhandConfig.CrossbowAmmoType> crossbowAmmoTypeOption = Option.<ArchersOffhandConfig.CrossbowAmmoType>createBuilder()
                                .name(Text.literal("Crossbow Ammo Type"))
                                .description(OptionDescription.of(
                                                Text.literal("Type of ammo to use with crossbow"),
                                                Text.literal(
                                                                "ARROWS: Use special arrows.\nROCKETS: Use explosive rockets.\nAUTO: Use rockets first, arrows as fallback.")))
                                .binding(
                                                ArchersOffhandConfig.CrossbowAmmoType.AUTO, // default
                                                () -> cfg.crossbowAmmoType,
                                                val -> cfg.crossbowAmmoType = val)
                                .controller(
                                                opt -> EnumControllerBuilder.create(opt)
                                                                .enumClass(ArchersOffhandConfig.CrossbowAmmoType.class))
                                .build();

                OptionGroup ammoGroup = OptionGroup.createBuilder()
                                .name(Text.literal("Ammo Selection"))
                                .option(ammoSwitchModeOption)
                                .option(crossbowAmmoTypeOption)
                                .build();

                Option<Double> lowHealthThresholdOption = Option.<Double>createBuilder()
                                .name(Text.literal("Low health threshold"))
                                .description(
                                                OptionDescription.of(Text.literal(
                                                                "Health threshold below which original offhand is restored")))
                                .binding(
                                                6.0, // default value
                                                () -> cfg.lowHealthThreshold,
                                                val -> cfg.lowHealthThreshold = val)
                                .controller(opt -> DoubleSliderControllerBuilder.create(opt).range(0.0, 20.0).step(0.5))
                                .build();

                Option<Boolean> restoreOnLowHealthOption = Option.<Boolean>createBuilder()
                                .name(Text.literal("Restore offhand on low health"))
                                .description(OptionDescription
                                                .of(Text.literal(
                                                                "Restore original offhand item when health drops below threshold")))
                                .binding(
                                                true, // default value
                                                () -> cfg.restoreOffhandOnLowHealth,
                                                val -> cfg.restoreOffhandOnLowHealth = val)
                                .controller(BooleanControllerBuilder::create)
                                .build();

                Option<Boolean> allowReplaceShieldTotemOption = Option.<Boolean>createBuilder()
                                .name(Text.literal("Allow replacing Shield/Totem"))
                                .description(OptionDescription.of(
                                                Text.literal("If unchecked, prevents replacing a Shield or Totem of Undying in your offhand.")))
                                .binding(
                                                true, // default value
                                                () -> cfg.allowReplaceShieldTotem,
                                                val -> cfg.allowReplaceShieldTotem = val)
                                .controller(BooleanControllerBuilder::create)
                                .build();

                OptionGroup failsafeGroup = OptionGroup.createBuilder()
                                .name(Text.literal("Failsafes and Protection"))
                                .option(restoreOnLowHealthOption)
                                .option(lowHealthThresholdOption)
                                .option(allowReplaceShieldTotemOption)
                                .build();

                // --- DELAYS ---
                Option<ArchersOffhandConfig.DelayPreset> delayPresetOption = Option.<ArchersOffhandConfig.DelayPreset>createBuilder()
                                .name(Text.literal("Delay Preset"))
                                .description(OptionDescription.of(
                                                Text.literal("Global preset for action delays"),
                                                Text.literal("PERFORMANCE: Maximize stability\nSPEED: 0 delay\nBALANCED: Default\nADAPTIVE: Dynamic anti-cheat decoy\nCUSTOM: Use sliders below")))
                                .binding(
                                                ArchersOffhandConfig.DelayPreset.BALANCED,
                                                () -> cfg.delayPreset,
                                                val -> cfg.delayPreset = val)
                                .controller(opt -> EnumControllerBuilder.create(opt)
                                                .enumClass(ArchersOffhandConfig.DelayPreset.class))
                                .build();

                Option<Integer> equipDelayOption = Option.<Integer>createBuilder()
                                .name(Text.literal("Equip Delay (ticks)"))
                                .description(OptionDescription.of(Text.literal(
                                                "Delay before sending ammo to the offhand after holding a bow/crossbow.")))
                                .binding(
                                                4,
                                                () -> cfg.getBaseEquipDelayTicks(cfg.delayPreset),
                                                val -> {
                                                        if (delayPresetOption
                                                                        .pendingValue() == ArchersOffhandConfig.DelayPreset.CUSTOM) {
                                                                cfg.equipDelayTicks = val;
                                                        }
                                                })
                                .controller(opt -> IntegerSliderControllerBuilder.create(opt).range(0, 40).step(1)
                                                .valueFormatter(val -> delayPresetOption
                                                                .pendingValue() == ArchersOffhandConfig.DelayPreset.ADAPTIVE
                                                                                ? Text.literal(val + " +/- 3")
                                                                                : Text.literal(val.toString())))
                                .available(cfg.delayPreset == ArchersOffhandConfig.DelayPreset.CUSTOM)
                                .build();

                Option<Integer> unequipDelayOption = Option.<Integer>createBuilder()
                                .name(Text.literal("Unequip Delay (ticks)"))
                                .description(OptionDescription.of(Text.literal(
                                                "Delay before restoring the original offhand item after unequipping a bow/crossbow.")))
                                .binding(
                                                4,
                                                () -> cfg.getBaseUnequipDelayTicks(cfg.delayPreset),
                                                val -> {
                                                        if (delayPresetOption
                                                                        .pendingValue() == ArchersOffhandConfig.DelayPreset.CUSTOM) {
                                                                cfg.unequipDelayTicks = val;
                                                        }
                                                })
                                .controller(opt -> IntegerSliderControllerBuilder.create(opt).range(0, 40).step(1)
                                                .valueFormatter(val -> delayPresetOption
                                                                .pendingValue() == ArchersOffhandConfig.DelayPreset.ADAPTIVE
                                                                                ? Text.literal(val + " +/- 3")
                                                                                : Text.literal(val.toString())))
                                .available(cfg.delayPreset == ArchersOffhandConfig.DelayPreset.CUSTOM)
                                .build();

                Option<Integer> swapDelayOption = Option.<Integer>createBuilder()
                                .name(Text.literal("Swap/Cycle Delay (ticks)"))
                                .description(OptionDescription.of(Text.literal(
                                                "Delay before loading the next piece of ammo after a shot (Shuffle/Serial modes).")))
                                .binding(
                                                2,
                                                () -> cfg.getBaseSwapDelayTicks(cfg.delayPreset),
                                                val -> {
                                                        if (delayPresetOption
                                                                        .pendingValue() == ArchersOffhandConfig.DelayPreset.CUSTOM) {
                                                                cfg.swapDelayTicks = val;
                                                        }
                                                })
                                .controller(opt -> IntegerSliderControllerBuilder.create(opt).range(0, 40).step(1)
                                                .valueFormatter(val -> delayPresetOption
                                                                .pendingValue() == ArchersOffhandConfig.DelayPreset.ADAPTIVE
                                                                                ? Text.literal(val + " +/- 3")
                                                                                : Text.literal(val.toString())))
                                .available(cfg.delayPreset == ArchersOffhandConfig.DelayPreset.CUSTOM)
                                .build();

                Option<Integer> inventoryScanDelayOption = Option.<Integer>createBuilder()
                                .name(Text.literal("Inventory Scan Delay (ticks)"))
                                .description(OptionDescription.of(Text.literal(
                                                "Delay between full inventory scans when out of ammo, avoiding constant lag.")))
                                .binding(
                                                10,
                                                () -> cfg.getBaseInventoryScanDelayTicks(cfg.delayPreset),
                                                val -> {
                                                        if (delayPresetOption
                                                                        .pendingValue() == ArchersOffhandConfig.DelayPreset.CUSTOM) {
                                                                cfg.inventoryScanDelayTicks = val;
                                                        }
                                                })
                                .controller(opt -> IntegerSliderControllerBuilder.create(opt).range(0, 40).step(1)
                                                .valueFormatter(val -> delayPresetOption
                                                                .pendingValue() == ArchersOffhandConfig.DelayPreset.ADAPTIVE
                                                                                ? Text.literal(val + " +/- 3")
                                                                                : Text.literal(val.toString())))
                                .available(cfg.delayPreset == ArchersOffhandConfig.DelayPreset.CUSTOM)
                                .build();

                Option<Integer> itemMovementDelayOption = Option.<Integer>createBuilder()
                                .name(Text.literal("Item Movement Delay (ticks)"))
                                .description(OptionDescription.of(Text.literal(
                                                "Delay between each simulated click when equipping ammo, preventing anti-cheat 'FastClick' kicks.")))
                                .binding(
                                                2,
                                                () -> cfg.getBaseItemMovementDelayTicks(cfg.delayPreset),
                                                val -> {
                                                        if (delayPresetOption
                                                                        .pendingValue() == ArchersOffhandConfig.DelayPreset.CUSTOM) {
                                                                cfg.itemMovementDelayTicks = val;
                                                        }
                                                })
                                .controller(opt -> IntegerSliderControllerBuilder.create(opt).range(0, 10).step(1)
                                                .valueFormatter(val -> delayPresetOption
                                                                .pendingValue() == ArchersOffhandConfig.DelayPreset.ADAPTIVE
                                                                                ? Text.literal(val + " +/- 3")
                                                                                : Text.literal(val.toString())))
                                .available(cfg.delayPreset == ArchersOffhandConfig.DelayPreset.CUSTOM)
                                .build();

                delayPresetOption.addListener((opt, newPreset) -> {
                        boolean isCustom = (newPreset == ArchersOffhandConfig.DelayPreset.CUSTOM);
                        equipDelayOption.setAvailable(isCustom);
                        unequipDelayOption.setAvailable(isCustom);
                        swapDelayOption.setAvailable(isCustom);
                        inventoryScanDelayOption.setAvailable(isCustom);
                        itemMovementDelayOption.setAvailable(isCustom);

                        if (!isCustom) {
                                equipDelayOption.requestSet(cfg.getBaseEquipDelayTicks(newPreset));
                                unequipDelayOption.requestSet(cfg.getBaseUnequipDelayTicks(newPreset));
                                swapDelayOption.requestSet(cfg.getBaseSwapDelayTicks(newPreset));
                                inventoryScanDelayOption.requestSet(cfg.getBaseInventoryScanDelayTicks(newPreset));
                                itemMovementDelayOption.requestSet(cfg.getBaseItemMovementDelayTicks(newPreset));
                        } else {
                                equipDelayOption.requestSet(cfg.equipDelayTicks);
                                unequipDelayOption.requestSet(cfg.unequipDelayTicks);
                                swapDelayOption.requestSet(cfg.swapDelayTicks);
                                inventoryScanDelayOption.requestSet(cfg.inventoryScanDelayTicks);
                                itemMovementDelayOption.requestSet(cfg.itemMovementDelayTicks);
                        }
                });

                OptionGroup delaysGroup = OptionGroup.createBuilder()
                                .name(Text.literal("Action Delays"))
                                .option(delayPresetOption)
                                .option(equipDelayOption)
                                .option(unequipDelayOption)
                                .option(swapDelayOption)
                                .option(inventoryScanDelayOption)
                                .option(itemMovementDelayOption)
                                .build();

                // --- ADVANCED OPTIONS ---
                Option<Boolean> debugLoggingOption = Option.<Boolean>createBuilder()
                                .name(Text.literal("Debug logging"))
                                .description(OptionDescription
                                                .of(Text.literal("Enable detailed logging for debugging")))
                                .binding(
                                                false, // default value
                                                () -> cfg.debugLogging,
                                                val -> cfg.debugLogging = val)
                                .controller(BooleanControllerBuilder::create)
                                .build();

                Option<Boolean> scanHotbarOption = Option.<Boolean>createBuilder()
                                .name(Text.literal("Scan Hotbar for Ammo"))
                                .description(OptionDescription.of(
                                                Text.literal("Whether to include hotbar slots when searching for arrows/rockets"),
                                                Text.literal("When disabled (default), only searches main inventory (slots 9-35)")))
                                .binding(
                                                false, // default value: do not scan hotbar
                                                () -> cfg.scanHotbar,
                                                val -> cfg.scanHotbar = val)
                                .controller(BooleanControllerBuilder::create)
                                .build();

                // Create categories
                ConfigCategory generalCategory = ConfigCategory.createBuilder()
                                .name(Text.literal("General"))
                                .group(masterGroup)
                                .build();

                ConfigCategory behaviorCategory = ConfigCategory.createBuilder()
                                .name(Text.literal("Behavior"))
                                .group(ammoGroup)
                                .group(failsafeGroup)
                                .build();

                ConfigCategory advancedCategory = ConfigCategory.createBuilder()
                                .name(Text.literal("Advanced"))
                                .group(delaysGroup)
                                .option(scanHotbarOption)
                                .option(debugLoggingOption)
                                .build();

                // Create the GUI
                YetAnotherConfigLib yacl = YetAnotherConfigLib.createBuilder()
                                .title(Text.literal("Archer's Offhand Settings"))
                                .category(generalCategory)
                                .category(behaviorCategory)
                                .category(advancedCategory)
                                .save(ConfigManager::save)
                                .build();

                return yacl.generateScreen(parent);
        }
}
