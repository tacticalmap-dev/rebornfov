package com.flowingsun.rebornfov.block.entity;

import com.flowingsun.rebornfov.data.RebornFovSavedData;
import com.flowingsun.rebornfov.data.TeleportTarget;
import com.flowingsun.rebornfov.team.TeamResolver;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public abstract class TeamNamedBlockEntity extends BlockEntity implements PlacementAwareBlockEntity {
    protected String teamId = "";
    protected String pointId = "";
    protected Component customName;

    protected TeamNamedBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public void finishInitialPlacement(LivingEntity placer, ItemStack stack) {
        if (!(placer instanceof ServerPlayer serverPlayer) || !(level instanceof ServerLevel serverLevel)) {
            return;
        }
        if (stack.hasCustomHoverName()) {
            this.customName = stack.getHoverName();
        }
        this.teamId = TeamResolver.resolveTeamId(serverPlayer);
        this.pointId = getBlockKind() + "@" + worldPosition.toShortString() + "@" + serverLevel.dimension().location();
        registerSelf(serverLevel);
        setChanged();
    }

    public void registerSelf(ServerLevel serverLevel) {
        if (teamId.isBlank()) {
            return;
        }
        RebornFovSavedData.get(serverLevel).putTarget(new TeleportTarget(pointId, teamId, getBlockKind(), getDisplayName().getString(), serverLevel.dimension().location(), worldPosition));
    }

    public void unregisterSelf(ServerLevel serverLevel) {
        if (!teamId.isBlank() && !pointId.isBlank()) {
            RebornFovSavedData.get(serverLevel).removeTarget(teamId, pointId);
        }
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
    }

    public Component getDisplayName() {
        if (customName != null) {
            return customName;
        }
        return Component.translatable("block.rebornfov." + getBlockKind()).withStyle(ChatFormatting.GOLD)
                .append(Component.literal(" #" + worldPosition.getX() + "," + worldPosition.getY() + "," + worldPosition.getZ()));
    }

    public boolean trySetCustomName(Component name) {
        if (name == null || customName != null) {
            return false;
        }
        customName = name.copy();
        if (level instanceof ServerLevel serverLevel) {
            registerSelf(serverLevel);
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
        setChanged();
        return true;
    }

    public abstract String getBlockKind();

    public Component getCustomName() {
        return customName;
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putString("teamId", teamId);
        tag.putString("pointId", pointId);
        if (customName != null) {
            tag.putString("customName", Component.Serializer.toJson(customName));
        }
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        teamId = tag.getString("teamId");
        pointId = tag.getString("pointId");
        if (tag.contains("customName")) {
            customName = Component.Serializer.fromJson(tag.getString("customName"));
        }
    }
}
