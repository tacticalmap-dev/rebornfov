package com.flowingsun.rebornfov.menu;

import com.flowingsun.rebornfov.config.RebornFovCommonConfig;
import com.flowingsun.rebornfov.data.RebornFovSavedData;
import com.flowingsun.rebornfov.data.TeleportTarget;
import com.flowingsun.rebornfov.registry.ModMenus;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;

import java.util.ArrayList;
import java.util.List;

public class BaseMenu extends AbstractContainerMenu {
    private final BlockPos basePos;
    private final String teamId;
    private final List<TeleportTarget> targets;

    private BaseMenu(int id, Inventory inventory, BlockPos basePos, String teamId, List<TeleportTarget> targets) {
        super(ModMenus.BASE.get(), id);
        this.basePos = basePos;
        this.teamId = teamId;
        this.targets = targets;
    }

    public static BaseMenu server(int id, Inventory inventory, BlockPos basePos, String teamId) {
        ServerLevel level = (ServerLevel) inventory.player.level();
        return new BaseMenu(id, inventory, basePos, teamId, RebornFovSavedData.get(level).getTargets(teamId));
    }

    public static BaseMenu client(int id, Inventory inventory, FriendlyByteBuf buffer) {
        BlockPos pos = buffer.readBlockPos();
        String teamId = buffer.readUtf();
        int size = buffer.readVarInt();
        List<TeleportTarget> targets = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            targets.add(new TeleportTarget(buffer.readUtf(), teamId, buffer.readUtf(), buffer.readUtf(), buffer.readResourceLocation(), buffer.readBlockPos()));
        }
        return new BaseMenu(id, inventory, pos, teamId, targets);
    }

    @Override
    public boolean stillValid(Player player) {
        return player.blockPosition().distSqr(basePos) <= (long) RebornFovCommonConfig.maxTeleportDistance * RebornFovCommonConfig.maxTeleportDistance;
    }

    @Override
    public net.minecraft.world.item.ItemStack quickMoveStack(Player player, int index) {
        return net.minecraft.world.item.ItemStack.EMPTY;
    }

    @Override
    public void broadcastChanges() {
        super.broadcastChanges();
    }

    public BlockPos getBasePos() {
        return basePos;
    }

    public String getTeamId() {
        return teamId;
    }

    public List<TeleportTarget> getTargets() {
        return targets;
    }
}
