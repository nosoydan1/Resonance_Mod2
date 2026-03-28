package com.resonance.mod;

import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Random;

@Mod.EventBusSubscriber(modid = ResonanceMod.MODID, value = Dist.CLIENT)
public class AmbientEffectsHandler {

    private static final Random RANDOM = new Random();
    private static int particleTick = 0;

    // Modificar el color del cielo según la fase
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

        // Mezclar hacia gris oscuro
        event.setRed(event.getRed() * (1 - factor) + 0.2f * factor);
        event.setGreen(event.getGreen() * (1 - factor) + 0.15f * factor);
        event.setBlue(event.getBlue() * (1 - factor) + 0.2f * factor);
    }

    // Modificar la densidad de la niebla según la fase
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

    // Partículas de ceniza en fases avanzadas
    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        int phase = ClientInfectionData.phase;
        if (phase < 4) return;

        particleTick++;
        int interval = switch (phase) {
            case 4 -> 10;
            case 5 -> 5;
            case 6 -> 2;
            default -> 20;
        };

        if (particleTick < interval) return;
        particleTick = 0;

        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null) return;

        // Spawnear partículas de ceniza alrededor del jugador
        for (int i = 0; i < phase; i++) {
            double x = player.getX() + (RANDOM.nextDouble() - 0.5) * 20;
            double y = player.getY() + RANDOM.nextDouble() * 10;
            double z = player.getZ() + (RANDOM.nextDouble() - 0.5) * 20;

            mc.level.addParticle(ParticleTypes.ASH, x, y, z,
                    0, -0.05, 0);
        }
    }
}