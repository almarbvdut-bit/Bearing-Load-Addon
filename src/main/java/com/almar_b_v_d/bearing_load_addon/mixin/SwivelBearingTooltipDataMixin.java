package com.almar_b_v_d.bearing_load_addon.mixin;

import com.almar_b_v_d.bearing_load_addon.BearingStressData;
import com.simibubi.create.content.kinetics.KineticNetwork;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import dev.ryanhcode.sable.api.physics.mass.MassData;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import dev.ryanhcode.sable.sublevel.SubLevel;
import dev.simulated_team.simulated.content.blocks.swivel_bearing.SwivelBearingBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = SwivelBearingBlockEntity.class, remap = false)
public abstract class SwivelBearingTooltipDataMixin
        implements BearingStressData {

    @Unique
    private double bearingload$assemblyMass;

    @Unique
    private float bearingload$assemblyBaseStress;

    @Unique
    private boolean bearingload$assemblyActive;

    @Shadow(remap = false)
    private SubLevel getAttachedSubLevel() {
        return null;
    }

    @Override
    @Unique
    public double bearingload$getAssemblyMass() {
        return bearingload$assemblyMass;
    }

    @Override
    @Unique
    public float bearingload$getAssemblyBaseStress() {
        return bearingload$assemblyBaseStress;
    }

    @Override
    @Unique
    public boolean bearingload$isAssemblyActive() {
        return bearingload$assemblyActive;
    }

    @Override
    @Unique
    public void bearingload$setAssemblyTooltipData(
            double mass,
            float baseStress,
            boolean active
    ) {
        bearingload$assemblyMass = mass;
        bearingload$assemblyBaseStress = baseStress;
        bearingload$assemblyActive = active;
    }

    @Inject(
            method = "tick()V",
            at = @At("TAIL"),
            order = 1100,
            remap = false
    )
    private void bearingload$updateTooltipData(CallbackInfo ci) {
        SwivelBearingBlockEntity bearing =
                (SwivelBearingBlockEntity) (Object) this;

        if (bearing.getLevel() == null || bearing.getLevel().isClientSide) {
            return;
        }

        boolean assembled = bearing.isAssembled();

        double mass = assembled
                ? bearingload$readAttachedMass()
                : 0.0;

        float baseStress = 0.0F;

        if (assembled) {
            KineticBlockEntity extra = bearing.getExtraKinetics();

            Float stress = bearingload$getMemberStress(bearing);

            if (stress == null && extra != null) {
                stress = bearingload$getMemberStress(extra);
            }

            if (stress != null && Float.isFinite(stress) && stress >= 0.0F) {
                baseStress = stress;
            }
        }

        boolean changed =
                Math.abs(bearingload$assemblyMass - mass) > 0.001
                        || Math.abs(bearingload$assemblyBaseStress - baseStress) > 0.001F
                        || bearingload$assemblyActive != assembled;

        if (!changed) {
            return;
        }

        bearingload$setAssemblyTooltipData(
                mass,
                baseStress,
                assembled
        );

        bearing.setChanged();
        bearing.sendData();
    }

    @Unique
    private Float bearingload$getMemberStress(
            KineticBlockEntity member
    ) {
        if (member == null || !member.hasNetwork()) {
            return null;
        }

        try {
            KineticNetwork network = member.getOrCreateNetwork();

            if (network == null || !network.members.containsKey(member)) {
                return null;
            }

            return network.members.get(member);

        } catch (RuntimeException e) {
            return null;
        }
    }

    @Unique
    private double bearingload$readAttachedMass() {
        try {
            SubLevel attached = getAttachedSubLevel();

            if (!(attached instanceof ServerSubLevel serverAttached)) {
                return 0.0;
            }

            MassData massData = serverAttached.getMassTracker();

            if (massData == null || massData.isInvalid()) {
                return 0.0;
            }

            double mass = massData.getMass();

            return Double.isFinite(mass) && mass >= 0.0
                    ? mass
                    : 0.0;

        } catch (RuntimeException e) {
            return 0.0;
        }
    }
}