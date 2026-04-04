package com.flowingsun.rebornfov.client;

import com.flowingsun.rebornfov.RebornFovMod;
import com.flowingsun.rebornfov.registry.ModMenus;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@Mod.EventBusSubscriber(modid = RebornFovMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientSetup {
    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            MenuScreens.register(ModMenus.BASE.get(), BaseScreen::new);
            MenuScreens.register(ModMenus.FOV_PRESET.get(), FovPresetScreen::new);
            MenuScreens.register(ModMenus.NAME_EDITOR.get(), NameEditorScreen::new);
        });
    }
}
