package com.resonance.mod.block;

import com.resonance.mod.MobInfectionHandler;
import com.resonance.mod.ResonanceData;
import com.resonance.mod.registry.ModBlocks;
import com.resonance.mod.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.util.RandomSource;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MineralSpikeBlock extends BushBlock implements BonemealableBlock {
    public static final int MAX_AGE = 3;
    public static final IntegerProperty AGE = IntegerProperty.create("age", 0, 3);
    private static final VoxelShape SAPLING_SHAPE = Block.box(3.0D, 0.0D, 3.0D, 13.0D, 4.0D, 13.0D);
    private static final VoxelShape MID_GROWTH_SHAPE = Block.box(1.0D, 0.0D, 1.0D, 15.0D, 8.0D, 15.0D);
    private static final VoxelShape FULL_GROWTH_SHAPE = Block.box(1.0D, 0.0D, 1.0D, 15.0D, 12.0D, 15.0D);
    private static final float HURT_SPEED_THRESHOLD = 0.003F;
    private static final Map<UUID, Integer> lastResonanceTick = new HashMap<>();
    private static final int RESONANCE_COOLDOWN = 20; // 1 segundo (20 ticks)

    public MineralSpikeBlock() {
        super(BlockBehaviour.Properties.of()
                .mapColor(MapColor.COLOR_PURPLE)
                .strength(1.5f, 4.0f)
                .sound(SoundType.GLASS)
                .noOcclusion()
                .randomTicks()
        );
        this.registerDefaultState(this.stateDefinition.any().setValue(AGE, 0));
    }

    @Override
    public ItemStack getCloneItemStack(BlockGetter level, BlockPos pos, BlockState state) {
        return new ItemStack(ModItems.MINERAL_SPIKE.get());
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        int age = state.getValue(AGE);
        if (age == 0) return SAPLING_SHAPE;
        if (age < MAX_AGE) return MID_GROWTH_SHAPE;
        return FULL_GROWTH_SHAPE;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.empty(); // Completamente traspasable
    }

    @Override
    public boolean isRandomlyTicking(BlockState state) {
        return state.getValue(AGE) < MAX_AGE;
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        int age = state.getValue(AGE);
        if (age < MAX_AGE && level.getRawBrightness(pos.above(), 0) >= 9 && random.nextInt(5) == 0) {
            BlockState newState = state.setValue(AGE, age + 1);
            level.setBlock(pos, newState, 2);
            level.gameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.of(newState));
        }
    }

    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (entity instanceof LivingEntity living && entity.getType() != EntityType.FOX) {
            // Ralentización (como las bayas)
            living.makeStuckInBlock(state, new Vec3(0.8F, 0.75D, 0.8F));

            if (!level.isClientSide && state.getValue(AGE) > 0 &&
                    (living.xOld != living.getX() || living.zOld != living.getZ())) {
                double dx = Math.abs(living.getX() - living.xOld);
                double dz = Math.abs(living.getZ() - living.zOld);
                if (dx >= HURT_SPEED_THRESHOLD || dz >= HURT_SPEED_THRESHOLD) {
                    // Daño (1 punto, sin cooldown)
                    living.hurt(level.damageSources().sweetBerryBush(), 1.0F);

                    // Aumentar resonancia con cooldown de 1 segundo
                    int currentTick = level.getServer().getTickCount();
                    UUID uuid = entity.getUUID();
                    int lastResTick = lastResonanceTick.getOrDefault(uuid, 0);
                    if (currentTick - lastResTick >= RESONANCE_COOLDOWN) {
                        if (entity instanceof Player player && ResonanceData.isMarked(player)) {
                            ResonanceData.addResonance(player, 0.1f);
                            lastResonanceTick.put(uuid, currentTick);
                        }
                    }
                }
            }
        }
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        int age = state.getValue(AGE);
        boolean isMature = age == MAX_AGE;
        ItemStack heldItem = player.getItemInHand(hand);

        if (heldItem.getItem() == Items.SHEARS && isMature) {
            if (!level.isClientSide) {
                int count = 1 + level.random.nextInt(2);
                popResource(level, pos, new ItemStack(ModItems.ORGANIC_GEM_OPAQUE.get(), count));
                level.setBlock(pos, state.setValue(AGE, 1), 2);
                heldItem.hurtAndBreak(1, player, (p) -> p.broadcastBreakEvent(hand));
                level.playSound(null, pos, SoundEvents.SWEET_BERRY_BUSH_PICK_BERRIES, SoundSource.BLOCKS, 1.0F, 0.8F + level.random.nextFloat() * 0.4F);
                level.gameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.of(player, state.setValue(AGE, 1)));
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        return super.use(state, level, pos, player, hand, hit);
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        BlockPos below = pos.below();
        BlockState belowState = level.getBlockState(below);
        Block belowBlock = belowState.getBlock();
        // Acepta todos los bloques corruptos (terreno, tronco, etc.)
        return belowBlock instanceof CorruptedBlock ||
                belowBlock instanceof CorruptedGrassBlock ||
                belowBlock == ModBlocks.CORRUPTED_MINERAL.get() ||
                belowBlock == ModBlocks.CORRUPTED_MINERAL_ORE.get();
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(AGE);
    }

    // ========== BONEMEAL (abono) ==========
    @Override
    public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state, boolean isClient) {
        return state.getValue(AGE) < MAX_AGE;
    }

    @Override
    public boolean isBonemealSuccess(Level level, RandomSource random, BlockPos pos, BlockState state) {
        return true;
    }

    @Override
    public void performBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state) {
        int newAge = Math.min(MAX_AGE, state.getValue(AGE) + 1);
        level.setBlock(pos, state.setValue(AGE, newAge), 2);
    }
}