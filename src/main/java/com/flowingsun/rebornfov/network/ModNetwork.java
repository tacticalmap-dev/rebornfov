package com.flowingsun.rebornfov.network;

import com.flowingsun.rebornfov.RebornFovMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public class ModNetwork {
    private static final String PROTOCOL = "1";
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(RebornFovMod.MOD_ID, "main"),
            () -> PROTOCOL,
            PROTOCOL::equals,
            PROTOCOL::equals
    );

    private static int packetId;

    public static void register() {
        CHANNEL.registerMessage(packetId++, TeleportRequestPacket.class, TeleportRequestPacket::encode, TeleportRequestPacket::decode, TeleportRequestPacket::handle);
        CHANNEL.registerMessage(packetId++, SelectPresetPacket.class, SelectPresetPacket::encode, SelectPresetPacket::decode, SelectPresetPacket::handle);
    }
}
