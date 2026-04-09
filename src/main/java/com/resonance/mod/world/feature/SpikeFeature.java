package com.resonance.mod.world.feature;

import com.mojang.serialization.Codec;
import com.resonance.mod.block.MineralSpikeBlock;
import com.resonance.mod.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public class SpikeFeature extends Feature<NoneFeatureConfiguration> {
    private static final int TRIES = 32;
    private static final int XZ_SPREAD = 6;
    private static final int Y_SPREAD = 2;

    public SpikeFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        RandomSource random = context.random();
        BlockPos origin = context.origin();

        int placed = 0;
        for (int i = 0; i < TRIES; i++) {
            int dx = random.nextInt(XZ_SPREAD * 2 + 1) - XZ_SPREAD;
            int dz = random.nextInt(XZ_SPREAD * 2 + 1) - XZ_SPREAD;
            int dy = random.nextInt(Y_SPREAD * 2 + 1) - Y_SPREAD;
            BlockPos pos = origin.offset(dx, dy, dz);

            var spikeState = ModBlocks.MINERAL_SPIKE.get().defaultBlockState()
                    .setValue(MineralSpikeBlock.AGE, 3);

            if (spikeState.canSurvive(level, pos)) {
                level.setBlock(pos, spikeState, 2);
                placed++;
            }
        }
        return placed > 0;
    }
}