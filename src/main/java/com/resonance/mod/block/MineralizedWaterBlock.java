package com.resonance.mod.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.MapColor;

public class MineralizedWaterBlock extends LiquidBlock {

    public MineralizedWaterBlock(FlowingFluid fluid) {
        super(fluid, Properties.of()
                .mapColor(MapColor.WATER)
                .replaceable()
                .noCollission()
                .strength(100.0f)
                .sound(SoundType.EMPTY)
        );
    }

    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (!level.isClientSide() && entity instanceof LivingEntity living) {
            // +2% Resonancia por segundo
            if (entity instanceof net.minecraft.world.entity.player.Player player) {
                if (com.resonance.mod.ResonanceData.isMarked(player)) {
                    com.resonance.mod.ResonanceData.addResonance(player, 0.04f); // 20 ticks = 1 segundo
                }
            }

            // Destruir cultivos
            BlockState blockAbove = level.getBlockState(pos.above());
            if (blockAbove.getBlock() instanceof net.minecraft.world.level.block.CropBlock) {
                level.setBlockAndUpdate(pos.above(), net.minecraft.world.level.block.Blocks.AIR.defaultBlockState());
            }
        }
    }
}
