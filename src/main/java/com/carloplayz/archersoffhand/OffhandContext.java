package com.carloplayz.archersoffhand;

import com.carloplayz.archersoffhand.config.ArchersOffhandConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;

public class OffhandContext {
    public final MinecraftClient client;
    public final PlayerEntity player;
    public final ArchersOffhandConfig config;
    public final InventoryManager inventoryManager;
    public final ItemStack mainHandStack;
    public final ItemStack offHandStack;
    public final boolean isHoldingBow;
    public final boolean isHoldingCrossbow;

    public OffhandContext(MinecraftClient client, PlayerEntity player, ArchersOffhandConfig config,
            InventoryManager inventoryManager) {
        this.client = client;
        this.player = player;
        this.config = config;
        this.inventoryManager = inventoryManager;
        this.mainHandStack = player.getMainHandStack();
        this.offHandStack = player.getOffHandStack();
        this.isHoldingBow = mainHandStack.getItem() instanceof net.minecraft.item.BowItem;
        this.isHoldingCrossbow = mainHandStack.getItem() instanceof net.minecraft.item.CrossbowItem;
    }

    public boolean hasWeapon() {
        return isHoldingBow || isHoldingCrossbow;
    }
}
