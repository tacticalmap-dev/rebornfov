package com.flowingsun.rebornfov.block;

import com.flowingsun.rebornfov.block.entity.TeamBagBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class TeamBagBlock extends TeamNamedEntityBlock {
    private static final VoxelShape SHAPE = Shapes.or(
            box(2.25, 0, 2.25, 13.75, 5.0, 13.75),
            box(4.5, 5.0, 4.5, 11.5, 5.25, 11.5)
    );

    public TeamBagBlock(Properties properties) {
        super(properties);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TeamBagBlockEntity(pos, state);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }
}
