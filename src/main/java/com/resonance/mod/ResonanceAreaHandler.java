package com.resonance.mod;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.resonance.mod.block.DampingMechanismBlock;
import com.resonance.mod.network.NetworkHandler;
import com.resonance.mod.network.ResonanceSyncPacket;
import com.resonance.mod.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

public class ResonanceAreaHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(ResonanceAreaHandler.class);

    public void algunMetodo() {
        // Antes: System.out.println("Jugador cerca");
        LOGGER.debug("Jugador cerca del área de resonancia");
    }
}

private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(ResonanceAreaHandler.class);

@Mod.EventBusSubscriber(modid = ResonanceMod.MODID)
public class ResonanceAreaHandler {

    private static final int CHECK_INTERVAL = 20;
    private static int tickCounter = 0;
    private static final int DETECTION_RADIUS = 10;
    private static final float RESONANCE_GAIN = 1.0f;
    private static final float RESONANCE_DECAY = 0.5f;

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (event.player.level().isClientSide()) return;
        if (!ResonanceData.isMarked(event.player)) return;

        tickCounter++;
        if (tickCounter < CHECK_INTERVAL) return;
        tickCounter = 0;

        Player player = event.player;
        Level level = player.level();
        BlockPos playerPos = player.blockPosition();

        boolean nearCorruption = isNearCorruptedMineral(level, playerPos);
        // FIX: si hay un DampingMechanism activo en radio 8, no sumar Resonancia
        boolean dampingActive = DampingMechanismBlock.playerHasDampingProtection(level, playerPos);

        if (nearCorruption && !dampingActive) {
            ResonanceData.addResonance(player, RESONANCE_GAIN);
        } else if (!nearCorruption) {
            ResonanceData.reduceResonance(player, RESONANCE_DECAY);
        }
        // Si nearCorruption && dampingActive: no sube ni baja (el bloque neutraliza la ganancia)
        // La reducción activa del DampingMechanism se maneja en DampingMechanismBlock.onServerTick

        if (player instanceof ServerPlayer serverPlayer) {
            NetworkHandler.sendToClient(
                    new ResonanceSyncPacket(ResonanceData.getResonance(player), ResonanceData.isMarked(player)),
                    serverPlayer
            );
        }
    }

    private static boolean isNearCorruptedMineral(Level level, BlockPos center) {
        Block corruptedMineral = ModBlocks.CORRUPTED_MINERAL.get();
        Block corruptedMineralOre = ModBlocks.CORRUPTED_MINERAL_ORE.get();

        for (int y = -DETECTION_RADIUS; y <= DETECTION_RADIUS; y++) {
            for (int x = -DETECTION_RADIUS; x <= DETECTION_RADIUS; x++) {
                for (int z = -DETECTION_RADIUS; z <= DETECTION_RADIUS; z++) {
                    Block block = level.getBlockState(center.offset(x, y, z)).getBlock();
                    if (block == corruptedMineral || block == corruptedMineralOre) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
private static boolean isNearCorruptedMineral(Level level, BlockPos center) {
    Block corruptedMineral = ModBlocks.CORRUPTED_MINERAL.get();
    Block corruptedMineralOre = ModBlocks.CORRUPTED_MINERAL_ORE.get();

    // Usar BFS en lugar de búsqueda bruta
    java.util.Queue<BlockPos> queue = new java.util.LinkedList<>();
    java.util.Set<BlockPos> visited = new java.util.HashSet<>();

    queue.add(center);
    visited.add(center);

    while (!queue.isEmpty() && visited.size() < 216) { // 6x6x6 = 216 bloques máx
        BlockPos current = queue.poll();

        if (Math.abs(current.getX() - center.getX()) > ResonanceAreaHandler.DETECTION_RADIUS ||
                Math.abs(current.getY() - center.getY()) > ResonanceAreaHandler.DETECTION_RADIUS ||
                Math.abs(current.getZ() - center.getZ()) > ResonanceAreaHandler.DETECTION_RADIUS) {
            continue;
        }

        Block block = level.getBlockState(current).getBlock();
        if (block == corruptedMineral || block == corruptedMineralOre) {
            return true;
        }

        // Añadir vecinos
        for (BlockPos neighbor : BlockPos.betweenClosed(
                current.offset(-1, -1, -1), current.offset(1, 1, 1))) {
            if (!visited.contains(neighbor)) {
                visited.add(neighbor);
                queue.add(neighbor);
            }
        }
    }

    return false;
}
