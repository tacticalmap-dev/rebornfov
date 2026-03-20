package com.flowingsun.rebornfov.block.entity;

import com.flowingsun.rebornfov.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class TeamBagBlockEntity extends TeamNamedBlockEntity {
    public TeamBagBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.TEAM_BAG.get(), pos, state);
    }

    @Override
    public String getBlockKind() {
        return "teambag";
    }
}
