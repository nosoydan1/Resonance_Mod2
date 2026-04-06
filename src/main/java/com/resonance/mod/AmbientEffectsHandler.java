package com.resonance.mod;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Random;

@Mod.EventBusSubscriber(modid = ResonanceMod.MODID)
public class AmbientEffectsHandler {

    private static final Random RANDOM = new Random();

    // Contadores server-side
    private static int lightningTick = 0;
    private static int coughTick = 0;

    // Contadores client-side (campos estáticos separados por dist)
    private static int particleTick = 0;

    // -------------------------------------------------------------------------
    // SERVER: relámpagos en fase 5–6
    // -------------------------------------------------------------------------
    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        event.getServer().getAllLevels().forEach(level -> {
            ServerLevel serverLevel = (ServerLevel) level;

            InfectionData data = InfectionData.get(serverLevel);
            int phase = data.getPhase();
            BlockPos nucleus = data.getNucleus();
            if (nucleus == null) return;

            // Relámpagos en fase 5+
            if (phase >= 5) {
                lightningTick++;
                // Intervalo: fase 5 = cada ~15s, fase 6 = cada ~8s
                int interval = phase >= 6 ? 20 * 8 : 20 * 15;
                // Añadir variabilidad aleatoria
                interval += RANDOM.nextInt(20 * 5);

                if (lightningTick >= interval) {
                    lightningTick = 0;
                    spawnLightningNearNucleus(serverLevel, nucleus);
                }
            }

            // Tos del jugador sin Breathing Mask en fase 4+
            if (phase >= 4) {
                coughTick++;
                int coughInterval = 20 * 12; // cada 12 segundos base
                coughInterval += RANDOM.nextInt(20 * 8); // variabilidad

                if (coughTick >= coughInterval) {
                    coughTick = 0;
                    triggerCoughForNearbyPlayers(serverLevel, nucleus, phase);
                }
            }
        });
    }

    private static void spawnLightningNearNucleus(ServerLevel level, BlockPos nucleus) {
        // Rayo en posición aleatoria cerca del núcleo
        int range = 30;
        int dx = RANDOM.nextInt(range * 2) - range;
        int dz = RANDOM.nextInt(range * 2) - range;

        BlockPos strikeBase = level.getHeightmapPos(
                Heightmap.Types.MOTION_BLOCKING,
                nucleus.offset(dx, 0, dz)
        );

        net.minecraft.world.entity.LightningBolt bolt =
                net.minecraft.world.entity.EntityType.LIGHTNING_BOLT.create(level);
        if (bolt == null) return;

        bolt.moveTo(strikeBase.getX() + 0.5, strikeBase.getY(), strikeBase.getZ() + 0.5);
        bolt.setVisualOnly(false); // rayo real, puede encender fuego y dañar
        level.addFreshEntity(bolt);
    }

    private static void triggerCoughForNearbyPlayers(ServerLevel level, BlockPos nucleus, int phase) {
        int radius = 64; // radio alrededor del núcleo donde aplica la tos
        level.getEntitiesOfClass(Player.class,
                new net.minecraft.world.phys.AABB(
                        nucleus.getX() - radius, level.getMinBuildHeight(), nucleus.getZ() - radius,
                        nucleus.getX() + radius, level.getMaxBuildHeight(), nucleus.getZ() + radius
                )
        ).forEach(player -> {
            // No toser si lleva Breathing Mask equipada en el slot de casco
            net.minecraft.world.item.ItemStack helmet = player.getItemBySlot(
                    net.minecraft.world.entity.EquipmentSlot.HEAD);
            boolean hasMask = helmet.getItem() instanceof com.resonance.mod.item.BreathingMaskItem;
            if (hasMask) return;

            // Sonido de tos (usamos ENTITY_PLAYER_HURT como placeholder hasta tener sonido custom)
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.PLAYER_HURT, SoundSource.PLAYERS, 0.5f, 0.6f);

            // En fase 5+ la tos también causa daño mínimo (1 HP)
            if (phase >= 5) {
                player.hurt(level.damageSources().magic(), 1.0f);
            }
        });
    }

    // -------------------------------------------------------------------------
    // CLIENT: color de cielo, niebla y partículas de ceniza
    // -------------------------------------------------------------------------
    @Mod.EventBusSubscriber(modid = ResonanceMod.MODID, value = Dist.CLIENT)
    public static class ClientEffects {

        private static int particleTick = 0;

        @SubscribeEvent
        public static void onFogColor(ViewportEvent.ComputeFogColor event) {
            int phase = ClientInfectionData.phase;
            if (phase < 3) return;

            float factor = switch (phase) {
                case 3 -> 0.15f;
                case 4 -> 0.30f;
                case 5 -> 0.50f;
                case 6 -> 0.70f;
                default -> 0f;
            };

            event.setRed(event.getRed() * (1 - factor) + 0.2f * factor);
            event.setGreen(event.getGreen() * (1 - factor) + 0.15f * factor);
            event.setBlue(event.getBlue() * (1 - factor) + 0.2f * factor);
        }

        @SubscribeEvent
        public static void onFogDensity(ViewportEvent.RenderFog event) {
            int phase = ClientInfectionData.phase;
            if (phase < 3) return;

            float density = switch (phase) {
                case 3 -> 0.003f;
                case 4 -> 0.006f;
                case 5 -> 0.012f;
                case 6 -> 0.020f;
                default -> 0f;
            };

            event.setNearPlaneDistance(event.getNearPlaneDistance() - density * 100);
            event.setFarPlaneDistance(event.getFarPlaneDistance() - density * 500);
            event.setCanceled(true);
        }

        @@SubscribeEvent
        public static void onClientTick(TickEvent.ClientTickEvent event) {
            if (event.phase != TickEvent.Phase.END) return;

            int phase = ClientInfectionData.phase;
            if (phase < 4) return;

            particleTick++;

            // Reducir frecuencia: máximo 1 spawn cada 2 ticks
            int interval = switch (phase) {
                case 4 -> 10;
                case 5 -> 7;
                case 6 -> 4;
                default -> 20;
            };

            if (particleTick < interval) return;
            particleTick = 0;

            Minecraft mc = Minecraft.getInstance();
            Player player = mc.player;
            if (player == null || mc.level == null) return;

            // Limitar número de partículas
            int particleCount = Math.min(phase, 3); // Máximo 3 partículas por tick
            for (int i = 0; i < particleCount; i++) {
                double x = player.getX() + (Math.random() - 0.5) * 20;
                double y = player.getY() + Math.random() * 10;
                double z = player.getZ() + (Math.random() - 0.5) * 20;
                mc.level.addParticle(net.minecraft.core.particles.ParticleTypes.ASH, x, y, z, 0, -0.05, 0);
            }
        }
    }
}