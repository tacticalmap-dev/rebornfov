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
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
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
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class FovBlockEntity extends BaseContainerBlockEntity implements WorldlyContainer, PlacementAwareBlockEntity {

    private NonNullList<ItemStack> items = NonNullList.withSize(54, ItemStack.EMPTY);
    private String presetId = RebornFovCommonConfig.defaultPreset;
    private final Map<String, Long> nextEntryRefreshTickByItem = new HashMap<>();
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
        this.pointId = "fov@" + worldPosition.toShortString() + "@" + serverLevel.dimension().location();
        registerSelf(serverLevel);
        setChanged();
    }

    public void registerSelf(ServerLevel serverLevel) {
        if (teamId.isBlank()) return;

        RebornFovSavedData.get(serverLevel).putTarget(
                new TeleportTarget(pointId, teamId, "fov",
                        getDisplayName().getString(),
                        serverLevel.dimension().location(),
                        worldPosition)
        );
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

    public void serverTick() {
        if (!(level instanceof ServerLevel serverLevel)) return;
        long now = serverLevel.getGameTime();
        SupplyPresetManager.SupplyPreset preset = SupplyPresetManager.getPreset(presetId);
        Set<String> validItemKeys = new HashSet<>();
        boolean changed = false;

        for (SupplyPresetManager.SupplyEntry entry : preset.entries()) {
            String itemKey = entry.itemId().toString();
            validItemKeys.add(itemKey);

            long interval = Math.max(20L,
                    Math.round(entry.intervalSeconds() * RebornFovCommonConfig.globalIntervalMultiplier * 20.0D));
            long nextRefreshTick = nextEntryRefreshTickByItem.getOrDefault(itemKey, now + interval);
            if (!nextEntryRefreshTickByItem.containsKey(itemKey)) {
                nextEntryRefreshTickByItem.put(itemKey, nextRefreshTick);
                changed = true;
                continue;
            }
            if (now < nextRefreshTick) {
                continue;
            }

            int amount = Math.max(0, (int) Math.round(entry.amount() * RebornFovCommonConfig.globalAmountMultiplier));
            if (amount <= 0) continue;
            Item item = net.minecraftforge.registries.ForgeRegistries.ITEMS.getValue(entry.itemId());
            if (item == null || item == Items.AIR) {
                continue;
            }
            int maxCount = entry.maxCount();
            if (maxCount >= 0) {
                int currentCount = countStoredItem(item);
                int remain = maxCount - currentCount;
                if (remain <= 0) {
                    nextEntryRefreshTickByItem.put(itemKey, now + interval);
                    changed = true;
                    continue;
                }
                amount = Math.min(amount, remain);
            }
            if (amount <= 0) {
                nextEntryRefreshTickByItem.put(itemKey, now + interval);
                changed = true;
                continue;
            }

            addToInventory(new ItemStack(item, amount));
            nextEntryRefreshTickByItem.put(itemKey, now + interval);
            changed = true;
        }

        if (nextEntryRefreshTickByItem.keySet().removeIf(key -> !validItemKeys.contains(key))) {
            changed = true;
        }
        if (changed) {
            setChanged();
        }
    }

    private void addToInventory(ItemStack stack) {
        for (int i = 0; i < items.size() && !stack.isEmpty(); i++) {
            ItemStack existing = items.get(i);

            if (existing.isEmpty()) {
                items.set(i, stack.copy());
                stack.setCount(0);
                return;
            }

            if (ItemStack.isSameItemSameTags(existing, stack)
                    && existing.getCount() < existing.getMaxStackSize()) {

                int moved = Math.min(stack.getCount(),
                        existing.getMaxStackSize() - existing.getCount());

                existing.grow(moved);
                stack.shrink(moved);
            }
        }
    }

    private int countStoredItem(Item item) {
        int total = 0;
        for (ItemStack stack : items) {
            if (!stack.isEmpty() && stack.is(item)) {
                total += stack.getCount();
            }
        }
        return total;
    }

    public MenuProvider createPresetMenuProvider() {
        return new MenuProvider() {
            @Override
            public Component getDisplayName() {
                return Component.translatable("screen.rebornfov.preset",
                        FovBlockEntity.this.getDisplayName().getString());
            }

            @Override
            public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
                return FovPresetMenu.server(id, inventory, worldPosition, presetId);
            }
        };
    }

    public void setPresetId(String presetId) {
        this.presetId = presetId;
        this.nextEntryRefreshTickByItem.clear();
        setChanged();
    }

    public String getPresetId() {
        return presetId;
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

    // ✅ 1.20 必须实现
    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    @Override
    public int[] getSlotsForFace(net.minecraft.core.Direction direction) {
        int[] slots = new int[items.size()];
        for (int i = 0; i < items.size(); i++) slots[i] = i;
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
        if (customName != null) return customName;

        return Component.translatable("block.rebornfov.fov")
                .withStyle(ChatFormatting.GOLD)
                .append(Component.literal(" #"
                        + worldPosition.getX() + ","
                        + worldPosition.getY() + ","
                        + worldPosition.getZ()));
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
    public boolean isEmpty() {
        for (ItemStack item : items) {
            if (!item.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public ItemStack getItem(int slot) {
        return slot >= 0 && slot < items.size() ? items.get(slot) : ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        ItemStack removed = ContainerHelper.removeItem(items, slot, amount);
        if (!removed.isEmpty()) {
            setChanged();
        }
        return removed;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        return ContainerHelper.takeItem(items, slot);
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        if (slot < 0 || slot >= items.size()) {
            return;
        }
        items.set(slot, stack);
        if (stack.getCount() > getMaxStackSize()) {
            stack.setCount(getMaxStackSize());
        }
        setChanged();
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
        tag.putString("teamId", teamId);
        tag.putString("pointId", pointId);

        if (customName != null) {
            tag.putString("customName", Component.Serializer.toJson(customName));
        }

        ListTag refreshTag = new ListTag();
        for (Map.Entry<String, Long> entry : nextEntryRefreshTickByItem.entrySet()) {
            CompoundTag entryTag = new CompoundTag();
            entryTag.putString("item", entry.getKey());
            entryTag.putLong("nextTick", entry.getValue());
            refreshTag.add(entryTag);
        }
        tag.put("entryRefresh", refreshTag);
        ContainerHelper.saveAllItems(tag, items);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);

        presetId = tag.getString("presetId");
        if (presetId.isBlank()) {
            presetId = RebornFovCommonConfig.defaultPreset;
        }

        teamId = tag.getString("teamId");
        pointId = tag.getString("pointId");

        if (tag.contains("customName")) {
            customName = Component.Serializer.fromJson(tag.getString("customName"));
        }

        nextEntryRefreshTickByItem.clear();
        ListTag refreshTag = tag.getList("entryRefresh", Tag.TAG_COMPOUND);
        for (Tag rawTag : refreshTag) {
            if (!(rawTag instanceof CompoundTag entryTag)) {
                continue;
            }
            String item = entryTag.getString("item");
            if (!item.isBlank()) {
                nextEntryRefreshTickByItem.put(item, entryTag.getLong("nextTick"));
            }
        }

        items = NonNullList.withSize(getContainerSize(), ItemStack.EMPTY);
        ContainerHelper.loadAllItems(tag, items);
    }

    @Override
    public void clearContent() {
        items.clear();
    }
}
