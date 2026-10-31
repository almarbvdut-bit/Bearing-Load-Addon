/**
 * Extends swivel bearing stress calculation with rotating structure mass
 * and keeps the kinetic network stress values consistent.
 */
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
    @Unique
    private static boolean bearingload$isDebugLoggingEnabled() {
        return BearingLoadConfig.ENABLE_DEBUG_LOGGING.get();
    }
    @Shadow(remap = false)
    private SubLevel getAttachedSubLevel() {
        return null;
    }

    @Shadow(remap = false)
    private SubLevel getContainingSubLevel() {
        return null;
    }

    /*
     * Calculate the base stress with the mass contribution.
     * Create applies the theoretical speed to the stress later.
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
        boolean log = bearingload$isDebugLoggingEnabled()
                && (call <= 5 || call % 100 == 0);

        if (level == null || level.isClientSide) {
            return;
        }

        float originalStress = cir.getReturnValue();

        boolean enabled = BearingLoadConfig.ENABLE_MASS_LOAD.get();
        boolean assembled = bearing.isAssembled();

        if (!enabled || !assembled) {
            bearingload$setLastStressApplied(bearing, originalStress);

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

        /*
         * Important:
         * persist exactly the stress value returned to Create.
         * This allows Create to subtract the previous contribution
         * correctly when rebuilding a saved kinetic network.
         */
        bearingload$setLastStressApplied(bearing, result);

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
     * Reconcile the registered network members.
     *
     * The mass contribution must belong to exactly one member:
     * the bearing if registered, otherwise its extra kinetics.
     * The other member retains only its normal Create stress.
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
        boolean log = bearingload$isDebugLoggingEnabled()
                && (call <= 5 || call % 100 == 0);

        boolean enabled = BearingLoadConfig.ENABLE_MASS_LOAD.get();
        boolean assembled = bearing.isAssembled();

        double mass = enabled && assembled
                ? bearingload$readAttachedMass(log)
                : 0.0;

        double multiplier = BearingLoadConfig.LOAD_MULTIPLIER.get();

        float addedMassStress = enabled && assembled
                ? (float) (mass * multiplier)
                : 0.0F;

        KineticBlockEntity extra = null;

        try {
            extra = bearing.getExtraKinetics();
        } catch (RuntimeException e) {
            if (bearingload$isDebugLoggingEnabled()) {
                bearingload$LOGGER.error(
                        "[EXTRA ERROR] Cannot get extra kinetics at {}",
                        bearing.getBlockPos(),
                        e
                );
            }
        }

        boolean bearingIsMember =
                bearingload$isRegisteredMember(bearing);

        boolean extraIsMember =
                extra != null && bearingload$isRegisteredMember(extra);

        boolean updated = false;

        /*
         * Prefer the bearing if it is a network member.
         */
        if (bearingIsMember) {
            updated = bearingload$updateMemberStress(
                    bearing,
                    addedMassStress,
                    "bearing",
                    log
            );
        }

        /*
         * Always reconcile extra kinetics too.
         * Do not add the same mass contribution twice.
         */
        if (extraIsMember) {
            float extraVanillaStress = extra.calculateStressApplied();

            float desiredExtraStress = extraVanillaStress
                    + (bearingIsMember ? 0.0F : addedMassStress);

            boolean extraUpdated = bearingload$updateMemberStress(
                    extra,
                    desiredExtraStress,
                    bearingIsMember
                            ? "extra-kinetics-cleanup"
                            : "extra-kinetics",
                    log
            );

            updated = updated || extraUpdated;
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

            bearingload$LOGGER.info(
                    "[NETWORK DIAGNOSTIC] pos={}, assembled={}, " +
                            "enabled={}, attached={}, containing={}, mass={}, " +
                            "multiplier={}, addedMassStress={}, " +
                            "bearingMember={}, extraClass={}, extraMember={}, " +
                            "updated={}",
                    bearing.getBlockPos(),
                    assembled,
                    enabled,
                    bearingload$className(attached),
                    bearingload$className(containing),
                    mass,
                    multiplier,
                    addedMassStress,
                    bearingIsMember,
                    extra == null ? "null" : extra.getClass().getName(),
                    extraIsMember,
                    updated
            );
        }

        if (!updated && log) {
            bearingload$LOGGER.warn(
                    "[NETWORK NOT UPDATED] pos={}, bearingNetwork={}, " +
                            "bearingMember={}, extraNetwork={}, extraMember={}",
                    bearing.getBlockPos(),
                    bearing.hasNetwork(),
                    bearingIsMember,
                    extra != null && extra.hasNetwork(),
                    extraIsMember
            );
        }
    }

    @Unique
    private boolean bearingload$isRegisteredMember(
            KineticBlockEntity member
    ) {
        if (member == null || !member.hasNetwork()) {
            return false;
        }

        try {
            KineticNetwork network = member.getOrCreateNetwork();

            return network != null
                    && network.members.containsKey(member);
        } catch (RuntimeException e) {
            if (bearingload$isDebugLoggingEnabled()) {
                bearingload$LOGGER.error(
                        "[NETWORK CHECK ERROR] Failed to check member at {}",
                        member.getBlockPos(),
                        e
                );
            }
            return false;
        }
    }

    @Unique
    private boolean bearingload$updateMemberStress(
            KineticBlockEntity member,
            float desiredStress,
            String source,
            boolean log
    ) {
        if (member == null || !member.hasNetwork()) {
            return false;
        }

        try {
            KineticNetwork network = member.getOrCreateNetwork();

            if (network == null || !network.members.containsKey(member)) {
                return false;
            }

            /*
             * Keep the saved value consistent even if the stress value
             * in the network map does not need an update.
             */
            bearingload$setLastStressApplied(member, desiredStress);

            Float previousStress = network.members.get(member);

            if (previousStress == null
                    || Math.abs(previousStress - desiredStress) > 0.01F) {

                network.updateStressFor(member, desiredStress);

                member.sendData();

                if (log) {
                    bearingload$LOGGER.info(
                            "[NETWORK UPDATE] source={}, pos={}, " +
                                    "oldStress={}, newStress={}, speed={}, " +
                                    "theoreticalSpeed={}",
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
        } catch (RuntimeException e) {
            if (bearingload$isDebugLoggingEnabled()) {
                bearingload$LOGGER.error(
                        "[NETWORK UPDATE ERROR] source={}, pos={}",
                        source,
                        member.getBlockPos(),
                        e
                );
            }
            return false;
        }
    }

    @Unique
    private void bearingload$setLastStressApplied(
            KineticBlockEntity member,
            float stress
    ) {
        ((KineticBlockEntityStressAccessor) (Object) member)
                .bearingload$setLastStressApplied(stress);
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

            if (massData == null || massData.isInvalid()) {
                if (log) {
                    bearingload$LOGGER.warn(
                            "[MASS] MassData is null or invalid"
                    );
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
