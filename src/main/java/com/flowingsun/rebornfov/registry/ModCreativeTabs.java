package com.flowingsun.rebornfov.registry;

import com.flowingsun.rebornfov.RebornFovMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> REGISTRY = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, RebornFovMod.MOD_ID);

    public static final RegistryObject<CreativeModeTab> MAIN = REGISTRY.register("main", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.rebornfov.main"))
            .icon(() -> new ItemStack(ModItems.BASE.get()))
            .displayItems((parameters, output) -> {
                output.accept(ModItems.BASE.get());
                output.accept(ModItems.TEAM_BAG.get());
                output.accept(ModItems.FOV.get());
            })
            .build());
}
