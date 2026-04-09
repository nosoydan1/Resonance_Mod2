package com.resonance.mod.block;

import com.resonance.mod.ResonanceData;
import com.resonance.mod.ResonanceMod;
import com.resonance.mod.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ResonanceMod.MODID)
public class DampingMechanismBlock extends Block {

    public static final int DAMPING_RADIUS = 8;
    private static final float DAMPING_REDUCTION_PER_SECOND = 2.0f; // 2% por segundo (GDD)
    private static final int TICK_INTERVAL = 20; // 1 segundo
    private static int tickCounter = 0;

    public DampingMechanismBlock() {
        super(BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .requiresCorrectToolForDrops()
                .strength(3.0f, 6.0f)
                .sound(SoundType.METAL));
    }

    // Reducción activa cada segundo para jugadores dentro del radio
    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        tickCounter++;
        if (tickCounter < TICK_INTERVAL) return;
        tickCounter = 0;

        event.getServer().getAllLevels().forEach(level -> {
            for (ServerPlayer player : level.players()) {
                if (!ResonanceData.isMarked(player)) continue;
                if (hasDampingInArea(level, player.blockPosition())) {
                    ResonanceData.reduceResonance(player, DAMPING_REDUCTION_PER_SECOND);
                }
            }
        });
    }

    // Detección de si hay un bloque Damping Mechanism en el radio (comparación directa)
    public static boolean hasDampingInArea(Level level, BlockPos center) {
        Block dampingBlock = ModBlocks.DAMPING_MECHANISM.get();
        for (int x = -DAMPING_RADIUS; x <= DAMPING_RADIUS; x++) {
            for (int y = -DAMPING_RADIUS; y <= DAMPING_RADIUS; y++) {
                for (int z = -DAMPING_RADIUS; z <= DAMPING_RADIUS; z++) {
                    BlockPos pos = center.offset(x, y, z);
                    Block block = level.getBlockState(pos).getBlock();
                    if (block == dampingBlock) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    // Método auxiliar para ser usado desde ResonanceAreaHandler
    public static boolean playerHasDampingProtection(Level level, BlockPos playerPos) {
        return hasDampingInArea(level, playerPos);
    }
}