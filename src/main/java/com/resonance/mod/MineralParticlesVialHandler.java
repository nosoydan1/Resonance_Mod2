package com.resonance.mod;

import com.resonance.mod.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Mineral Particles Vial — obtención (GDD §6.4):
 * Cuando un creeper infectado explota deja una "estela" temporal de partículas
 * en el suelo. Si un jugador tiene un Glass Bottle en la mano y se acerca
 * a esa zona en los siguientes 10 segundos, obtiene un Mineral Particles Vial.
 *
 * La estela se registra cuando InfectedCreeperHandler procesa la explosión.
 * Este handler verifica la proximidad del jugador cada tick.
 */
@Mod.EventBusSubscriber(modid = ResonanceMod.MODID)
public class MineralParticlesVialHandler {

    // Registro de estelas activas: posición + ticks restantes
    private static final Set<ActiveTrail> activeTrails = new HashSet<>();

    // Radio en bloques para recoger la estela
    private static final double COLLECT_RADIUS = 3.0;
    // Duración de la estela: 10 segundos
    private static final int TRAIL_DURATION_TICKS = 200;

    /**
     * Registra una estela nueva. Llamado desde InfectedCreeperHandler.
     */
    public static void registerTrail(BlockPos pos) {
        activeTrails.add(new ActiveTrail(pos, TRAIL_DURATION_TICKS));
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (activeTrails.isEmpty()) return;

        event.getServer().getAllLevels().forEach(level -> {
            // ✅ level ya es ServerLevel, no necesita instanceof
            ServerLevel serverLevel = level;

            Iterator<ActiveTrail> it = activeTrails.iterator();
            while (it.hasNext()) {
                ActiveTrail trail = it.next();
                trail.ticksLeft--;

                if (trail.ticksLeft <= 0) {
                    it.remove();
                    continue;
                }

                // Partículas visuales cada 10 ticks para que se vea la estela
                if (trail.ticksLeft % 10 == 0) {
                    serverLevel.sendParticles(
                            net.minecraft.core.particles.ParticleTypes.WITCH,
                            trail.pos.getX() + 0.5,
                            trail.pos.getY() + 0.3,
                            trail.pos.getZ() + 0.5,
                            5, 0.5, 0.1, 0.5, 0.02);
                    serverLevel.sendParticles(
                            net.minecraft.core.particles.ParticleTypes.DRAGON_BREATH,
                            trail.pos.getX() + 0.5,
                            trail.pos.getY() + 0.1,
                            trail.pos.getZ() + 0.5,
                            3, 0.3, 0.05, 0.3, 0.01);
                }

                // Verificar jugadores cerca con Glass Bottle en mano
                for (Player player : serverLevel.players()) {
                    double dist = player.blockPosition().distSqr(trail.pos);
                    if (dist > COLLECT_RADIUS * COLLECT_RADIUS) continue;

                    ItemStack mainHand = player.getMainHandItem();
                    ItemStack offHand = player.getOffhandItem();

                    ItemStack bottle = ItemStack.EMPTY;

                    if (mainHand.getItem() == net.minecraft.world.item.Items.GLASS_BOTTLE) {
                        bottle = mainHand;
                    } else if (offHand.getItem() == net.minecraft.world.item.Items.GLASS_BOTTLE) {
                        bottle = offHand;
                    }

                    if (bottle.isEmpty()) continue;

                    if (!player.getAbilities().instabuild) {
                        bottle.shrink(1);
                    }

                    ItemStack vial = new ItemStack(ModItems.MINERAL_PARTICLES_VIAL.get(), 1);
                    player.getInventory().add(vial);

                    player.sendSystemMessage(
                            net.minecraft.network.chat.Component.literal(
                                    "§5Capturas las partículas minerales en el frasco."));

                    it.remove();
                    break;
                }
            }
        });
    }

    // -------------------------------------------------------------------------
    static class ActiveTrail {
        final BlockPos pos;
        int ticksLeft;

        ActiveTrail(BlockPos pos, int ticksLeft) {
            this.pos = pos;
            this.ticksLeft = ticksLeft;
        }
    }
}