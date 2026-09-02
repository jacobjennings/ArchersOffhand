package com.carloplayz.archersoffhand.config;

import com.carloplayz.archersoffhand.ArchersOffhand;
import com.mojang.serialization.JsonOps;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.component.FireworkExplosion;
import net.minecraft.world.item.component.Fireworks;

/** Builds the UI's projectile choices and serializes exact component variants. */
public final class ProjectileCatalog {
    private static final Gson GSON = new Gson();

    private ProjectileCatalog() {
    }

    public record Entry(String token, Component label, ItemStack icon) {
    }

    public static List<Entry> create(Minecraft minecraft) {
        Map<String, Entry> entries = new LinkedHashMap<>();
        add(entries, ProjectilePreferenceTokens.ANY_PROJECTILE, "Any compatible projectile", Items.CROSSBOW.getDefaultInstance());
        add(entries, ProjectilePreferenceTokens.ANY_ARROW, "Any arrow", Items.ARROW.getDefaultInstance());
        add(entries, ProjectilePreferenceTokens.NORMAL_ARROW, "Normal Arrow", Items.ARROW.getDefaultInstance());
        add(entries, ProjectilePreferenceTokens.SPECTRAL_ARROW, "Spectral Arrow", Items.SPECTRAL_ARROW.getDefaultInstance());
        add(entries, ProjectilePreferenceTokens.ANY_TIPPED_ARROW, "Any tipped arrow", Items.TIPPED_ARROW.getDefaultInstance());

        BuiltInRegistries.POTION.listElements()
                .sorted((a, b) -> a.key().identifier().compareTo(b.key().identifier()))
                .forEach(holder -> addPotion(entries, holder));
        if (minecraft.level != null) {
            // Include server-provided registry additions while keeping the screen useful
            // when Mod Menu is opened from the title screen.
            minecraft.level.registryAccess().lookupOrThrow(Registries.POTION).listElements()
                    .sorted((a, b) -> a.key().identifier().compareTo(b.key().identifier()))
                    .forEach(holder -> addPotion(entries, holder));
        }

        add(entries, ProjectilePreferenceTokens.ANY_FIREWORK, "Any firework rocket", Items.FIREWORK_ROCKET.getDefaultInstance());
        for (int flight = 0; flight <= 3; flight++) {
            add(entries, ProjectilePreferenceTokens.fireworkFlight(flight),
                    "Any Flight " + flight + " firework", firework(flight, List.of()));
            add(entries, ProjectilePreferenceTokens.fireworkUtility(flight),
                    "Flight " + flight + " utility rocket (no explosions)", firework(flight, List.of()));
            add(entries, ProjectilePreferenceTokens.fireworkExplosive(flight),
                    "Flight " + flight + " explosive rocket", firework(flight, List.of(FireworkExplosion.DEFAULT)));
        }
        for (FireworkExplosion.Shape shape : FireworkExplosion.Shape.values()) {
            add(entries, ProjectilePreferenceTokens.fireworkShape(shape.getSerializedName()),
                    "Any rocket containing a " + words(shape.getSerializedName()) + " explosion",
                    firework(1, List.of(new FireworkExplosion(shape, it.unimi.dsi.fastutil.ints.IntList.of(0xFFFFFF),
                            it.unimi.dsi.fastutil.ints.IntList.of(), false, false))));
        }

        if (minecraft.player != null && minecraft.level != null) {
            Inventory inventory = minecraft.player.getInventory();
            for (int slot = 0; slot < Inventory.INVENTORY_SIZE; slot++) {
                addObserved(entries, inventory.getItem(slot), minecraft.level.registryAccess());
            }
            addObserved(entries, minecraft.player.getOffhandItem(), minecraft.level.registryAccess());
        }
        if (minecraft.level != null) {
            // Keep previously selected exact variants visible even when the player
            // temporarily has none of that projectile in their inventory.
            for (String token : ConfigManager.CONFIG.projectilePreferences) {
                if (token.startsWith("exact:")) {
                    decodeExact(token, minecraft.level.registryAccess()).ifPresent(stack ->
                            entries.putIfAbsent(token, new Entry(token, describeExact(stack), stack)));
                }
            }
        }
        return List.copyOf(entries.values());
    }

    public static Component labelForToken(String token) {
        Minecraft minecraft = Minecraft.getInstance();
        return create(minecraft).stream()
                .filter(entry -> entry.token().equals(token))
                .map(Entry::label)
                .findFirst()
                .orElseGet(() -> Component.literal(token.startsWith("exact:") ? "Exact projectile variant" : token));
    }

    public static Optional<String> encodeExact(ItemStack stack, HolderLookup.Provider registries) {
        if (stack.isEmpty()) {
            return Optional.empty();
        }
        ItemStack one = stack.copyWithCount(1);
        return ItemStack.CODEC.encodeStart(registries.createSerializationContext(JsonOps.INSTANCE), one)
                .resultOrPartial(message -> ArchersOffhand.LOGGER.warn("Could not encode projectile: {}", message))
                .map(GSON::toJson)
                .map(ProjectilePreferenceTokens::exactStack);
    }

    public static Optional<ItemStack> decodeExact(String token, HolderLookup.Provider registries) {
        String json = ProjectilePreferenceTokens.exactStackJson(token);
        if (json.isEmpty()) {
            return Optional.empty();
        }
        try {
            JsonElement element = JsonParser.parseString(json);
            return ItemStack.CODEC.parse(registries.createSerializationContext(JsonOps.INSTANCE), element)
                    .resultOrPartial(message -> ArchersOffhand.LOGGER.warn("Could not decode projectile preference: {}", message));
        } catch (RuntimeException exception) {
            ArchersOffhand.LOGGER.warn("Could not parse exact projectile preference", exception);
            return Optional.empty();
        }
    }

    public static Component describeExact(ItemStack stack) {
        if (stack.is(Items.FIREWORK_ROCKET)) {
            return Component.literal("Exact firework — " + describeFirework(stack));
        }
        PotionContents potion = stack.get(DataComponents.POTION_CONTENTS);
        if (potion != null) {
            List<String> details = new ArrayList<>();
            potion.potion().flatMap(Holder::unwrapKey)
                    .ifPresent(key -> details.add(key.identifier().toString()));
            potion.customColor().ifPresent(color -> details.add(String.format(
                    Locale.ROOT, "color #%06X", color & 0xFFFFFF)));
            if (!potion.customEffects().isEmpty()) {
                details.add(potion.customEffects().size() + " custom effect(s)");
            }
            potion.customName().ifPresent(name -> details.add("name " + name));
            return Component.literal("Exact " + stack.getHoverName().getString()
                    + (details.isEmpty() ? "" : " — " + String.join(", ", details)));
        }
        return Component.literal("Exact " + stack.getHoverName().getString());
    }

    private static void addObserved(Map<String, Entry> entries, ItemStack stack, HolderLookup.Provider registries) {
        if (!isSupported(stack)) {
            return;
        }
        Identifier itemId = BuiltInRegistries.ITEM.getKey(stack.getItem());
        if (itemId != null && !isBuiltInFamily(stack)) {
            String generic = ProjectilePreferenceTokens.item(itemId.toString());
            entries.putIfAbsent(generic, new Entry(generic,
                    Component.literal("Any " + stack.getHoverName().getString()), stack.copyWithCount(1)));
        }
        encodeExact(stack, registries).ifPresent(token -> entries.putIfAbsent(token,
                new Entry(token, describeExact(stack), stack.copyWithCount(1))));
    }

    private static void addPotion(Map<String, Entry> entries, Holder.Reference<Potion> potion) {
        String id = potion.key().identifier().toString();
        ItemStack stack = PotionContents.createItemStack(Items.TIPPED_ARROW, potion);
        add(entries, ProjectilePreferenceTokens.potion(id),
                stack.getHoverName().getString() + " [" + id + "]", stack);
    }

    private static void add(Map<String, Entry> entries, String token, String label, ItemStack icon) {
        entries.putIfAbsent(token, new Entry(token, Component.literal(label), icon));
    }

    private static ItemStack firework(int flight, List<FireworkExplosion> explosions) {
        ItemStack stack = Items.FIREWORK_ROCKET.getDefaultInstance();
        stack.set(DataComponents.FIREWORKS, new Fireworks(flight, explosions));
        return stack;
    }

    private static String describeFirework(ItemStack stack) {
        Fireworks fireworks = stack.getOrDefault(DataComponents.FIREWORKS, new Fireworks(0, List.of()));
        if (fireworks.explosions().isEmpty()) {
            return "Flight " + fireworks.flightDuration() + ", no explosions";
        }
        List<String> explosions = new ArrayList<>();
        for (FireworkExplosion explosion : fireworks.explosions()) {
            StringBuilder text = new StringBuilder(words(explosion.shape().getSerializedName()));
            if (!explosion.colors().isEmpty()) {
                text.append(" colors ").append(hexColors(explosion.colors()));
            }
            if (!explosion.fadeColors().isEmpty()) {
                text.append(" fade ").append(hexColors(explosion.fadeColors()));
            }
            if (explosion.hasTrail()) {
                text.append(" trail");
            }
            if (explosion.hasTwinkle()) {
                text.append(" twinkle");
            }
            explosions.add(text.toString());
        }
        return "Flight " + fireworks.flightDuration() + ", " + String.join("; ", explosions);
    }

    private static String hexColors(it.unimi.dsi.fastutil.ints.IntList colors) {
        List<String> values = new ArrayList<>();
        for (int color : colors) {
            values.add(String.format(Locale.ROOT, "#%06X", color & 0xFFFFFF));
        }
        return String.join("/", values);
    }

    private static String words(String value) {
        String replaced = value.replace('_', ' ');
        return Character.toUpperCase(replaced.charAt(0)) + replaced.substring(1);
    }

    private static boolean isSupported(ItemStack stack) {
        return !stack.isEmpty()
                && ((CrossbowItem) Items.CROSSBOW).getSupportedHeldProjectiles().test(stack);
    }

    private static boolean isBuiltInFamily(ItemStack stack) {
        return stack.is(Items.ARROW)
                || stack.is(Items.SPECTRAL_ARROW)
                || stack.is(Items.TIPPED_ARROW)
                || stack.is(Items.FIREWORK_ROCKET);
    }
}
