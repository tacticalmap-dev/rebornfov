package com.flowingsun.rebornfov.registry;

import com.flowingsun.rebornfov.RebornFovMod;
import com.flowingsun.rebornfov.menu.BaseMenu;
import com.flowingsun.rebornfov.menu.FovPresetMenu;
import com.flowingsun.rebornfov.menu.NameEditorMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModMenus {
    public static final DeferredRegister<MenuType<?>> REGISTRY = DeferredRegister.create(ForgeRegistries.MENU_TYPES, RebornFovMod.MOD_ID);

    public static final RegistryObject<MenuType<BaseMenu>> BASE = REGISTRY.register("base", () -> IForgeMenuType.create(BaseMenu::client));
    public static final RegistryObject<MenuType<FovPresetMenu>> FOV_PRESET = REGISTRY.register("fov_preset", () -> IForgeMenuType.create(FovPresetMenu::client));
    public static final RegistryObject<MenuType<NameEditorMenu>> NAME_EDITOR = REGISTRY.register("name_editor", () -> IForgeMenuType.create(NameEditorMenu::client));
}
