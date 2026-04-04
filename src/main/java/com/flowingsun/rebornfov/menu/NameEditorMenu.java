package com.flowingsun.rebornfov.menu;

import com.flowingsun.rebornfov.registry.ModMenus;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

public class NameEditorMenu extends AbstractContainerMenu {
    private final BlockPos blockPos;
    private final String blockLabel;
    private final String initialName;

    private NameEditorMenu(int id, Inventory inventory, BlockPos blockPos, String blockLabel, String initialName) {
        super(ModMenus.NAME_EDITOR.get(), id);
        this.blockPos = blockPos;
        this.blockLabel = blockLabel;
        this.initialName = initialName;
    }

    public static NameEditorMenu server(int id, Inventory inventory, BlockPos blockPos, String blockLabel, String initialName) {
        return new NameEditorMenu(id, inventory, blockPos, blockLabel, initialName);
    }

    public static NameEditorMenu client(int id, Inventory inventory, FriendlyByteBuf buffer) {
        BlockPos blockPos = buffer.readBlockPos();
        String blockLabel = buffer.readUtf();
        String initialName = buffer.readUtf();
        return new NameEditorMenu(id, inventory, blockPos, blockLabel, initialName);
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

    public String getBlockLabel() {
        return blockLabel;
    }

    public String getInitialName() {
        return initialName;
    }
}
