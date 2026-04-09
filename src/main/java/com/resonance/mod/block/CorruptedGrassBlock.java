package com.resonance.mod.block;

import com.resonance.mod.InfectionConversion;
import com.resonance.mod.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class CorruptedGrassBlock extends CorruptedBlock {
    public CorruptedGrassBlock(Properties properties) {
        super(properties);
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        super.randomTick(state, level, pos, random); // propagación general

        // Propagación de césped: convertir tierra corrupta adyacente a césped corrupto
        if (level.getRawBrightness(pos.above(), 0) >= 9) {
            for (BlockPos neighbor : BlockPos.withinManhattan(pos, 1, 1, 1)) {
                if (level.getBlockState(neighbor).getBlock() == ModBlocks.CORRUPTED_DIRT.get()) {
                    if (random.nextInt(3) == 0) { // 33% de probabilidad
                        level.setBlockAndUpdate(neighbor, ModBlocks.CORRUPTED_GRASS.get().defaultBlockState());
                    }
                }
            }
        }
    }
}