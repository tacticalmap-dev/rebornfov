package com.flowingsun.rebornfov.network;

import com.flowingsun.rebornfov.block.entity.FovBlockEntity;
import com.flowingsun.rebornfov.config.SupplyPresetManager;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record SelectPresetPacket(BlockPos pos, String presetId) {
    public static void encode(SelectPresetPacket packet, FriendlyByteBuf buffer) {
        buffer.writeBlockPos(packet.pos);
        buffer.writeUtf(packet.presetId);
    }

    public static SelectPresetPacket decode(FriendlyByteBuf buffer) {
        return new SelectPresetPacket(buffer.readBlockPos(), buffer.readUtf());
    }

    public static void handle(SelectPresetPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null) {
                return;
            }
            if (player.level().getBlockEntity(packet.pos) instanceof FovBlockEntity blockEntity) {
                if (SupplyPresetManager.getPresets().stream().anyMatch(preset -> preset.id().equals(packet.presetId))) {
                    blockEntity.setPresetId(packet.presetId);
                }
            }
        });
        context.setPacketHandled(true);
    }
}
