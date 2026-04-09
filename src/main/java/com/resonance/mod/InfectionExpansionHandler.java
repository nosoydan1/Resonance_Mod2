package com.resonance.mod;

import com.resonance.mod.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Random;

@Mod.EventBusSubscriber(modid = ResonanceMod.MODID)
public class InfectionExpansionHandler {

    private static final Random RANDOM = new Random();

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        // Obtener el nivel principal (overworld)
        ServerLevel level = event.getServer().overworld();
        InfectionData data = InfectionData.get(level);
        if (data.getNucleus() == null) return; // No hay núcleo, aún no hay infección

        long currentTick = level.getServer().getTickCount();

        // 1. Expansión del radio (cada intervalo)
        if (data.tryExpandRadius(currentTick,
                Config.radialExpansionIntervalSeconds,
                Config.radialIncrementBlocks,
                Config.radialMaxRadius)) {
            ResonanceMod.LOGGER.info("Radio expandido a " + data.getCurrentRadius());
            // El radio acaba de aumentar, puedes disparar un efecto visual o sonido
        }

        // 2. Infección interna aleatoria: cada segundo (20 ticks) intentamos infectar un bloque dentro del radio
        if (currentTick % 20 == 0) {
            infectRandomBlockInRadius(level, data);
        }
    }

    private static void infectRandomBlockInRadius(ServerLevel level, InfectionData data) {
        BlockPos nucleus = data.getNucleus();
        int radius = data.getCurrentRadius();
        if (radius <= 0) return;

        // Elegir una posición aleatoria dentro del círculo de radio 'radius'
        int x = nucleus.getX() + (int)(RANDOM.nextGaussian() * radius);
        int z = nucleus.getZ() + (int)(RANDOM.nextGaussian() * radius);
        // Asegurar que esté dentro del círculo
        while (Math.hypot(x - nucleus.getX(), z - nucleus.getZ()) > radius) {
            x = nucleus.getX() + RANDOM.nextInt(2 * radius + 1) - radius;
            z = nucleus.getZ() + RANDOM.nextInt(2 * radius + 1) - radius;
        }
        // Elegir una altura de superficie (puedes usar el heightmap)
        int y = level.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.WORLD_SURFACE, x, z);
        BlockPos pos = new BlockPos(x, y, z);

        // Convertir el bloque si es necesario
        BlockState currentState = level.getBlockState(pos);
        Block targetBlock = currentState.getBlock();
        Block corruptedVersion = InfectionConversion.getCorruptedVersion(targetBlock);
        if (corruptedVersion != null) {
            level.setBlockAndUpdate(pos, corruptedVersion.defaultBlockState());
        } else {
            // Bloque artificial: manejar según probabilidades (opcional)
            // Por ahora, no hacemos nada, pero puedes implementar la lógica de desaparición/ceniza
        }
    }
}