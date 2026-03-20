package com.flowingsun.rebornfov.registry;

import com.flowingsun.rebornfov.RebornFovMod;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {
    public static final DeferredRegister<Item> REGISTRY = DeferredRegister.create(ForgeRegistries.ITEMS, RebornFovMod.MOD_ID);

    public static final RegistryObject<Item> TEAM_BAG = REGISTRY.register("teambag", () -> new BlockItem(ModBlocks.TEAM_BAG.get(), new Item.Properties()));
    public static final RegistryObject<Item> FOV = REGISTRY.register("fov", () -> new BlockItem(ModBlocks.FOV.get(), new Item.Properties()));
    public static final RegistryObject<Item> BASE = REGISTRY.register("base", () -> new BlockItem(ModBlocks.BASE.get(), new Item.Properties()));
}
