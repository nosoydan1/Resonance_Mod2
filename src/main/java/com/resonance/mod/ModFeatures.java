package com.resonance.mod.registry;

import com.resonance.mod.ResonanceMod;
import com.resonance.mod.world.feature.SpikeFeature;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class ModFeatures {
    public static final DeferredRegister<Feature<?>> FEATURES =
            DeferredRegister.create(Registries.FEATURE, ResonanceMod.MODID);

    public static final RegistryObject<Feature<NoneFeatureConfiguration>> SPIKE_FEATURE =
            FEATURES.register("spike_patch", () -> new SpikeFeature(NoneFeatureConfiguration.CODEC));
}