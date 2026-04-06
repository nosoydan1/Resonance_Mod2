package com.resonance.mod.block;

import com.resonance.mod.registry.ModBlocks;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockStateProperties;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.Level;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

public class MineralizedWaterBlock extends LiquidBlock {

    public MineralizedWaterBlock(FlowingFluid fluid) {
        super(fluid, BlockBehaviour.Properties.of()
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

// Clase del fluido
package com.resonance.mod.block;

import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;

public class MineralizedWaterFluid extends FlowingFluid {

    @Override
    public Fluid getFlowing() {
        return ModBlocks.MINERALIZED_WATER_FLOWING.get();
    }

    @Override
    public Fluid getSource() {
        return ModBlocks.MINERALIZED_WATER.get();
    }

    @Override
    public boolean isSame(Fluid fluid) {
        return fluid == getSource() || fluid == getFlowing();
    }

    @Override
    public int getDropOff(LevelAccessor level) {
        return 1;
    }

    @Override
    public int getTickDelay(LevelAccessor level) {
        return 5;
    }

    @Override
    public boolean canBeReplacedWith(FluidState state, BlockGetter level, BlockPos pos,
                                     Fluid fluid, Direction direction) {
        return false;
    }

    @Override
    protected boolean canConvertToSource(Level level) {
        return false;
    }

    @Override
    public boolean isSource(FluidState state) {
        return false;
    }

    @Override
    public int getAmount(FluidState state) {
        return 0;
    }

    @Override
    protected void animateTick(Level level, BlockPos pos, FluidState state, Random random) {
        if (random.nextInt(4) == 0) {
            level.addParticle(
                    ParticleTypes.WATER_SPLASH,
                    pos.getX() + Math.random(),
                    pos.getY() + 1,
                    pos.getZ() + Math.random(),
                    0, 0, 0
            );
        }
    }
}