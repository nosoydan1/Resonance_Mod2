package com.resonance.mod.block;

import com.resonance.mod.InfectionData;
import com.resonance.mod.network.InfectionSyncPacket;
import com.resonance.mod.network.NetworkHandler;
import com.resonance.mod.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;

import static com.resonance.mod.block.CorruptedMineralBlock.canInfectStatic;

public class CorruptedMineralOreBlock extends Block {

    public CorruptedMineralOreBlock() {
        super(BlockBehaviour.Properties.of()
                .mapColor(MapColor.COLOR_PURPLE)
                .requiresCorrectToolForDrops()
                .strength(3.0f, 6.0f)
                .sound(SoundType.STONE)
        );
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        InfectionData data = InfectionData.get(level);

        // Limitar propagación en Fase 6
        if (data.getPhase() >= 6) {
            return; // No expandir más en fase final
        }

        // Reducir probabilidad en fases avanzadas
        float spreadChance = switch (data.getPhase()) {
            case 1, 2 -> 1.0f;
            case 3 -> 0.8f;
            case 4 -> 0.6f;
            case 5 -> 0.3f;
            default -> 0.0f;
        };

        if (random.nextFloat() > spreadChance) {
            return; // Skip este tick
        }

        int radius = 3;
        for (int attempts = 0; attempts < 5; attempts++) {
            int dx = random.nextInt(radius * 2 + 1) - radius;
            int dy = random.nextInt(radius * 2 + 1) - radius;
            int dz = random.nextInt(radius * 2 + 1) - radius;

            BlockPos target = pos.offset(dx, dy, dz);
            Block targetBlock = level.getBlockState(target).getBlock();

            if (canInfectStatic(targetBlock)) {
                level.setBlockAndUpdate(target,
                        ModBlocks.CORRUPTED_MINERAL.get().defaultBlockState());

                int points = InfectionData.getPointsForBlock(
                        net.minecraft.core.registries.BuiltInRegistries.BLOCK
                                .getKey(targetBlock).toString()
                );
                data.addPoints(points);

                NetworkHandler.sendToAllClients(
                        new InfectionSyncPacket(data.getPoints(), data.getPhase())
                );
                return; // Solo infectar 1 bloque por tick
            }
        }
    }
}