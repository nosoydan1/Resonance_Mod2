package com.resonance.mod;

import com.resonance.mod.network.InfectionSyncPacket;
import com.resonance.mod.network.NetworkHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.List;

@Mod.EventBusSubscriber(modid = ResonanceMod.MODID)
public class NucleusReachHandler {

    private static final int CHECK_INTERVAL = 40; // cada 2 segundos
    private static final int REACH_RADIUS = 3; // distancia para considerar que llegó
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

            // Buscar mobs infectados cerca del núcleo
            List<LivingEntity> infected = level.getEntitiesOfClass(
                    LivingEntity.class,
                    new net.minecraft.world.phys.AABB(
                            nucleus.getX() - REACH_RADIUS,
                            nucleus.getY() - REACH_RADIUS,
                            nucleus.getZ() - REACH_RADIUS,
                            nucleus.getX() + REACH_RADIUS,
                            nucleus.getY() + REACH_RADIUS,
                            nucleus.getZ() + REACH_RADIUS
                    ),
                    e -> MobInfectionHandler.isInfected(e) && e instanceof Mob
            );

            List<LivingEntity> toRemove = new ArrayList<>();

            for (LivingEntity mob : infected) {
                int points = getPointsForMob(mob);
                data.addPoints(points);
                toRemove.add(mob);

                // Notificar a jugadores cercanos
                level.players().forEach(p -> {
                    if (p.distanceTo(mob) < 50) {
                        p.sendSystemMessage(Component.literal(
                                "§5[+" + points + " pts] " +
                                        mob.getName().getString() + " §5fue asimilado por el Núcleo"
                        ));
                    }
                });

                // Eliminar mob asimilado
                mob.remove(net.minecraft.world.entity.Entity.RemovalReason.DISCARDED);
            }

            if (!toRemove.isEmpty()) {
                NetworkHandler.sendToAllClients(
                        new InfectionSyncPacket(data.getPoints(), data.getPhase())
                );
            }
        });
    }

    private static int getPointsForMob(LivingEntity entity) {
        String name = entity.getType().toShortString();
        return switch (name) {
            // Pequeños - 2 pts
            case "chicken", "cod", "salmon", "tropical_fish",
                 "bat", "bee", "frog", "tadpole" -> 2;

            // Estándar - 8 pts
            case "zombie", "skeleton", "pig", "cow", "sheep",
                 "wolf", "fox", "panda", "dolphin", "turtle",
                 "villager", "enderman", "phantom", "spider",
                 "rabbit", "squid", "glow_squid" -> 8;

            // Grande/Hostil - 20 pts
            case "creeper", "iron_golem", "ravager", "piglin",
                 "hoglin", "warden", "evoker", "vindicator",
                 "ghast", "slime", "magma_cube", "blaze",
                 "witch", "pillager", "guardian" -> 20;

            // Bosses
            case "wither" -> 150;
            case "elder_guardian" -> 200;
            case "ender_dragon" -> 400;

            default -> 8;
        };
    }
}