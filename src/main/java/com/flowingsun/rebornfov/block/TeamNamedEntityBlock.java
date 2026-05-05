package com.flowingsun.rebornfov.block;

import com.flowingsun.rebornfov.block.entity.PlacementAwareBlockEntity;
import com.flowingsun.rebornfov.block.entity.FovBlockEntity;
import com.flowingsun.rebornfov.block.entity.TeamNamedBlockEntity;
import com.flowingsun.rebornfov.menu.NameEditorMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.network.NetworkHooks;

public abstract class TeamNamedEntityBlock extends BaseEntityBlock {
    protected TeamNamedEntityBlock(Properties properties) {
        super(properties);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof PlacementAwareBlockEntity placementAwareBlockEntity) {
            placementAwareBlockEntity.finishInitialPlacement(placer, stack);
        }
        if (placer instanceof ServerPlayer serverPlayer) {
            openNameEditor(serverPlayer, pos, state, blockEntity);
        }
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (tryApplyInitialName(level, pos, player, hand)) {
            return InteractionResult.CONSUME;
        }
        return InteractionResult.PASS;
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock()) && level instanceof ServerLevel serverLevel) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof TeamNamedBlockEntity teamNamedBlockEntity) {
                teamNamedBlockEntity.unregisterSelf(serverLevel);
            } else if (blockEntity instanceof FovBlockEntity fovBlockEntity) {
                fovBlockEntity.unregisterSelf(serverLevel);
            }
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }

    protected boolean tryApplyInitialName(Level level, BlockPos pos, Player player, InteractionHand hand) {
        if (level.isClientSide || hand != InteractionHand.MAIN_HAND) {
            return false;
        }
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (!(blockEntity instanceof TeamNamedBlockEntity) && !(blockEntity instanceof FovBlockEntity)) {
            return false;
        }
        ItemStack held = player.getItemInHand(hand);
        if (!held.is(Items.NAME_TAG) || !held.hasCustomHoverName()) {
            return false;
        }

        Component customName = held.getHoverName();
        boolean success = false;
        Component displayName = null;

        if (blockEntity instanceof TeamNamedBlockEntity teamNamed) {
            success = teamNamed.trySetCustomName(customName);
            displayName = teamNamed.getDisplayName();
        } else if (blockEntity instanceof FovBlockEntity fovBlock) {
            success = fovBlock.trySetCustomName(customName);
            displayName = fovBlock.getDisplayName();
        }

        if (!success) {
            return false;
        }

        if (!player.getAbilities().instabuild) {
            held.shrink(1);
        }
        if (level instanceof ServerLevel) {
            player.displayClientMessage(Component.translatable("message.rebornfov.named", displayName), true);
        }
        return true;
    }

    private void openNameEditor(ServerPlayer player, BlockPos pos, BlockState state, BlockEntity blockEntity) {
        String initialName = "";
        if (blockEntity instanceof TeamNamedBlockEntity namedBlockEntity && namedBlockEntity.getCustomName() != null) {
            initialName = namedBlockEntity.getCustomName().getString();
        } else if (blockEntity instanceof FovBlockEntity fovBlockEntity && fovBlockEntity.getCustomName() != null) {
            initialName = fovBlockEntity.getCustomName().getString();
        }
        final String initialNameFinal = initialName;
        String blockLabel = state.getBlock().getName().getString();
        NetworkHooks.openScreen(player, new MenuProvider() {
            @Override
            public Component getDisplayName() {
                return Component.translatable("screen.rebornfov.name_editor", blockLabel);
            }

            @Override
            public AbstractContainerMenu createMenu(int id, Inventory inventory, Player menuPlayer) {
                return NameEditorMenu.server(id, inventory, pos, blockLabel, initialNameFinal);
            }
        }, buffer -> {
            buffer.writeBlockPos(pos);
            buffer.writeUtf(blockLabel);
            buffer.writeUtf(initialNameFinal);
        });
    }
}
