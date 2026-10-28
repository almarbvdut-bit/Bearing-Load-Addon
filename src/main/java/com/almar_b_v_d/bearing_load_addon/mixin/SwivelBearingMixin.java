
package com.almar_b_v_d.bearing_load_addon.mixin;

import com.almar_b_v_d.bearing_load_addon.config.BearingLoadConfig;
import com.simibubi.create.content.kinetics.KineticNetwork;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import dev.ryanhcode.sable.api.physics.mass.MassData;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import dev.ryanhcode.sable.sublevel.SubLevel;
import dev.simulated_team.simulated.content.blocks.swivel_bearing.SwivelBearingBlockEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.concurrent.atomic.AtomicInteger;

@Mixin(value = SwivelBearingBlockEntity.class, remap = false)
public abstract class SwivelBearingMixin {

    @Unique
    private static final Logger bearingload$LOGGER =
            LoggerFactory.getLogger("BearingLoadAddon");

    @Unique
    private static final AtomicInteger bearingload$TICK_CALLS =
            new AtomicInteger();

    @Unique
    private static final AtomicInteger bearingload$STRESS_CALLS =
            new AtomicInteger();

    @Shadow(remap = false)
    private SubLevel getAttachedSubLevel() {
        return null;
    }

    @Shadow(remap = false)
    private SubLevel getContainingSubLevel() {
        return null;
    }

    /*
     * Adds mass-based stress to the bearing's base stress.
     *
     * Create multiplies base stress by the absolute theoretical
     * speed when calculating network stress, so speed is not
     * multiplied here a second time.
     */
    @Inject(
            method = "calculateStressApplied()F",
            at = @At("RETURN"),
            cancellable = true,
            remap = false
    )
    private void bearingload$addMassStress(
            CallbackInfoReturnable<Float> cir
    ) {
        SwivelBearingBlockEntity bearing =
                (SwivelBearingBlockEntity) (Object) this;

        Level level = ((BlockEntity) (Object) this).getLevel();

        int call = bearingload$STRESS_CALLS.incrementAndGet();
        boolean log = call <= 5 || call % 100 == 0;

        if (level == null || level.isClientSide) {
            if (log) {
                bearingload$LOGGER.info(
                        "[CALCULATE] Skipped on client or Level is null"
                );
            }
            return;
        }

        float originalStress = cir.getReturnValue();

        boolean enabled = BearingLoadConfig.ENABLE_MASS_LOAD.get();
        boolean assembled = bearing.isAssembled();

        if (!enabled || !assembled) {
            if (log) {
                bearingload$LOGGER.info(
                        "[CALCULATE] pos={}, enabled={}, assembled={}, " +
                                "originalStress={}, result=unchanged",
                        bearing.getBlockPos(),
                        enabled,
                        assembled,
                        originalStress
                );
            }
            return;
        }

        double mass = bearingload$readAttachedMass(log);
        double multiplier = BearingLoadConfig.LOAD_MULTIPLIER.get();

        float addedStress = (float) (mass * multiplier);
        float result = originalStress + addedStress;

        if (log) {
            bearingload$LOGGER.info(
                    "[CALCULATE] pos={}, mass={}, multiplier={}, " +
                            "originalStress={}, addedStress={}, result={}",
                    bearing.getBlockPos(),
                    mass,
                    multiplier,
                    originalStress,
                    addedStress,
                    result
            );
        }

        cir.setReturnValue(result);
    }

    /*
     * Recheck the network after assembly and during normal ticking.
     * Mass may become available after the kinetic network has first
     * registered its members.
     */
    @Inject(
            method = "tick()V",
            at = @At("TAIL"),
            remap = false
    )
    private void bearingload$refreshNetworkStress(CallbackInfo ci) {
        SwivelBearingBlockEntity bearing =
                (SwivelBearingBlockEntity) (Object) this;

        Level level = ((BlockEntity) (Object) this).getLevel();

        if (level == null || level.isClientSide) {
            return;
        }

        int call = bearingload$TICK_CALLS.incrementAndGet();
        boolean log = call <= 5 || call % 100 == 0;

        boolean enabled = BearingLoadConfig.ENABLE_MASS_LOAD.get();
        boolean assembled = bearing.isAssembled();

        double mass = enabled && assembled
                ? bearingload$readAttachedMass(log)
                : 0.0;

        double multiplier = BearingLoadConfig.LOAD_MULTIPLIER.get();

        // SwivelBearingBlockEntity.calculateStressApplied() normally
        // returns zero, so its desired base stress is mass * multiplier.
        float addedMassStress = enabled && assembled
                ? (float) (mass * multiplier)
                : 0.0F;

        KineticBlockEntity extra = null;

        try {
            extra = bearing.getExtraKinetics();
        } catch (RuntimeException e) {
            bearingload$LOGGER.error(
                    "[EXTRA ERROR] Cannot get extra kinetics at {}",
                    bearing.getBlockPos(),
                    e
            );
        }

        boolean bearingHasNetwork = bearing.hasNetwork();
        boolean extraHasNetwork = extra != null && extra.hasNetwork();

        boolean bearingIsMember = false;
        boolean extraIsMember = false;

        Float bearingPreviousStress = null;
        Float extraPreviousStress = null;

        boolean updated = false;

        /*
         * Prefer the actual bearing if it is a registered member.
         */
        if (bearingHasNetwork) {
            try {
                KineticNetwork network = bearing.getOrCreateNetwork();

                bearingIsMember = network.members.containsKey(bearing);

                if (bearingIsMember) {
                    bearingPreviousStress = network.members.get(bearing);

                    updated = bearingload$updateMemberStress(
                            bearing,
                            addedMassStress,
                            "bearing",
                            log
                    );
                }
            } catch (RuntimeException e) {
                bearingload$LOGGER.error(
                        "[NETWORK ERROR] Failed to update bearing network at {}",
                        bearing.getBlockPos(),
                        e
                );
            }
        }

        /*
         * If the bearing itself is not a member, try its extra kinetics.
         * Keep the extra block's normal Create stress and add mass stress.
         */
        if (!updated && extra != null && extraHasNetwork) {
            try {
                KineticNetwork network = extra.getOrCreateNetwork();

                extraIsMember = network.members.containsKey(extra);

                if (extraIsMember) {
                    extraPreviousStress = network.members.get(extra);

                    float extraVanillaStress = extra.calculateStressApplied();

                    float desiredExtraStress =
                            extraVanillaStress + addedMassStress;

                    updated = bearingload$updateMemberStress(
                            extra,
                            desiredExtraStress,
                            "extra-kinetics",
                            log
                    );
                }
            } catch (RuntimeException e) {
                bearingload$LOGGER.error(
                        "[NETWORK ERROR] Failed to update extra kinetics network at {}",
                        bearing.getBlockPos(),
                        e
                );
            }
        }

        if (log) {
            SubLevel attached = null;
            SubLevel containing = null;

            try {
                attached = getAttachedSubLevel();
                containing = getContainingSubLevel();
            } catch (RuntimeException e) {
                bearingload$LOGGER.error(
                        "[SUBLEVEL ERROR] pos={}",
                        bearing.getBlockPos(),
                        e
                );
            }

            float speed = 0.0F;
            float theoreticalSpeed = 0.0F;
            String extraStatus;

            if (extra == null) {
                extraStatus = "NULL";
            } else {
                try {
                    speed = extra.getSpeed();
                    theoreticalSpeed = extra.getTheoreticalSpeed();
                    extraStatus = "OK";
                } catch (RuntimeException e) {
                    extraStatus = "ERROR";
                    bearingload$LOGGER.error(
                            "[SPEED ERROR] pos={}",
                            bearing.getBlockPos(),
                            e
                    );
                }
            }

            bearingload$LOGGER.info(
                    "[NETWORK DIAGNOSTIC] pos={}, assembled={}, " +
                            "enabled={}, attached={}, containing={}, mass={}, " +
                            "multiplier={}, addedMassStress={}, bearingNetwork={}, " +
                            "bearingMember={}, bearingPreviousStress={}, " +
                            "extraClass={}, extraNetwork={}, extraMember={}, " +
                            "extraPreviousStress={}, extraStatus={}, speed={}, " +
                            "theoreticalSpeed={}, updated={}",
                    bearing.getBlockPos(),
                    assembled,
                    enabled,
                    bearingload$className(attached),
                    bearingload$className(containing),
                    mass,
                    multiplier,
                    addedMassStress,
                    bearingHasNetwork,
                    bearingIsMember,
                    bearingPreviousStress,
                    extra == null ? "null" : extra.getClass().getName(),
                    extraHasNetwork,
                    extraIsMember,
                    extraPreviousStress,
                    extraStatus,
                    speed,
                    theoreticalSpeed,
                    updated
            );
        }

        if (!updated && log) {
            bearingload$LOGGER.warn(
                    "[NETWORK NOT UPDATED] pos={}, bearingNetwork={}, " +
                            "bearingMember={}, extraNetwork={}, extraMember={}. " +
                            "Neither object was updated because it was not " +
                            "a registered member of a kinetic network.",
                    bearing.getBlockPos(),
                    bearingHasNetwork,
                    bearingIsMember,
                    extraHasNetwork,
                    extraIsMember
            );
        }
    }

    @Unique
    private boolean bearingload$updateMemberStress(
            KineticBlockEntity member,
            float desiredStress,
            String source,
            boolean log
    ) {
        if (!member.hasNetwork()) {
            return false;
        }

        KineticNetwork network = member.getOrCreateNetwork();

        if (!network.members.containsKey(member)) {
            return false;
        }

        Float previousStress = network.members.get(member);

        if (previousStress == null
                || Math.abs(previousStress - desiredStress) > 0.01F) {

            network.updateStressFor(member, desiredStress);

            if (log) {
                bearingload$LOGGER.info(
                        "[NETWORK UPDATE] source={}, pos={}, oldStress={}, " +
                                "newStress={}, speed={}, theoreticalSpeed={}",
                        source,
                        member.getBlockPos(),
                        previousStress,
                        desiredStress,
                        member.getSpeed(),
                        member.getTheoreticalSpeed()
                );
            }
        }

        return true;
    }

    @Unique
    private double bearingload$readAttachedMass(boolean log) {
        SubLevel attached;

        try {
            attached = getAttachedSubLevel();
        } catch (RuntimeException e) {
            if (log) {
                bearingload$LOGGER.error(
                        "[MASS ERROR] Failed to get attached sublevel",
                        e
                );
            }
            return 0.0;
        }

        if (attached == null) {
            if (log) {
                bearingload$LOGGER.info(
                        "[MASS] Attached sublevel is null"
                );
            }
            return 0.0;
        }

        if (!(attached instanceof ServerSubLevel serverAttached)) {
            if (log) {
                bearingload$LOGGER.info(
                        "[MASS] Attached sublevel is {}, not ServerSubLevel",
                        attached.getClass().getName()
                );
            }
            return 0.0;
        }

        try {
            MassData massData = serverAttached.getMassTracker();

            if (massData == null) {
                if (log) {
                    bearingload$LOGGER.warn("[MASS] MassData is null");
                }
                return 0.0;
            }

            if (massData.isInvalid()) {
                if (log) {
                    bearingload$LOGGER.warn("[MASS] MassData is invalid");
                }
                return 0.0;
            }

            double mass = massData.getMass();

            if (!Double.isFinite(mass) || mass < 0.0) {
                if (log) {
                    bearingload$LOGGER.warn(
                            "[MASS] Invalid mass value: {}",
                            mass
                    );
                }
                return 0.0;
            }

            if (log) {
                bearingload$LOGGER.info(
                        "[MASS OK] source=attached, mass={}",
                        mass
                );
            }

            return mass;
        } catch (RuntimeException e) {
            if (log) {
                bearingload$LOGGER.error(
                        "[MASS ERROR] Failed to read MassData",
                        e
                );
            }
            return 0.0;
        }
    }

    @Unique
    private static String bearingload$className(SubLevel subLevel) {
        return subLevel == null
                ? "null"
                : subLevel.getClass().getName();
    }
}
