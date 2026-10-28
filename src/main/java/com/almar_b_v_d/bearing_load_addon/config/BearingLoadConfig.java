package com.almar_b_v_d.bearing_load_addon.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public class BearingLoadConfig {
    public static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.BooleanValue ENABLE_MASS_LOAD =
            BUILDER.comment("Включить расчёт нагрузки от массы конструкции")
                    .define("enableMassLoad", true);

    public static final ModConfigSpec.DoubleValue LOAD_MULTIPLIER =
            BUILDER.comment("Коэффициент умножения: Формула = Масса × Скорость × этот коэффициент")
                    .defineInRange("loadMultiplier", 0.4, 0.0, 100.0);

    public static final ModConfigSpec SPEC = BUILDER.build();
}