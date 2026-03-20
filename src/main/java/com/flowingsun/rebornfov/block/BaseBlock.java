package com.flowingsun.rebornfov.block;

import com.flowingsun.rebornfov.data.RebornFovSavedData;
import com.flowingsun.rebornfov.data.TeleportTarget;
import com.flowingsun.rebornfov.menu.BaseMenu;
import com.flowingsun.rebornfov.team.TeamResolver;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.network.NetworkHooks;

import java.util.List;

public class BaseBlock extends Block implements EntityBlock {
    public BaseBlock(Properties properties) {
        super(properties);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        ServerPlayer serverPlayer = (ServerPlayer) player;
        String teamId = TeamResolver.resolveTeamId(serverPlayer);
        List<TeleportTarget> targets = RebornFovSavedData.get((ServerLevel) level).getTargets(teamId);
        NetworkHooks.openScreen(serverPlayer, new MenuProvider() {
            @Override
            public Component getDisplayName() {
                return Component.translatable("screen.rebornfov.base", teamId);
            }

            @Override
            public AbstractContainerMenu createMenu(int id, Inventory inventory, Player menuPlayer) {
                return BaseMenu.server(id, inventory, pos, teamId);
            }
        }, buffer -> {
            buffer.writeBlockPos(pos);
            buffer.writeUtf(teamId);
            buffer.writeVarInt(targets.size());
            for (TeleportTarget target : targets) {
                buffer.writeUtf(target.id());
                buffer.writeUtf(target.type());
                buffer.writeUtf(target.name());
                buffer.writeResourceLocation(target.dimension());
                buffer.writeBlockPos(target.pos());
            }
        });
        return InteractionResult.CONSUME;
    }

    @Override
    public net.minecraft.world.level.block.entity.BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return null;
    }
}
