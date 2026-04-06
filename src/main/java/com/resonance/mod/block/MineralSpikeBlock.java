package com.resonance.mod.block;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import com.resonance.mod.registry.ModItems;
import net.minecraft.util.RandomSource;

public class MineralSpikeBlock extends Block {

    private static final VoxelShape SHAPE = Block.box(4, 0, 4, 12, 8, 12);
    private static final float DAMAGE = 2.0f;
    public static final BooleanProperty HAS_FRUIT = BooleanProperty.create("has_fruit");

    public MineralSpikeBlock() {
        super(BlockBehaviour.Properties.of()
                .mapColor(MapColor.COLOR_PURPLE)
                .strength(1.5f, 4.0f)
                .sound(SoundType.GLASS)
                .noOcclusion()
                .randomTicks()  // Necesario para regenerar el fruto
        );
        this.registerDefaultState(this.stateDefinition.any().setValue(HAS_FRUIT, true));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(HAS_FRUIT);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    // Daño al pisar (solo si tiene fruto o siempre, como prefieras)
    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (!level.isClientSide() && entity instanceof LivingEntity living) {
            living.hurt(level.damageSources().generic(), DAMAGE);

            if (entity instanceof Player player && com.resonance.mod.ResonanceData.isMarked(player)) {
                com.resonance.mod.ResonanceData.addResonance(player, 2.0f);
            }
        }
    }

    // Cosechar el fruto con la herramienta específica (ej. tijeras)
    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        ItemStack heldItem = player.getItemInHand(hand);
        // Cambia la herramienta aquí por la que quieras (por ejemplo, una azada o una herramienta personalizada)
        if (heldItem.getItem() == Items.SHEARS && state.getValue(HAS_FRUIT)) {
            if (!level.isClientSide) {
                // Suelta el fruto (la gema)
                int count = 1 + level.random.nextInt(2); // 1-2 gemas
                popResource(level, pos, new ItemStack(ModItems.ORGANIC_GEM_OPAQUE.get(), count));
                // El arbusto pierde el fruto
                level.setBlock(pos, state.setValue(HAS_FRUIT, false), Block.UPDATE_ALL);
                // Daña la herramienta
                heldItem.hurtAndBreak(1, player, (p) -> p.broadcastBreakEvent(hand));
                // Reproducir sonido de cosecha
                level.playSound(null, pos, SoundEvents.SWEET_BERRY_BUSH_PICK_BERRIES, SoundSource.BLOCKS, 1.0f, 1.0f);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        return InteractionResult.PASS;
    }

    // Regenerar el fruto con el tiempo
    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!state.getValue(HAS_FRUIT) && random.nextFloat() < 0.1f) { // 10% de probabilidad por tick aleatorio
            level.setBlock(pos, state.setValue(HAS_FRUIT, true), Block.UPDATE_ALL);
        }
    }

    // Drop al minar: solo suelta el bloque si la herramienta usada es la correcta (ej. pico)
    // Nota: Para que funcione correctamente, debes definir en tu loot table o usar playerDestroy.
    // Aquí sobrescribimos playerDestroy para un control preciso.
    @Override
    public void playerDestroy(Level level, Player player, BlockPos pos, BlockState state, net.minecraft.world.level.block.entity.BlockEntity te, ItemStack tool) {
        super.playerDestroy(level, player, pos, state, te, tool);
        if (!level.isClientSide && tool.isCorrectToolForDrops(state)) {
            // Si la herramienta es la adecuada (según el tag de minado), suelta el bloque del arbusto
            popResource(level, pos, new ItemStack(this));
        }
        // Si la herramienta no es la correcta, no suelta nada (el bloque se destruye sin drop)
    }

    // Para el modo creativo, al recoger con pick block (botón central) se obtiene el bloque
    @Override
    public ItemStack getCloneItemStack(BlockGetter level, BlockPos pos, BlockState state) {
        return new ItemStack(this);
    }
}
