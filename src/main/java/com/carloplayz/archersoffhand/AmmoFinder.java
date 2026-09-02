package com.carloplayz.archersoffhand;

import com.carloplayz.archersoffhand.config.ArchersOffhandConfig;
import java.util.Optional;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

/** Selects the lowest-ranked available projectile, preserving inventory order for ties. */
public final class AmmoFinder {
    private AmmoFinder() {
    }

    public record Match(int inventorySlot, int rank, ItemStack stack) {
    }

    public static Optional<Match> findBest(Inventory inventory, ArchersOffhandConfig config,
            HolderLookup.Provider registries, int excludedSlot) {
        int firstSlot = config.scanHotbar ? 0 : Inventory.SELECTION_SIZE;
        Match best = null;
        for (int slot = firstSlot; slot < Inventory.INVENTORY_SIZE; slot++) {
            if (slot == excludedSlot) {
                continue;
            }
            ItemStack stack = inventory.getItem(slot);
            int rank = ProjectileMatcher.rank(stack, config, registries);
            if (rank == Integer.MAX_VALUE) {
                continue;
            }
            if (best == null || rank < best.rank()) {
                best = new Match(slot, rank, stack.copy());
            }
        }
        return Optional.ofNullable(best);
    }
}
