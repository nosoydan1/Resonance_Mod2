package com.resonance.mod;

import com.resonance.mod.entity.*;
import com.resonance.mod.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.monster.ElderGuardian;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;

import static MobInfectionHandler.INFECTION_RADIUS;
import static MobInfectionHandler.INFECTION_RADIUS;

@Mod.EventBusSubscriber(modid = ResonanceMod.MODID)
public class MobInfectionHandler {

    private static final String TAG_INFECTED = "Resonance_Infected";
    private static final int CHECK_INTERVAL = 100;
    private static final int INFECTION_RADIUS = 16;
    // Radio en el que un infectado defiende el núcleo en vez de asimilarse
    private static final int DEFEND_RADIUS = 8;
    // Radio de detección del jugador — si está cerca atacan primero
    private static final int PLAYER_ATTACK_RADIUS = 20;
    private static int tickCounter = 0;

    public static boolean isInfected(LivingEntity entity) {
        if (entity == null) return false;
        return entity.getPersistentData().getBoolean(TAG_INFECTED);
    }

    public static void infectEntity(LivingEntity entity) {
        // FIX: nunca infectar mobs propios del mod
        if (isModMob(entity)) return;

        entity.getPersistentData().putBoolean(TAG_INFECTED, true);
        entity.setCustomName(Component.literal("§5[Infectado] §r" +
                entity.getName().getString().replace("[Infectado] ", "")));
        entity.setCustomNameVisible(true);

        if (entity instanceof PathfinderMob pathMob) {
            Level level = entity.level();
            if (!level.isClientSide()) {
                InfectionData data = InfectionData.get(level);
                BlockPos nucleus = data.getNucleus();

                // Si el Coloso ya está activo, los infectados solo defienden
                // el área del núcleo, no se asimilan
                if (nucleus != null && !MobSpawnHandler.isColossusSpawned()) {
                    // Prioridad 0: atacar al jugador si está cerca
                    pathMob.goalSelector.addGoal(0,
                            new AttackNearbyPlayerGoal(pathMob, PLAYER_ATTACK_RADIUS));
                    // Prioridad 1: caminar al núcleo
                    pathMob.goalSelector.addGoal(1,
                            new WalkToNucleusGoal(pathMob, nucleus, 1.0));
                } else if (nucleus != null) {
                    // Coloso activo: solo defender radio del núcleo
                    pathMob.goalSelector.addGoal(0,
                            new AttackNearbyPlayerGoal(pathMob, PLAYER_ATTACK_RADIUS));
                }
            }
        }
    }

    /** Devuelve true si la entidad es un mob propio del mod Resonance. */
    public static boolean isModMob(LivingEntity entity) {
        return entity instanceof ChipsEntity
                || entity instanceof MineEntity
                || entity instanceof RaliteEntity
                || entity instanceof AshenKnightEntity
                || entity instanceof MineralGuardianEntity
                || entity instanceof MineralColossusEntity;
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
            if (nucleus == null) return;

            int searchRadius = 128;

            List<LivingEntity> mobs = level.getEntitiesOfClass(
                    LivingEntity.class,
                    new net.minecraft.world.phys.AABB(
                            nucleus.getX() - searchRadius, level.getMinBuildHeight(),
                            nucleus.getZ() - searchRadius,
                            nucleus.getX() + searchRadius, level.getMaxBuildHeight(),
                            nucleus.getZ() + searchRadius
                    ),
                    e -> canBeInfected(e) && !isInfected(e)
            );

            for (LivingEntity mob : mobs) {
                if (isNearCorruption(level, mob.blockPosition())) {
                    infectEntity(mob);
                }
            }
        });
    }

    public static boolean canBeInfected(LivingEntity entity) {
        // Checks básicos
        if (entity == null) return false;
        if (entity instanceof Player) return false;
        if (isModMob(entity)) return false;
        if (entity.isInvulnerable()) return false;

        // Entidades especiales que no pueden infectarse
        if (entity instanceof net.minecraft.world.entity.monster.Shulker) return false;
        if (entity instanceof net.minecraft.world.entity.npc.Villager) return false;
        if (entity instanceof net.minecraft.world.entity.npc.WanderingTrader) return false;
        if (entity instanceof net.minecraft.world.entity.animal.IronGolem) return false;
        if (entity instanceof net.minecraft.world.entity.boss.wither.WitherBoss) return false;
        if (entity instanceof net.minecraft.world.entity.boss.enderdragon.EnderDragon) return false;
        if (entity instanceof net.minecraft.world.entity.monster.ElderGuardian) return false;

        // Para bosses normales: 25% probabilidad
        if (entity.getMaxHealth() > 50) {
            return Math.random() < 0.25;
        }

        return entity instanceof net.minecraft.world.entity.Mob;
    }}

    private static boolean isNearCorruption(Level level, BlockPos pos) {
        Block corrupted = ModBlocks.CORRUPTED_MINERAL.get();
        Block corruptedOre = ModBlocks.CORRUPTED_MINERAL_ORE.get();

        for (int y = -MobInfectionHandler.INFECTION_RADIUS; y <= MobInfectionHandler.INFECTION_RADIUS; y++) {
            for (int x = -MobInfectionHandler.INFECTION_RADIUS; x <= MobInfectionHandler.INFECTION_RADIUS; x++) {
                for (int z = -MobInfectionHandler.INFECTION_RADIUS; z <= MobInfectionHandler.INFECTION_RADIUS; z++) {
                    Block b = level.getBlockState(pos.offset(x, y, z)).getBlock();
                    if (b == corrupted || b == corruptedOre) return true;
                }
            }
        }
        return false;
    }

    // -------------------------------------------------------------------------
    // Goal: atacar al jugador más cercano si está dentro del radio
    // -------------------------------------------------------------------------
    static class AttackNearbyPlayerGoal extends net.minecraft.world.entity.ai.goal.Goal {

        private final PathfinderMob mob;
        private final int radius;
        private Player target;

        public AttackNearbyPlayerGoal(PathfinderMob mob, int radius) {
            this.mob = mob;
            this.radius = radius;
        }

        @Override
        public boolean canUse() {
            target = mob.level().getNearestPlayer(
                    mob.getX(), mob.getY(), mob.getZ(), radius, false);
            return target != null && !target.isSpectator() && !target.isCreative();
        }

        @Override
        public boolean canContinueToUse() {
            return target != null && target.isAlive()
                    && mob.distanceTo(target) <= radius * 1.5;
        }

        @Override
        public void tick() {
            if (target == null) return;
            mob.getLookControl().setLookAt(target, 30f, 30f);
            mob.getNavigation().moveTo(target, 1.2);

            if (mob.distanceTo(target) < 2.0) {
                mob.doHurtTarget(target);
            }
        }

        @Override
        public void stop() {
            target = null;
            mob.getNavigation().stop();
        }
    }