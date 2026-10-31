package com.almar_b_v_d.bearing_load_addon.mixin;

import com.almar_b_v_d.bearing_load_addon.config.BearingLoadConfig;
import com.almar_b_v_d.bearing_load_addon.BearingStressData;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import dev.simulated_team.simulated.util.extra_kinetics.ExtraKinetics.ExtraKineticsBlockEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.text.NumberFormat;
import java.util.List;

/**
 * Synchronizes custom bearing tooltip data and displays
 * assembly mass and stress in Create Goggles.
 */

@Mixin(value = KineticBlockEntity.class, remap = false)
public abstract class BearingStressTooltipMixin {

    private static final String MASS_TAG =
            "BearingLoadAddonAssemblyMass";

    private static final String STRESS_TAG =
            "BearingLoadAddonAssemblyBaseStress";

    private static final String ACTIVE_TAG =
            "BearingLoadAddonAssemblyActive";

    @Inject(
            method = "write(Lnet/minecraft/nbt/CompoundTag;Lnet/minecraft/core/HolderLookup$Provider;Z)V",
            at = @At("TAIL"),
            remap = false
    )
    private void bearingload$writeTooltipData(
            CompoundTag tag,
            HolderLookup.Provider registries,
            boolean clientPacket,
            CallbackInfo ci
    ) {
        if (!((Object) this instanceof BearingStressData data)) {
            return;
        }

        tag.putDouble(
                MASS_TAG,
                data.bearingload$getAssemblyMass()
        );

        tag.putFloat(
                STRESS_TAG,
                data.bearingload$getAssemblyBaseStress()
        );

        tag.putBoolean(
                ACTIVE_TAG,
                data.bearingload$isAssemblyActive()
        );
    }

    @Inject(
            method = "read(Lnet/minecraft/nbt/CompoundTag;Lnet/minecraft/core/HolderLookup$Provider;Z)V",
            at = @At("RETURN"),
            remap = false
    )
    private void bearingload$readTooltipData(
            CompoundTag tag,
            HolderLookup.Provider registries,
            boolean clientPacket,
            CallbackInfo ci
    ) {
        if (!((Object) this instanceof BearingStressData data)) {
            return;
        }

        data.bearingload$setAssemblyTooltipData(
                tag.getDouble(MASS_TAG),
                tag.getFloat(STRESS_TAG),
                tag.getBoolean(ACTIVE_TAG)
        );
    }

    @Inject(
            method = "addToGoggleTooltip(Ljava/util/List;Z)Z",
            at = @At("RETURN"),
            cancellable = true,
            remap = false
    )
    private void bearingload$addAssemblyStressTooltip(
            List<Component> tooltip,
            boolean isPlayerSneaking,
            CallbackInfoReturnable<Boolean> cir
    ) {
        if (!BearingLoadConfig.ENABLE_MASS_LOAD.get()) {
            return;
        }

        if (!((Object) this instanceof ExtraKineticsBlockEntity extraBlock)) {
            return;
        }

        if (!(extraBlock.getParentBlockEntity()
                instanceof BearingStressData data)) {
            return;
        }

        KineticBlockEntity extra =
                (KineticBlockEntity) (Object) this;

        Level level = extra.getLevel();

        if (level == null || !level.isClientSide) {
            return;
        }

        if (cir.getReturnValue()) {
            tooltip.add(Component.empty());
        }

        if (!data.bearingload$isAssemblyActive()) {
            tooltip.add(
                    Component.translatable(
                            "bearing_load_addon.gui.goggles.bearing_inactive"
                    ).withStyle(ChatFormatting.RED)
            );

            cir.setReturnValue(true);
            return;
        }

        double mass = data.bearingload$getAssemblyMass();

        float baseStress =
                data.bearingload$getAssemblyBaseStress();

        float speed = Math.abs(extra.getTheoreticalSpeed());

        float totalStress = baseStress * speed;

        if (!Double.isFinite(mass)
                || !Double.isFinite(totalStress)
                || !Float.isFinite(speed)) {
            return;
        }

        NumberFormat format = NumberFormat.getNumberInstance();
        format.setMaximumFractionDigits(2);

        tooltip.add(
                Component.translatable(
                        "bearing_load_addon.gui.goggles.assembly_mass",
                        format.format(mass)
                ).withStyle(ChatFormatting.GRAY)
        );

        tooltip.add(
                Component.translatable(
                        "bearing_load_addon.gui.goggles.assembly_stress",
                        format.format(totalStress),
                        format.format(speed)
                ).withStyle(ChatFormatting.AQUA)
        );

        cir.setReturnValue(true);
    }
}