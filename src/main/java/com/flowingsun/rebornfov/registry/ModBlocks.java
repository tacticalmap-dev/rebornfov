package com.flowingsun.rebornfov.registry;

import com.flowingsun.rebornfov.RebornFovMod;
import com.flowingsun.rebornfov.block.BaseBlock;
import com.flowingsun.rebornfov.block.FovBlock;
import com.flowingsun.rebornfov.block.TeamBagBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModBlocks {
    public static final DeferredRegister<Block> REGISTRY = DeferredRegister.create(ForgeRegistries.BLOCKS, RebornFovMod.MOD_ID);

    private static BlockBehaviour.Properties nonFullBlock(BlockBehaviour.Properties properties) {
        return properties
                .noOcclusion()
                .isViewBlocking((state, level, pos) -> false)
                .isSuffocating((state, level, pos) -> false)
                .isRedstoneConductor((state, level, pos) -> false);
    }

    public static final RegistryObject<Block> TEAM_BAG = REGISTRY.register("teambag",
            () -> new TeamBagBlock(nonFullBlock(
                    BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_RED).strength(3.0F).sound(SoundType.WOOL).requiresCorrectToolForDrops()
            )));
    public static final RegistryObject<Block> FOV = REGISTRY.register("fov",
            () -> new FovBlock(nonFullBlock(
                    BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(5.0F).sound(SoundType.METAL).requiresCorrectToolForDrops()
            )));
    public static final RegistryObject<Block> BASE = REGISTRY.register("base",
            () -> new BaseBlock(nonFullBlock(
                    BlockBehaviour.Properties.of().mapColor(MapColor.STONE).strength(50.0F, 1200.0F).sound(SoundType.STONE).requiresCorrectToolForDrops()
            )));
}
