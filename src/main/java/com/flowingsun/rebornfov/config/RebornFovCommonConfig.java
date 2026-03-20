package com.flowingsun.rebornfov.config;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.event.config.ModConfigEvent;

public class RebornFovCommonConfig {
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    public static final ForgeConfigSpec.DoubleValue GLOBAL_AMOUNT_MULTIPLIER = BUILDER
            .comment("Global multiplier applied to every FOV preset entry amount.")
            .defineInRange("globalAmountMultiplier", 1.0D, 0.0D, 64.0D);

    public static final ForgeConfigSpec.DoubleValue GLOBAL_INTERVAL_MULTIPLIER = BUILDER
            .comment("Global multiplier applied to every FOV preset entry interval.")
            .defineInRange("globalIntervalMultiplier", 1.0D, 0.1D, 120.0D);

    public static final ForgeConfigSpec.IntValue MAX_TELEPORT_DISTANCE = BUILDER
            .comment("Maximum distance from a base block to allow teleport list usage.")
            .defineInRange("maxTeleportDistance", 64, 1, 512);

    public static final ForgeConfigSpec.ConfigValue<String> DEFAULT_PRESET = BUILDER
            .comment("Preset name used by a newly placed FOV block.")
            .define("defaultPreset", "default");

    public static final ForgeConfigSpec SPEC = BUILDER.build();

    public static double globalAmountMultiplier = 1.0D;
    public static double globalIntervalMultiplier = 1.0D;
    public static int maxTeleportDistance = 64;
    public static String defaultPreset = "default";

    public static void onConfigReload(ModConfigEvent event) {
        if (event.getConfig().getSpec() != SPEC) {
            return;
        }
        globalAmountMultiplier = GLOBAL_AMOUNT_MULTIPLIER.get();
        globalIntervalMultiplier = GLOBAL_INTERVAL_MULTIPLIER.get();
        maxTeleportDistance = MAX_TELEPORT_DISTANCE.get();
        defaultPreset = DEFAULT_PRESET.get();
    }
}
