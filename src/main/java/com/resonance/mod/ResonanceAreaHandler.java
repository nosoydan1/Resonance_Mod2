package com.resonance.mod;

import com.resonance.mod.block.DampingMechanismBlock;
import com.resonance.mod.network.NetworkHandler;
import com.resonance.mod.network.ResonanceSyncPacket;
import com.resonance.mod.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
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
    private static final float RESONANCE_GAIN = 1.5f;
    private static final float RESONANCE_DECAY = 1.0f;

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
        boolean dampingActive = DampingMechanismBlock.playerHasDampingProtection(level, playerPos);

        if (nearCorruption && !dampingActive) {
            ResonanceData.addResonance(player, RESONANCE_GAIN);
        } else if (!nearCorruption) {
            ResonanceData.reduceResonance(player, RESONANCE_DECAY);
        }

        if (player instanceof ServerPlayer serverPlayer) {
            NetworkHandler.sendToClient(
                    new ResonanceSyncPacket(ResonanceData.getResonance(player), ResonanceData.isMarked(player)),
                    serverPlayer
            );
        }

        // ========== Manejo de petrificación ==========
        float resonance = ResonanceData.getResonance(player);

        // Si la resonancia alcanzó 100% y no está ya en proceso de petrificación
        if (resonance >= 100.0f && !ResonanceData.isPetrifying(player)) {
            ResonanceData.startPetrification(player, 100); // 5 segundos = 100 ticks
            player.sendSystemMessage(Component.literal("§c¡Estás siendo petrificado! Usa Dissonant Injection para salvarte."));
        }

        // Si ya está petrificándose, manejar el temporizador
        if (ResonanceData.isPetrifying(player)) {
            int ticksLeft = ResonanceData.getPetrifyTicksLeft(player);
            if (ticksLeft <= 0) {
                ResonanceMod.createPetrifiedStatue(player);
                player.kill();
                ResonanceData.clearPetrification(player);
            } else {
                ResonanceData.decrementPetrifyTicks(player);
                if (ticksLeft % 20 == 0 && ticksLeft <= 100) {
                    int secondsLeft = ticksLeft / 20;
                    player.sendSystemMessage(Component.literal("§c¡" + secondsLeft + " segundos antes de petrificarte!"));
                }
            }
        }
    }

    /**
     * Busca si hay mineral corrupto en la columna vertical hasta 15 bloques hacia abajo.
     */
    private static boolean isNearCorruptedMineral(Level level, BlockPos center) {
        Block corruptedMineral = ModBlocks.CORRUPTED_MINERAL.get();
        Block corruptedMineralOre = ModBlocks.CORRUPTED_MINERAL_ORE.get();

        for (int dy = 0; dy <= 15; dy++) {
            BlockPos checkPos = center.below(dy);
            Block block = level.getBlockState(checkPos).getBlock();
            if (block == corruptedMineral || block == corruptedMineralOre) {
                return true;
            }
        }
        return false;
    }
}