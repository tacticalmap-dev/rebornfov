package com.flowingsun.rebornfov.menu;

import com.flowingsun.rebornfov.config.SupplyPresetManager;
import com.flowingsun.rebornfov.registry.ModMenus;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class FovPresetMenu extends AbstractContainerMenu {
    private final BlockPos blockPos;
    private final String selectedPreset;
    private final List<SupplyPresetManager.SupplyPreset> presets;

    private FovPresetMenu(int id, Inventory inventory, BlockPos blockPos, String selectedPreset, List<SupplyPresetManager.SupplyPreset> presets) {
        super(ModMenus.FOV_PRESET.get(), id);
        this.blockPos = blockPos;
        this.selectedPreset = selectedPreset;
        this.presets = presets;
    }

    public static FovPresetMenu server(int id, Inventory inventory, BlockPos blockPos, String selectedPreset) {
        return new FovPresetMenu(id, inventory, blockPos, selectedPreset, SupplyPresetManager.getPresets());
    }

    public static FovPresetMenu client(int id, Inventory inventory, FriendlyByteBuf buffer) {
        BlockPos pos = buffer.readBlockPos();
        String selected = buffer.readUtf();
        int size = buffer.readVarInt();
        List<SupplyPresetManager.SupplyPreset> presets = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            presets.add(new SupplyPresetManager.SupplyPreset(buffer.readUtf(), buffer.readUtf(), List.of()));
        }
        return new FovPresetMenu(id, inventory, pos, selected, presets);
    }

    @Override
    public boolean stillValid(Player player) {
        return player.blockPosition().distSqr(blockPos) <= 64 * 64;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    public BlockPos getBlockPos() {
        return blockPos;
    }

    public String getSelectedPreset() {
        return selectedPreset;
    }

    public List<SupplyPresetManager.SupplyPreset> getPresets() {
        return presets;
    }
}
