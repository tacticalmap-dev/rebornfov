package com.flowingsun.rebornfov.network;

import com.flowingsun.rebornfov.block.entity.FovBlockEntity;
import com.flowingsun.rebornfov.block.entity.TeamNamedBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record RenameTargetPacket(BlockPos pos, String name) {
    public static void encode(RenameTargetPacket packet, FriendlyByteBuf buffer) {
        buffer.writeBlockPos(packet.pos);
        buffer.writeUtf(packet.name);
    }

    public static RenameTargetPacket decode(FriendlyByteBuf buffer) {
        return new RenameTargetPacket(buffer.readBlockPos(), buffer.readUtf());
    }

    public static void handle(RenameTargetPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null) {
                return;
            }
            String trimmedName = packet.name == null ? "" : packet.name.trim();
            if (trimmedName.isEmpty() || trimmedName.length() > 64) {
                return;
            }
            if (player.level().getBlockEntity(packet.pos) instanceof TeamNamedBlockEntity namedBlockEntity) {
                namedBlockEntity.trySetCustomName(Component.literal(trimmedName));
                return;
            }
            if (player.level().getBlockEntity(packet.pos) instanceof FovBlockEntity fovBlockEntity) {
                fovBlockEntity.trySetCustomName(Component.literal(trimmedName));
            }
        });
        context.setPacketHandled(true);
    }
}
