package com.flowingsun.rebornfov.block;

import com.flowingsun.rebornfov.block.entity.FovBlockEntity;
import com.flowingsun.rebornfov.config.SupplyPresetManager;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.network.NetworkHooks;

import java.util.Properties;

public class FovBlock extends TeamNamedEntityBlock {
    public FovBlock(Properties properties) {
        super(properties);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new FovBlockEntity(pos, state);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (!(blockEntity instanceof FovBlockEntity fov)) {
            return InteractionResult.PASS;
        }
        if (player.isShiftKeyDown()) {
            NetworkHooks.openScreen((ServerPlayer) player, fov.createPresetMenuProvider(), buffer -> {
                buffer.writeBlockPos(pos);
                buffer.writeUtf(fov.getPresetId());
                var presets = SupplyPresetManager.getPresets();
                buffer.writeVarInt(presets.size());
                for (var preset : presets) {
                    buffer.writeUtf(preset.id());
                    buffer.writeUtf(preset.displayName());
                }
            });
        } else {
            MenuProvider provider = state.getMenuProvider(level, pos);
            if (provider != null) {
                NetworkHooks.openScreen((ServerPlayer) player, provider, pos);
            }
        }
        return InteractionResult.CONSUME;
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof FovBlockEntity fov) {
                net.minecraft.world.Containers.dropContents(level, pos, fov);
                level.updateNeighbourForOutputSignal(pos, this);
            }
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide ? null : (lvl, pos, blockState, blockEntity) -> {
            if (blockEntity instanceof FovBlockEntity fovBlockEntity) {
                fovBlockEntity.serverTick();
            }
        };
    }
}
