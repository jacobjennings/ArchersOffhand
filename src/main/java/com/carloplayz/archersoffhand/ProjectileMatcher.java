package com.carloplayz.archersoffhand;

import com.carloplayz.archersoffhand.config.ArchersOffhandConfig;
import com.carloplayz.archersoffhand.config.ProjectileCatalog;
import com.carloplayz.archersoffhand.config.ProjectilePreferenceTokens;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.component.FireworkExplosion;
import net.minecraft.world.item.component.Fireworks;

/** Matches item stacks against stable preference tokens in priority order. */
public final class ProjectileMatcher {
    private static final Predicate<ItemStack> CROSSBOW_PROJECTILE =
            ((CrossbowItem) Items.CROSSBOW).getSupportedHeldProjectiles();

    private ProjectileMatcher() {
    }

    public static boolean isSupported(ItemStack stack) {
        return !stack.isEmpty() && CROSSBOW_PROJECTILE.test(stack);
    }

    public static int rank(ItemStack stack, ArchersOffhandConfig config, HolderLookup.Provider registries) {
        if (!isSupported(stack)) {
            return Integer.MAX_VALUE;
        }
        for (int index = 0; index < config.projectilePreferences.size(); index++) {
            if (matches(stack, config.projectilePreferences.get(index), registries)) {
                return index;
            }
        }
        return matchesFallback(stack, config.fallbackPolicy)
                ? config.projectilePreferences.size()
                : Integer.MAX_VALUE;
    }

    public static boolean matches(ItemStack stack, String token, HolderLookup.Provider registries) {
        if (!isSupported(stack) || token == null) {
            return false;
        }
        if (token.equals(ProjectilePreferenceTokens.ANY_PROJECTILE)) {
            return true;
        }
        if (token.equals(ProjectilePreferenceTokens.ANY_ARROW)) {
            return stack.is(ItemTags.ARROWS);
        }
        if (token.equals(ProjectilePreferenceTokens.ANY_TIPPED_ARROW)) {
            return stack.is(Items.TIPPED_ARROW);
        }
        if (token.equals(ProjectilePreferenceTokens.ANY_FIREWORK)) {
            return stack.is(Items.FIREWORK_ROCKET);
        }
        if (token.startsWith("item:")) {
            Identifier actual = BuiltInRegistries.ITEM.getKey(stack.getItem());
            return actual != null && actual.toString().equals(token.substring("item:".length()));
        }
        if (token.startsWith("potion:")) {
            PotionContents contents = stack.get(DataComponents.POTION_CONTENTS);
            return contents != null && contents.potion()
                    .flatMap(holder -> holder.unwrapKey())
                    .map(key -> key.identifier().toString().equals(token.substring("potion:".length())))
                    .orElse(false);
        }
        if (token.startsWith("firework:")) {
            return matchesFirework(stack, token);
        }
        if (token.startsWith("exact:")) {
            Optional<ItemStack> expected = ProjectileCatalog.decodeExact(token, registries);
            return expected.isPresent() && ItemStack.isSameItemSameComponents(stack, expected.get());
        }
        return false;
    }

    private static boolean matchesFallback(ItemStack stack, ArchersOffhandConfig.FallbackPolicy fallback) {
        return switch (fallback) {
            case NONE -> false;
            case ANY_ARROW -> stack.is(ItemTags.ARROWS);
            case ANY_FIREWORK -> stack.is(Items.FIREWORK_ROCKET);
            case ANY_PROJECTILE -> isSupported(stack);
        };
    }

    private static boolean matchesFirework(ItemStack stack, String token) {
        if (!stack.is(Items.FIREWORK_ROCKET)) {
            return false;
        }
        Fireworks fireworks = stack.getOrDefault(DataComponents.FIREWORKS, new Fireworks(0, List.of()));
        String[] parts = token.split(":", 3);
        if (parts.length < 3) {
            return false;
        }
        return switch (parts[1]) {
            case "flight" -> parseInt(parts[2]).map(value -> fireworks.flightDuration() == value).orElse(false);
            case "utility" -> parseInt(parts[2])
                    .map(value -> fireworks.flightDuration() == value && fireworks.explosions().isEmpty())
                    .orElse(false);
            case "explosive" -> parseInt(parts[2])
                    .map(value -> fireworks.flightDuration() == value && !fireworks.explosions().isEmpty())
                    .orElse(false);
            case "shape" -> fireworks.explosions().stream()
                    .map(FireworkExplosion::shape)
                    .map(FireworkExplosion.Shape::getSerializedName)
                    .anyMatch(parts[2]::equals);
            default -> false;
        };
    }

    private static Optional<Integer> parseInt(String value) {
        try {
            return Optional.of(Integer.parseInt(value));
        } catch (NumberFormatException ignored) {
            return Optional.empty();
        }
    }
}
