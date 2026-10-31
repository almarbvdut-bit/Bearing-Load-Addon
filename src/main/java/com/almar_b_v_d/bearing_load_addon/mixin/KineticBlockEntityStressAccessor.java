/**
 * Provides access to Create's internal applied stress value.
 */

package com.almar_b_v_d.bearing_load_addon.mixin;

import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = KineticBlockEntity.class, remap = false)
public interface KineticBlockEntityStressAccessor {

    @Accessor(value = "lastStressApplied", remap = false)
    void bearingload$setLastStressApplied(float stress);

    @Accessor(value = "lastStressApplied", remap = false)
    float bearingload$getLastStressApplied();
}