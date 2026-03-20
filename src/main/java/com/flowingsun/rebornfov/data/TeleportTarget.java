package com.flowingsun.rebornfov.data;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

public record TeleportTarget(String id, String teamId, String type, String name, ResourceLocation dimension, BlockPos pos) {
    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putString("id", id);
        tag.putString("teamId", teamId);
        tag.putString("type", type);
        tag.putString("name", name);
        tag.putString("dimension", dimension.toString());
        tag.putLong("pos", pos.asLong());
        return tag;
    }

    public static TeleportTarget load(CompoundTag tag) {
        return new TeleportTarget(
                tag.getString("id"),
                tag.getString("teamId"),
                tag.getString("type"),
                tag.getString("name"),
                new ResourceLocation(tag.getString("dimension")),
                BlockPos.of(tag.getLong("pos"))
        );
    }
}
