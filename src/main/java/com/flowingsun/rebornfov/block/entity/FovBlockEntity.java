package com.flowingsun.rebornfov.block.entity;

import com.flowingsun.rebornfov.config.RebornFovCommonConfig;
import com.flowingsun.rebornfov.config.SupplyPresetManager;
import com.flowingsun.rebornfov.data.RebornFovSavedData;
import com.flowingsun.rebornfov.data.TeleportTarget;
import com.flowingsun.rebornfov.menu.FovPresetMenu;
import com.flowingsun.rebornfov.registry.ModBlockEntities;
import com.flowingsun.rebornfov.team.TeamResolver;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class FovBlockEntity extends BaseContainerBlockEntity implements WorldlyContainer, PlacementAwareBlockEntity {
    private NonNullList<ItemStack> items = NonNullList.withSize(54, ItemStack.EMPTY);
    private String presetId = RebornFovCommonConfig.defaultPreset;
    private long nextRefreshTick;
    private String teamId = "";
    private String pointId = "";
    private Component customName;

    public FovBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.FOV.get(), pos, state);
    }

    @Override
    public void finishInitialPlacement(LivingEntity placer, ItemStack stack) {
        if (!(placer instanceof ServerPlayer serverPlayer) || !(level instanceof ServerLevel serverLevel)) {
            return;
        }
        if (stack.hasCustomHoverName()) {
            this.customName = stack.getHoverName();
        }
        this.teamId = TeamResolver.resolveTeamId(serverPlayer);
        this.pointId = "fov@" + worldPosition.asShortString() + "@" + serverLevel.dimension().location();
        registerSelf(serverLevel);
        setChanged();
    }

    public void registerSelf(ServerLevel serverLevel) {
        if (teamId.isBlank()) {
            return;
        }
        RebornFovSavedData.get(serverLevel).putTarget(new TeleportTarget(pointId, teamId, "fov", getDisplayName().getString(), serverLevel.dimension().location(), worldPosition));
    }

    @Override
    public void setRemoved() {
        if (!remove && level instanceof ServerLevel serverLevel && !teamId.isBlank() && !pointId.isBlank()) {
            RebornFovSavedData.get(serverLevel).removeTarget(teamId, pointId);
        }
        super.setRemoved();
    }

    public void serverTick() {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }
        if (nextRefreshTick <= 0L) {
            nextRefreshTick = serverLevel.getGameTime() + 20L;
        }
        if (serverLevel.getGameTime() < nextRefreshTick) {
            return;
        }

        SupplyPresetManager.SupplyPreset preset = SupplyPresetManager.getPreset(presetId);
        long soonest = serverLevel.getGameTime() + 20L * 60L;
        for (SupplyPresetManager.SupplyEntry entry : preset.entries()) {
            int amount = Math.max(0, (int) Math.round(entry.amount() * RebornFovCommonConfig.globalAmountMultiplier));
            long interval = Math.max(20L, Math.round(entry.intervalSeconds() * RebornFovCommonConfig.globalIntervalMultiplier * 20.0D));
            soonest = Math.min(soonest, serverLevel.getGameTime() + interval);
            if (amount <= 0) {
                continue;
            }
            ItemStack item = new ItemStack(net.minecraftforge.registries.ForgeRegistries.ITEMS.getValue(entry.itemId()), amount);
            addToInventory(item);
        }
        nextRefreshTick = soonest;
        setChanged();
    }

    private void addToInventory(ItemStack stack) {
        for (int i = 0; i < items.size() && !stack.isEmpty(); i++) {
            ItemStack existing = items.get(i);
            if (existing.isEmpty()) {
                items.set(i, stack.copy());
                stack.setCount(0);
                return;
            }
            if (ItemStack.isSameItemSameTags(existing, stack) && existing.getCount() < existing.getMaxStackSize()) {
                int moved = Math.min(stack.getCount(), existing.getMaxStackSize() - existing.getCount());
                existing.grow(moved);
                stack.shrink(moved);
            }
        }
    }

    public MenuProvider createPresetMenuProvider() {
        return new MenuProvider() {
            @Override
            public Component getDisplayName() {
                return Component.translatable("screen.rebornfov.preset", FovBlockEntity.this.getDisplayName().getString());
            }

            @Override
            public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
                return FovPresetMenu.server(id, inventory, worldPosition, presetId);
            }
        };
    }

    public void setPresetId(String presetId) {
        this.presetId = presetId;
        this.nextRefreshTick = 0L;
        setChanged();
    }

    public String getPresetId() {
        return presetId;
    }

    @Override
    public int[] getSlotsForFace(net.minecraft.core.Direction direction) {
        int[] slots = new int[items.size()];
        for (int i = 0; i < items.size(); i++) {
            slots[i] = i;
        }
        return slots;
    }

    @Override
    public boolean canPlaceItemThroughFace(int slot, ItemStack stack, net.minecraft.core.Direction direction) {
        return true;
    }

    @Override
    public boolean canTakeItemThroughFace(int slot, ItemStack stack, net.minecraft.core.Direction direction) {
        return true;
    }

    @Override
    protected Component getDefaultName() {
        if (customName != null) {
            return customName;
        }
        return Component.translatable("block.rebornfov.fov").withStyle(ChatFormatting.GOLD)
                .append(Component.literal(" #" + worldPosition.getX() + "," + worldPosition.getY() + "," + worldPosition.getZ()));
    }

    @Override
    protected AbstractContainerMenu createMenu(int id, Inventory inventory) {
        return ChestMenu.sixRows(id, inventory, this);
    }

    @Override
    public int getContainerSize() {
        return items.size();
    }

    @Override
    protected NonNullList<ItemStack> getItems() {
        return items;
    }

    @Override
    protected void setItems(NonNullList<ItemStack> items) {
        this.items = items;
    }

    @Override
    public int getMaxStackSize() {
        return 64;
    }

    @Override
    public Component getCustomName() {
        return customName;
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putString("presetId", presetId);
        tag.putLong("nextRefreshTick", nextRefreshTick);
        tag.putString("teamId", teamId);
        tag.putString("pointId", pointId);
        if (customName != null) {
            tag.putString("customName", Component.Serializer.toJson(customName));
        }
        ContainerHelper.saveAllItems(tag, items);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        presetId = tag.getString("presetId");
        if (presetId.isBlank()) {
            presetId = RebornFovCommonConfig.defaultPreset;
        }
        nextRefreshTick = tag.getLong("nextRefreshTick");
        teamId = tag.getString("teamId");
        pointId = tag.getString("pointId");
        if (tag.contains("customName")) {
            customName = Component.Serializer.fromJson(tag.getString("customName"));
        }
        items = NonNullList.withSize(getContainerSize(), ItemStack.EMPTY);
        ContainerHelper.loadAllItems(tag, items);
    }

    @Override
    public void clearContent() {
        items.clear();
    }
}
