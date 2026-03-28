package com.resonance.mod;

import com.resonance.mod.registry.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;
import java.util.Random;

@Mod.EventBusSubscriber(modid = ResonanceMod.MODID)
public class MobSpawnHandler {

    private static final Random RANDOM = new Random();
    private static final int BASE_SPAWN_INTERVAL = 20 * 30; // 30 segundos base
    private static int tickCounter = 0;

    // Límite de mobs del mod activos simultáneamente por fase (progresivo)
    // Etapa 0: 0, 1: 3, 2: 6, 3: 10, 4: 15, 5: 20, 6+: 25
    private static final int[] MAX_MOBS = { 0, 3, 6, 10, 15, 20, 25 };

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        tickCounter++;
        ServerLevel level = event.getServer().overworld();
        InfectionData data = InfectionData.get(level);
        int phase = data.getPhase();

        // Intervalo dinámico: más frecuente en fases altas (más agresivo)
        // Etapa 0-1: 60s, 2-3: 30s, 4: 22.5s, 5: 15s, 6+: 10s
        int spawnInterval = switch (phase) {
            case 0, 1 -> BASE_SPAWN_INTERVAL * 2; // 60 segundos
            case 2, 3 -> BASE_SPAWN_INTERVAL;     // 30 segundos
            case 4 -> BASE_SPAWN_INTERVAL * 3 / 4; // 22.5 segundos
            case 5 -> BASE_SPAWN_INTERVAL / 2;    // 15 segundos
            default -> BASE_SPAWN_INTERVAL / 3;   // 10 segundos (muy agresivo)
        };

        if (tickCounter < spawnInterval) return;
        tickCounter = 0;

        if (phase < 1) return;

        BlockPos nucleus = data.getNucleus();
        if (nucleus == null) return;

        // Contar mobs del mod activos
        int activeMobs = countActiveMobs(level, nucleus);
        int maxMobs = (phase < MAX_MOBS.length) ? MAX_MOBS[phase] : MAX_MOBS[MAX_MOBS.length - 1];

        if (activeMobs >= maxMobs) return;

        // Spawnear cerca del núcleo
        for (ServerPlayer player : level.players()) {
            BlockPos spawnPos = findSpawnPos(level, nucleus, 20);
            if (spawnPos == null) continue;

            spawnMobForPhase(level, phase, spawnPos);
            break;
        }
    }

    private static int countActiveMobs(ServerLevel level, BlockPos nucleus) {
        // Optimizado: Buscar solo en un radio de 64 bloques alrededor del nucleo
        int searchRadius = 64;
        return level.getEntitiesOfClass(PathfinderMob.class,
                new net.minecraft.world.phys.AABB(
                        nucleus.getX() - searchRadius, level.getMinBuildHeight(), nucleus.getZ() - searchRadius,
                        nucleus.getX() + searchRadius, level.getMaxBuildHeight(), nucleus.getZ() + searchRadius
                ),
                e -> e.getType() == ModEntities.CHIPS.get()
                        || e.getType() == ModEntities.MINE.get()
                        || e.getType() == ModEntities.RALITE.get()
                        || e.getType() == ModEntities.ASHEN_KNIGHT.get()
                        || e.getType() == ModEntities.MINERAL_GUARDIAN.get()
        ).size();
    }

    private static BlockPos findSpawnPos(ServerLevel level, BlockPos center, int radius) {
        for (int attempts = 0; attempts < 10; attempts++) {
            int dx = RANDOM.nextInt(radius * 2) - radius;
            int dz = RANDOM.nextInt(radius * 2) - radius;
            BlockPos candidate = level.getHeightmapPos(
                    net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING,
                    center.offset(dx, 0, dz)
            );
            if (!level.getBlockState(candidate).isAir()) {
                return candidate;
            }
        }
        return null;
    }

    private static void spawnMobForPhase(ServerLevel level, int phase, BlockPos pos) {
        // SISTEMA DE ETAPAS PROGRESIVO:
        // Etapa 0-1: Solo Chips (100%)
        // Etapa 2: Chips (70%) + Mine (30%)
        // Etapa 3: Chips (50%) + Mine (35%) + Ralite (15%)
        // Etapa 4: Chips (30%) + Mine (30%) + Ralite (25%) + Ashen Knight (15%)
        // Etapa 5: Mine (25%) + Ralite (30%) + Ashen Knight (30%) + Mineral Guardian (15%)
        // Etapa 6+: Ralite (20%) + Ashen Knight (40%) + Mineral Guardian (40%)

        PathfinderMob mob = null;

        // Sistema de etapas progresivo con probabilidades aleatorias
        switch (phase) {
            case 0, 1 -> {
                // Etapa 0-1: Solo Chips (100% probabilidad)
                mob = ModEntities.CHIPS.get().create(level);
            }
            case 2 -> {
                // Etapa 2: Chips (70%) o Mine (30%)
                if (RANDOM.nextFloat() < 0.7f) {
                    mob = ModEntities.CHIPS.get().create(level);
                } else {
                    mob = ModEntities.MINE.get().create(level);
                }
            }
            case 3 -> {
                // Etapa 3: Chips (50%), Mine (35%), Ralite (15%)
                float rand = RANDOM.nextFloat();
                if (rand < 0.5f) {
                    mob = ModEntities.CHIPS.get().create(level);
                } else if (rand < 0.85f) {
                    mob = ModEntities.MINE.get().create(level);
                } else {
                    mob = ModEntities.RALITE.get().create(level);
                }
            }
            case 4 -> {
                // Etapa 4: Chips (30%), Mine (30%), Ralite (25%), Ashen Knight (15%)
                float rand = RANDOM.nextFloat();
                if (rand < 0.3f) {
                    mob = ModEntities.CHIPS.get().create(level);
                } else if (rand < 0.6f) {
                    mob = ModEntities.MINE.get().create(level);
                } else if (rand < 0.85f) {
                    mob = ModEntities.RALITE.get().create(level);
                } else {
                    mob = ModEntities.ASHEN_KNIGHT.get().create(level);
                }
            }
            case 5 -> {
                // Etapa 5: Mine (25%), Ralite (30%), Ashen Knight (30%), Mineral Guardian (15%)
                float rand = RANDOM.nextFloat();
                if (rand < 0.25f) {
                    mob = ModEntities.MINE.get().create(level);
                } else if (rand < 0.55f) {
                    mob = ModEntities.RALITE.get().create(level);
                } else if (rand < 0.85f) {
                    mob = ModEntities.ASHEN_KNIGHT.get().create(level);
                } else {
                    mob = ModEntities.MINERAL_GUARDIAN.get().create(level);
                }
            }
            default -> { // Etapa 6+
                // Etapa final: Ralite (20%), Ashen Knight (40%), Mineral Guardian (40%)
                float rand = RANDOM.nextFloat();
                if (rand < 0.2f) {
                    mob = ModEntities.RALITE.get().create(level);
                } else if (rand < 0.6f) {
                    mob = ModEntities.ASHEN_KNIGHT.get().create(level);
                } else {
                    mob = ModEntities.MINERAL_GUARDIAN.get().create(level);
                }
            }
        }

        if (mob == null) return;

        // Añadir variabilidad de spawn (como en Minecraft vanilla)
        // 80% de probabilidad de que el spawn sea exitoso
        if (RANDOM.nextFloat() < 0.8f) {
            mob.moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, 0, 0);
            mob.finalizeSpawn(level, level.getCurrentDifficultyAt(pos),
                    MobSpawnType.MOB_SUMMONED, null, null);
            level.addFreshEntity(mob);
        }
    }
}