package com.flowingsun.rebornfov.block.entity;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public interface PlacementAwareBlockEntity {
    void finishInitialPlacement(LivingEntity placer, ItemStack stack);
}
