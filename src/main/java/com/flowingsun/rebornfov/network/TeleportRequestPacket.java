package com.flowingsun.rebornfov.network;

import com.flowingsun.rebornfov.config.RebornFovCommonConfig;
import com.flowingsun.rebornfov.data.RebornFovSavedData;
import com.flowingsun.rebornfov.data.TeleportTarget;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.Optional;
import java.util.function.Supplier;

public record TeleportRequestPacket(BlockPos basePos, String teamId, String targetId) {
    public static void encode(TeleportRequestPacket packet, FriendlyByteBuf buffer) {
        buffer.writeBlockPos(packet.basePos);
        buffer.writeUtf(packet.teamId);
        buffer.writeUtf(packet.targetId);
    }

    public static TeleportRequestPacket decode(FriendlyByteBuf buffer) {
        return new TeleportRequestPacket(buffer.readBlockPos(), buffer.readUtf(), buffer.readUtf());
    }

    public static void handle(TeleportRequestPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null || !(player.level() instanceof ServerLevel serverLevel)) {
                return;
            }
            if (player.blockPosition().distSqr(packet.basePos) > (long) RebornFovCommonConfig.maxTeleportDistance * RebornFovCommonConfig.maxTeleportDistance) {
                return;
            }
            Optional<TeleportTarget> target = RebornFovSavedData.get(serverLevel).getTargets(packet.teamId).stream()
                    .filter(entry -> entry.id().equals(packet.targetId))
                    .findFirst();
            if (target.isEmpty()) {
                return;
            }
            ServerLevel destination = player.getServer() == null ? null : player.getServer().getLevel(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.DIMENSION, target.get().dimension()));
            if (destination == null) {
                return;
            }
            BlockPos pos = target.get().pos().above();
            player.teleportTo(destination, pos.getX() + 0.5D, pos.getY() + 0.1D, pos.getZ() + 0.5D, player.getYRot(), player.getXRot());
        });
        context.setPacketHandled(true);
    }
}
