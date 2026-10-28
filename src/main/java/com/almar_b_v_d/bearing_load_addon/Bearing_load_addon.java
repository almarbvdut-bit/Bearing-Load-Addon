package com.almar_b_v_d.bearing_load_addon;

import com.almar_b_v_d.bearing_load_addon.config.BearingLoadConfig;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;

@Mod(Bearing_load_addon.MODID)
public class Bearing_load_addon {
    public static final String MODID = "bearing_load_addon";

    public Bearing_load_addon(IEventBus modBus, ModContainer modContainer) {
        modContainer.registerConfig(ModConfig.Type.SERVER, BearingLoadConfig.SPEC);
    }
}