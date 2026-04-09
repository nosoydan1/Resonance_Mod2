package com.resonance.mod.registry;

import com.resonance.mod.ResonanceMod;
import com.resonance.mod.entity.AshenKnightEntity;
import com.resonance.mod.entity.ChipsEntity;
import com.resonance.mod.entity.MineEntity;
import com.resonance.mod.entity.MineralColossusEntity;
import com.resonance.mod.entity.MineralGuardianEntity;
import com.resonance.mod.entity.MineralParticlesProjectileEntity;
import com.resonance.mod.entity.RaliteEntity;
import com.resonance.mod.entity.EchoEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModEntities {

    public static final DeferredRegister<EntityType<?>> ENTITIES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, ResonanceMod.MODID);

    public static final RegistryObject<EntityType<ChipsEntity>> CHIPS =
            ENTITIES.register("chips",
                    () -> EntityType.Builder.<ChipsEntity>of(ChipsEntity::new, MobCategory.MONSTER)
                            .sized(0.8f, 0.5f)
                            .build(new ResourceLocation(ResonanceMod.MODID, "chips").toString()));

    public static final RegistryObject<EntityType<MineEntity>> MINE =
            ENTITIES.register("mine",
                    () -> EntityType.Builder.<MineEntity>of(MineEntity::new, MobCategory.MONSTER)
                            .sized(0.6f, 2.0f)
                            .build(new ResourceLocation(ResonanceMod.MODID, "mine").toString()));

    public static final RegistryObject<EntityType<RaliteEntity>> RALITE =
            ENTITIES.register("ralite",
                    () -> EntityType.Builder.<RaliteEntity>of(RaliteEntity::new, MobCategory.MONSTER)
                            .sized(1.2f, 1.2f)
                            .build(new ResourceLocation(ResonanceMod.MODID, "ralite").toString()));

    public static final RegistryObject<EntityType<AshenKnightEntity>> ASHEN_KNIGHT =
            ENTITIES.register("ashen_knight",
                    () -> EntityType.Builder.<AshenKnightEntity>of(AshenKnightEntity::new, MobCategory.MONSTER)
                            .sized(0.6f, 1.8f)
                            .build(new ResourceLocation(ResonanceMod.MODID, "ashen_knight").toString()));

    public static final RegistryObject<EntityType<MineralGuardianEntity>> MINERAL_GUARDIAN =
            ENTITIES.register("mineral_guardian",
                    () -> EntityType.Builder.<MineralGuardianEntity>of(MineralGuardianEntity::new, MobCategory.MONSTER)
                            .sized(1.5f, 1.8f)
                            .build(new ResourceLocation(ResonanceMod.MODID, "mineral_guardian").toString()));

    public static final RegistryObject<EntityType<MineralColossusEntity>> MINERAL_COLOSSUS =
            ENTITIES.register("mineral_colossus",
                    () -> EntityType.Builder.<MineralColossusEntity>of(MineralColossusEntity::new, MobCategory.MONSTER)
                            .sized(3.0f, 4.0f) // Muy grande
                            .build(new ResourceLocation(ResonanceMod.MODID, "mineral_colossus").toString()));

    public static final RegistryObject<EntityType<EchoEntity>> ECHO = ENTITIES.register("echo",
            () -> EntityType.Builder.of(EchoEntity::new, MobCategory.MONSTER)
                    .sized(0.6f, 0.6f)
                    .build(new ResourceLocation(ResonanceMod.MODID, "echo").toString()));

    public static final RegistryObject<EntityType<MineralParticlesProjectileEntity>> MINERAL_PARTICLES_PROJECTILE =
            ENTITIES.register("mineral_particles_projectile",
                    () -> EntityType.Builder.<MineralParticlesProjectileEntity>of(
                                    MineralParticlesProjectileEntity::new, MobCategory.MISC)
                            .sized(0.25f, 0.25f)
                            .clientTrackingRange(4)
                            .updateInterval(10)
                            .build(new ResourceLocation(ResonanceMod.MODID,
                                    "mineral_particles_projectile").toString()));
}