package com.resonance.mod;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.resonance.mod.network.InfectionSyncPacket;
import com.resonance.mod.network.NetworkHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.List;

@Mod.EventBusSubscriber(modid = ResonanceMod.MODID)
public class NucleusReachHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(NucleusReachHandler.class);
    private static final int CHECK_INTERVAL = 40;
    private static final int REACH_RADIUS = 3;
    // Radio de defensa — mobs infectados aquí defienden el núcleo
    private static final int DEFEND_RADIUS = 12;
    private static int tickCounter = 0;

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        tickCounter++;
        if (tickCounter < CHECK_INTERVAL) return;
        tickCounter = 0;

        event.getServer().getAllLevels().forEach(level -> {
            InfectionData data = InfectionData.get(level);
            BlockPos nucleus = data.getNucleus();
            if (nucleus == null) return;

            boolean colossusActive = MobSpawnHandler.isColossusSpawned();

            List<LivingEntity> nearNucleus = level.getEntitiesOfClass(
                    LivingEntity.class,
                    new AABB(
                            nucleus.getX() - REACH_RADIUS,
                            nucleus.getY() - REACH_RADIUS,
                            nucleus.getZ() - REACH_RADIUS,
                            nucleus.getX() + REACH_RADIUS,
                            nucleus.getY() + REACH_RADIUS,
                            nucleus.getZ() + REACH_RADIUS
                    ),
                    e -> MobInfectionHandler.isInfected(e)
                            && e instanceof Mob
                            // FIX: nunca asimilar mobs del mod
                            && !MobInfectionHandler.isModMob(e)
            );

            List<LivingEntity> toRemove = new ArrayList<>();

            for (LivingEntity mob : nearNucleus) {
                // Si el Coloso ya está activo, no asimilar — el mob defiende
                if (colossusActive) continue;

                int points = getPointsForMob(mob);
                data.addPoints(points);
                toRemove.add(mob);

                level.players().forEach(p -> {
                    if (p.distanceTo(mob) < 50) {
                        p.sendSystemMessage(Component.literal(
                                "§5[+" + points + " pts] " +
                                        mob.getName().getString() +
                                        " §5fue asimilado por el Núcleo"));
                    }
                });

                mob.remove(net.minecraft.world.entity.Entity.RemovalReason.DISCARDED);
            }

            if (!toRemove.isEmpty()) {
                NetworkHandler.sendToAllClients(
                        new InfectionSyncPacket(data.getPoints(), data.getPhase()));
            }
        });
    }

    private static int getPointsForMob(LivingEntity entity) {
        String name = entity.getType().toShortString();
        return switch (name) {
            case "chicken", "cod", "salmon", "tropical_fish",
                 "bat", "bee", "frog", "tadpole" -> 2;
            case "zombie", "skeleton", "pig", "cow", "sheep",
                 "wolf", "fox", "panda", "dolphin", "turtle",
                 "villager", "enderman", "phantom", "spider",
                 "rabbit", "squid", "glow_squid" -> 8;
            case "creeper", "iron_golem", "ravager", "piglin",
                 "hoglin", "warden", "evoker", "vindicator",
                 "ghast", "slime", "magma_cube", "blaze",
                 "witch", "pillager", "guardian" -> 20;
            case "wither" -> 150;
            case "elder_guardian" -> 200;
            case "ender_dragon" -> 400;
            default -> 8;
        };
    }
}