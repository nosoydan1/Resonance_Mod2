package com.resonance.mod;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod.EventBusSubscriber(modid = ResonanceMod.MODID)
public class ResonanceAreaHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(ResonanceAreaHandler.class);
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
        // Si hay un DampingMechanism activo en radio 8, no sumar Resonancia
        boolean dampingActive = DampingMechanismBlock.playerHasDampingProtection(level, playerPos);

        if (nearCorruption && !dampingActive) {
            ResonanceData.addResonance(player, RESONANCE_GAIN);
        } else if (!nearCorruption) {
            ResonanceData.reduceResonance(player, RESONANCE_DECAY);
        }
        // Si nearCorruption && dampingActive: no sube ni baja (el bloque neutraliza la ganancia)

        if (player instanceof ServerPlayer serverPlayer) {
            NetworkHandler.sendToClient(
                    new ResonanceSyncPacket(ResonanceData.getResonance(player), ResonanceData.isMarked(player)),
                    serverPlayer
            );
        }
    }

    /**
     * Busca si hay mineral corrupto cerca usando BFS (más eficiente que fuerza bruta).
     */
    private static boolean isNearCorruptedMineral(Level level, BlockPos center) {
        Block corruptedMineral = ModBlocks.CORRUPTED_MINERAL.get();
        Block corruptedMineralOre = ModBlocks.CORRUPTED_MINERAL_ORE.get();

        java.util.Queue<BlockPos> queue = new java.util.LinkedList<>();
        java.util.Set<BlockPos> visited = new java.util.HashSet<>();

        queue.add(center);
        visited.add(center);

        while (!queue.isEmpty() && visited.size() < 216) { // 6x6x6 = 216 bloques máx
            BlockPos current = queue.poll();

            // Limitar la búsqueda al radio DETECTION_RADIUS
            if (Math.abs(current.getX() - center.getX()) > DETECTION_RADIUS ||
                    Math.abs(current.getY() - center.getY()) > DETECTION_RADIUS ||
                    Math.abs(current.getZ() - center.getZ()) > DETECTION_RADIUS) {
                continue;
            }

            Block block = level.getBlockState(current).getBlock();
            if (block == corruptedMineral || block == corruptedMineralOre) {
                return true;
            }

            // Añadir vecinos (cubo 3x3 alrededor)
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
}