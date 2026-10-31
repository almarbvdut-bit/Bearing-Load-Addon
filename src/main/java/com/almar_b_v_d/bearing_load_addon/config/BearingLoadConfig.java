package com.almar_b_v_d.bearing_load_addon.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public class BearingLoadConfig {

    public static final ModConfigSpec.Builder BUILDER =
            new ModConfigSpec.Builder();

    public static final ModConfigSpec.BooleanValue ENABLE_MASS_LOAD =
            BUILDER.comment("Enable stress calculation from structure mass")
                    .define("enableMassLoad", true);

    public static final ModConfigSpec.DoubleValue LOAD_MULTIPLIER =
            BUILDER.comment("Load multiplier. Formula: mass × speed × multiplier")
                    .defineInRange("loadMultiplier", 1, 0.0, 100.0);

    public static final ModConfigSpec.BooleanValue ENABLE_DEBUG_LOGGING =
            BUILDER.comment("Enable diagnostic logging for the addon")
                    .define("enableDebugLogging", false);

    public static final ModConfigSpec SPEC = BUILDER.build();
}