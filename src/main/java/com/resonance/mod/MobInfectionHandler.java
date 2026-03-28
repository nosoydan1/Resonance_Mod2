package com.resonance.mod;

import com.resonance.mod.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.animal.*;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.ElderGuardian;
import net.minecraft.world.entity.monster.Skeleton;
import net.minecraft.world.entity.monster.Spider;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;

@Mod.EventBusSubscriber(modid = ResonanceMod.MODID)
public class MobInfectionHandler {

    private static final String TAG_INFECTED = "Resonance_Infected";
    private static final int CHECK_INTERVAL = 100;
    private static final int INFECTION_RADIUS = 16;
    private static int tickCounter = 0;

    public static boolean isInfected(LivingEntity entity) {
        return entity.getPersistentData().getBoolean(TAG_INFECTED);
    }

    public static void infectEntity(LivingEntity entity) {
        entity.getPersistentData().putBoolean(TAG_INFECTED, true);
        entity.setCustomName(Component.literal("§5[Infectado] §r" +
                entity.getName().getString().replace("[Infectado] ", "")));
        entity.setCustomNameVisible(true);

        if (entity instanceof PathfinderMob pathMob) {
            Level level = entity.level();
            if (!level.isClientSide()) {
                InfectionData data = InfectionData.get(level);
                BlockPos nucleus = data.getNucleus();
                if (nucleus != null) {
                    pathMob.goalSelector.addGoal(1,
                            new WalkToNucleusGoal(pathMob, nucleus, 1.0));
                }
            }
        }
    }

    @SubscribeEvent
    public static void onWorldTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        tickCounter++;
        if (tickCounter < CHECK_INTERVAL) return;
        tickCounter = 0;

        event.getServer().getAllLevels().forEach(level -> {
            InfectionData data = InfectionData.get(level);
            BlockPos nucleus = data.getNucleus();
            
            // Búsqueda optimizada: solo en radio alrededor del núcleo si existe
            int searchRadius = 128; // 128 bloques es suficiente para propagación
            
            List<LivingEntity> mobs;
            if (nucleus != null) {
                // Buscar solo cerca del núcleo
                mobs = level.getEntitiesOfClass(
                        LivingEntity.class,
                        new net.minecraft.world.phys.AABB(
                                nucleus.getX() - searchRadius, level.getMinBuildHeight(), nucleus.getZ() - searchRadius,
                                nucleus.getX() + searchRadius, level.getMaxBuildHeight(), nucleus.getZ() + searchRadius
                        ),
                        e -> canBeInfected(e) && !isInfected(e)
                );
            } else {
                // Si no hay núcleo aún, no buscar nada
                return;
            }

            for (LivingEntity mob : mobs) {
                if (isNearCorruption(level, mob.blockPosition())) {
                    infectEntity(mob);
                }
            }
        });
    }

    private static boolean canBeInfected(LivingEntity entity) {
        // Excluir jugadores y mobs del propio mod
        if (entity instanceof net.minecraft.world.entity.player.Player) return false;
        if (entity instanceof net.minecraft.world.entity.monster.Shulker) return false;

        // Bosses con resistencia 75%
        if (entity instanceof WitherBoss
                || entity instanceof ElderGuardian
                || entity instanceof EnderDragon) {
            return Math.random() < 0.25; // 25% probabilidad
        }

        // Todos los demás mobs son infectables
        return entity instanceof net.minecraft.world.entity.Mob;
    }

    private static boolean isNearCorruption(Level level, BlockPos pos) {
        Block corrupted = ModBlocks.CORRUPTED_MINERAL.get();
        Block corruptedOre = ModBlocks.CORRUPTED_MINERAL_ORE.get();

        // Optimizado: Búsqueda por capas verticales para mejor caché
        for (int y = -INFECTION_RADIUS; y <= INFECTION_RADIUS; y++) {
            for (int x = -INFECTION_RADIUS; x <= INFECTION_RADIUS; x++) {
                for (int z = -INFECTION_RADIUS; z <= INFECTION_RADIUS; z++) {
                    Block b = level.getBlockState(pos.offset(x, y, z)).getBlock();
                    if (b == corrupted || b == corruptedOre) return true;
                }
            }
        }
        return false;
    }
}