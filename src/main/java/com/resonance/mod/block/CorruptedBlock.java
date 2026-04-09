package com.resonance.mod.block;

import com.resonance.mod.InfectionConversion;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class CorruptedBlock extends Block {
    public CorruptedBlock(Properties properties) {
        super(properties);
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        // Intentar infectar bloques vecinos (6 direcciones)
        if (random.nextInt(5) == 0) { // 20% de probabilidad por tick
            for (BlockPos neighbor : BlockPos.betweenClosed(pos.offset(-1, -1, -1), pos.offset(1, 1, 1))) {
                if (!neighbor.equals(pos)) {
                    Block targetBlock = level.getBlockState(neighbor).getBlock();
                    Block corrupted = InfectionConversion.getCorruptedVersion(targetBlock);
                    if (corrupted != null) {
                        level.setBlockAndUpdate(neighbor, corrupted.defaultBlockState());
                        // Aquí podrías añadir lógica de puntos si es necesario
                    }
                }
            }
        }
    }
}