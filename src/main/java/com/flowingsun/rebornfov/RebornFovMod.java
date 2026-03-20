package com.flowingsun.rebornfov;

import com.flowingsun.rebornfov.config.RebornFovCommonConfig;
import com.flowingsun.rebornfov.config.SupplyPresetManager;
import com.flowingsun.rebornfov.network.ModNetwork;
import com.flowingsun.rebornfov.registry.ModBlockEntities;
import com.flowingsun.rebornfov.registry.ModBlocks;
import com.flowingsun.rebornfov.registry.ModCreativeTabs;
import com.flowingsun.rebornfov.registry.ModItems;
import com.flowingsun.rebornfov.registry.ModMenus;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(RebornFovMod.MOD_ID)
public class RebornFovMod {
    public static final String MOD_ID = "rebornfov";

    public RebornFovMod() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        ModBlocks.REGISTRY.register(modBus);
        ModItems.REGISTRY.register(modBus);
        ModBlockEntities.REGISTRY.register(modBus);
        ModMenus.REGISTRY.register(modBus);
        ModCreativeTabs.REGISTRY.register(modBus);

        modBus.addListener(this::commonSetup);
        modBus.addListener(RebornFovCommonConfig::onConfigReload);
        modBus.addListener(SupplyPresetManager::onConfigReload);
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, RebornFovCommonConfig.SPEC);

        MinecraftForge.EVENT_BUS.register(SupplyPresetManager.class);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            ModNetwork.register();
            SupplyPresetManager.ensurePresetDirectory();
        });
    }
}
