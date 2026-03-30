package com.resonance.mod;

import com.resonance.mod.entity.MineralColossusEntity;
import com.resonance.mod.registry.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;
import java.util.Random;

@Mod.EventBusSubscriber(modid = ResonanceMod.MODID)
public class MobSpawnHandler {

    private static final Random RANDOM = new Random();
    private static final int BASE_SPAWN_INTERVAL = 20 * 30;
    private static int spawnTickAccumulator = 0;
    private static boolean colossusDefeated = false;

    // Fase 6: bandera para saber si el Coloso ya fue spawneado esta partida
    private static boolean colossusSpawned = false;
    // Tick counter para el spawn del Coloso (30 segundos después de entrar en fase 6)
    private static int colossusSpawnCountdown = -1;
    private static final int COLOSSUS_SPAWN_DELAY = 20 * 30;

    private static final int[] MAX_MOBS = { 0, 3, 6, 10, 15, 20, 25 };

    // FIX: contador independiente por fase para no perder ticks al cambiar

    public static boolean isColossusSpawned() {
        return colossusSpawned;
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        ServerLevel level = event.getServer().overworld();
        InfectionData data = InfectionData.get(level);
        int phase = data.getPhase();

        // -------------------------------------------------------------------------
        // Fases 1–5: spawn normal
        // -------------------------------------------------------------------------
        spawnTickAccumulator++;  //

        int spawnInterval = switch (phase) {
            case 0, 1 -> BASE_SPAWN_INTERVAL * 2;
            case 2, 3 -> BASE_SPAWN_INTERVAL;
            case 4    -> BASE_SPAWN_INTERVAL * 3 / 4;
            case 5    -> BASE_SPAWN_INTERVAL / 2;
            default   -> BASE_SPAWN_INTERVAL / 3;
        };

        if (spawnTickAccumulator < spawnInterval) return;
        spawnTickAccumulator = 0;

        if (phase < 1) return;

        BlockPos nucleus = data.getNucleus();
        if (nucleus == null) return;

        int activeMobs = countActiveMobs(level, nucleus);
        int maxMobs = (phase < MAX_MOBS.length) ? MAX_MOBS[phase] : MAX_MOBS[MAX_MOBS.length - 1];
        if (activeMobs >= maxMobs) return;

        for (ServerPlayer player : level.players()) {
            BlockPos spawnPos = findSpawnPos(level, nucleus, 20);
            if (spawnPos == null) continue;
            spawnMobForPhase(level, phase, spawnPos);
            break;
        }
    }

    // -------------------------------------------------------------------------
    // Lógica de fase 6
    // -------------------------------------------------------------------------
    private static void handlePhase6(ServerLevel level, InfectionData data) {
        // Si el Coloso ya existe en el mundo, no hacer nada
        if (colossusAlive(level)) return;

        if (!colossusSpawned) {
            // Primera vez que entramos en fase 6: iniciar countdown
            if (colossusSpawnCountdown < 0) {
                colossusSpawnCountdown = COLOSSUS_SPAWN_DELAY;

                // Avisar a todos los jugadores
                level.players().forEach(p -> {
                    p.sendSystemMessage(Component.literal(
                            "§4§l[EL NÚCLEO HA COMPLETADO SU FORMA. EL COLOSO DESPIERTA...]"));
                    p.sendSystemMessage(Component.literal(
                            "§c[Tienes 30 segundos para prepararte]"));
                });
            }

            colossusSpawnCountdown--;

            if (colossusSpawnCountdown <= 0) {
                spawnColossus(level, data);
                colossusSpawned = true;
                colossusSpawnCountdown = -1;
            }
        }
    }

    private static void spawnColossus(ServerLevel level, InfectionData data) {
        BlockPos nucleus = data.getNucleus();
        if (nucleus == null) return;

        MineralColossusEntity colossus = ModEntities.MINERAL_COLOSSUS.get().create(level);
        if (colossus == null) return;

        // Spawnear encima del núcleo
        BlockPos spawnPos = level.getHeightmapPos(
                net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING,
                nucleus
        );

        colossus.moveTo(spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5, 0, 0);
        colossus.finalizeSpawn(level, level.getCurrentDifficultyAt(spawnPos),
                MobSpawnType.MOB_SUMMONED, null, null);
        level.addFreshEntity(colossus);

        level.players().forEach(p -> p.sendSystemMessage(Component.literal(
                "§4§l[EL COLOSO HA DESPERTADO]")));
    }

    private static boolean colossusAlive(ServerLevel level) {
        return !level.getEntitiesOfClass(MineralColossusEntity.class,
                new net.minecraft.world.phys.AABB(
                        level.getWorldBorder().getMinX(), level.getMinBuildHeight(),
                        level.getWorldBorder().getMinZ(),
                        level.getWorldBorder().getMaxX(), level.getMaxBuildHeight(),
                        level.getWorldBorder().getMaxZ()
                )).isEmpty();
    }

    // -------------------------------------------------------------------------
    // Reset cuando el Coloso muere (para permitir re-spawn si se necesita)
    // -------------------------------------------------------------------------
    private static void handlePhase6(ServerLevel level, InfectionData data) {
        // Si el Coloso ya fue derrotado esta sesión, no volver a spawnear
        if (colossusDefeated) return;
        if (colossusAlive(level)) return;

        if (!colossusSpawned) {
            if (colossusSpawnCountdown < 0) {
                colossusSpawnCountdown = COLOSSUS_SPAWN_DELAY;
                level.players().forEach(p -> {
                    p.sendSystemMessage(Component.literal(
                            "§4§l[EL NÚCLEO HA COMPLETADO SU FORMA. EL COLOSO DESPIERTA...]"));
                    p.sendSystemMessage(Component.literal(
                            "§c[Tienes 30 segundos para prepararte]"));
                });
            }

            colossusSpawnCountdown--;

            if (colossusSpawnCountdown <= 0) {
                spawnColossus(level, data);
                colossusSpawned = true;
                colossusSpawnCountdown = -1;
            }
        }
    }

    public static void onColossusDefeated() {
        colossusSpawned = false;
        colossusDefeated = true; // nunca más spawnear en esta sesión
        colossusSpawnCountdown = -1;
    }

    // -------------------------------------------------------------------------
    // Helpers de spawn normal
    // -------------------------------------------------------------------------
    private static int countActiveMobs(ServerLevel level, BlockPos nucleus) {
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
            if (!level.getBlockState(candidate).isAir()) return candidate;
        }
        return null;
    }

    private static void spawnMobForPhase(ServerLevel level, int phase, BlockPos pos) {
        PathfinderMob mob = null;

        switch (phase) {
            case 0, 1 -> mob = ModEntities.CHIPS.get().create(level);
            case 2 -> {
                if (RANDOM.nextFloat() < 0.7f) mob = ModEntities.CHIPS.get().create(level);
                else mob = ModEntities.MINE.get().create(level);
            }
            case 3 -> {
                float r = RANDOM.nextFloat();
                if (r < 0.5f)      mob = ModEntities.CHIPS.get().create(level);
                else if (r < 0.85f) mob = ModEntities.MINE.get().create(level);
                else               mob = ModEntities.RALITE.get().create(level);
            }
            case 4 -> {
                float r = RANDOM.nextFloat();
                if (r < 0.3f)      mob = ModEntities.CHIPS.get().create(level);
                else if (r < 0.6f) mob = ModEntities.MINE.get().create(level);
                else if (r < 0.85f) mob = ModEntities.RALITE.get().create(level);
                else               mob = ModEntities.ASHEN_KNIGHT.get().create(level);
            }
            case 5 -> {
                float r = RANDOM.nextFloat();
                if (r < 0.25f)     mob = ModEntities.MINE.get().create(level);
                else if (r < 0.55f) mob = ModEntities.RALITE.get().create(level);
                else if (r < 0.85f) mob = ModEntities.ASHEN_KNIGHT.get().create(level);
                else               mob = ModEntities.MINERAL_GUARDIAN.get().create(level);
            }
        }

        if (mob == null) return;

        if (RANDOM.nextFloat() < 0.8f) {
            mob.moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, 0, 0);
            mob.finalizeSpawn(level, level.getCurrentDifficultyAt(pos),
                    MobSpawnType.MOB_SUMMONED, null, null);
            level.addFreshEntity(mob);
        }
    }
}