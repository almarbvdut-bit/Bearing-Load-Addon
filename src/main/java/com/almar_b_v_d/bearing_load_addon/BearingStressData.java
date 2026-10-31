package com.almar_b_v_d.bearing_load_addon;

/**
 * Defines synchronized assembly mass and stress data
 * used by the swivel bearing tooltip.
 */
public interface BearingStressData {

    double bearingload$getAssemblyMass();

    float bearingload$getAssemblyBaseStress();

    boolean bearingload$isAssemblyActive();

    void bearingload$setAssemblyTooltipData(
            double mass,
            float baseStress,
            boolean active
    );
}