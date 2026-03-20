package com.flowingsun.rebornfov.registry;

import com.flowingsun.rebornfov.RebornFovMod;
import com.flowingsun.rebornfov.block.entity.FovBlockEntity;
import com.flowingsun.rebornfov.block.entity.TeamBagBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> REGISTRY = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, RebornFovMod.MOD_ID);

    public static final RegistryObject<BlockEntityType<TeamBagBlockEntity>> TEAM_BAG = REGISTRY.register("teambag",
            () -> BlockEntityType.Builder.of(TeamBagBlockEntity::new, ModBlocks.TEAM_BAG.get()).build(null));
    public static final RegistryObject<BlockEntityType<FovBlockEntity>> FOV = REGISTRY.register("fov",
            () -> BlockEntityType.Builder.of(FovBlockEntity::new, ModBlocks.FOV.get()).build(null));
}
