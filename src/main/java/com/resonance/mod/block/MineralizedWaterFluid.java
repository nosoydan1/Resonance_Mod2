package com.resonance.mod.block;

import com.resonance.mod.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;

public class MineralizedWaterFluid extends FlowingFluid {

    public MineralizedWaterFluid() {
        registerDefaultState(getStateDefinition().any().setValue(LEVEL, 8));
    }

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
    public int getDropOff(LevelReader level) {
        return 1;
    }

    @Override
    public int getTickDelay(LevelReader level) {
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
    protected void beforeDestroyingBlock(LevelAccessor level, BlockPos pos, BlockState state) {
        // No-op
    }

    @Override
    protected int getSlopeFindDistance(LevelReader level) {
        return 4;
    }

    @Override
    protected BlockState createLegacyBlock(FluidState state) {
        return ModBlocks.MINERALIZED_WATER_BLOCK.get().defaultBlockState();
    }

    @Override
    public float getExplosionResistance() {
        return 100.0f;
    }

    @Override
    public Item getBucket() {
        // Si tienes un ítem cubeta personalizado, cámbialo aquí
        return Items.WATER_BUCKET;
    }

    @Override
    protected void animateTick(Level level, BlockPos pos, FluidState state, RandomSource random) {
        if (random.nextInt(4) == 0) {
            level.addParticle(
                    ParticleTypes.SPLASH,
                    pos.getX() + random.nextDouble(),
                    pos.getY() + 1,
                    pos.getZ() + random.nextDouble(),
                    0, 0, 0
            );
        }
    }

    @Override
    protected void createFluidStateDefinition(StateDefinition.Builder<Fluid, FluidState> builder) {
        super.createFluidStateDefinition(builder);
        builder.add(LEVEL);
    }
}