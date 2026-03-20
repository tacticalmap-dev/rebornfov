package com.flowingsun.rebornfov.block;

import com.flowingsun.rebornfov.block.entity.TeamBagBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class TeamBagBlock extends TeamNamedEntityBlock {
    public TeamBagBlock(Properties properties) {
        super(properties);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TeamBagBlockEntity(pos, state);
    }
}
