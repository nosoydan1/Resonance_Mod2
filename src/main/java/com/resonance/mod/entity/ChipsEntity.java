package com.resonance.mod.entity;

import com.resonance.mod.InfectionData;
import com.resonance.mod.block.CorruptedMineralBlock;
import com.resonance.mod.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

/**
 * Chips — fragmento de roca ambulante.
 *
 * Fases 1–4: infecta bloques cercanos con efectividad decreciente. No ataca.
 * Fase 5:    deja de infectar, ataca al jugador y animales activamente.
 * Fase 6:    se detiene y petrifica gradualmente en bloque de piedra.
 */
public class ChipsEntity extends PathfinderMob {

    private boolean isPetrifying = false;
    private int petrifyTimer = 0;
    private static final int PETRIFY_TICKS = 200; // 10 segundos

    public ChipsEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 12.0)
                .add(Attributes.MOVEMENT_SPEED, 0.35)
                .add(Attributes.ATTACK_DAMAGE, 2.0)
                .add(Attributes.FOLLOW_RANGE, 16.0);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.0, true));
        this.goalSelector.addGoal(2, new WaterAvoidingRandomStrollGoal(this, 0.8));
        this.goalSelector.addGoal(3, new InfectNearbyBlocksGoal(this));

        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(
                this, Animal.class, true,
                e -> canAttack()
                        && !(e instanceof ChipsEntity) && !(e instanceof MineEntity)
                        && !(e instanceof RaliteEntity) && !(e instanceof AshenKnightEntity)
                        && !(e instanceof MineralGuardianEntity)));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(
                this, Player.class, true,
                e -> canAttack()));
    }

    private boolean canAttack() {
        if (this.level().isClientSide()) return false;
        if (!(this.level() instanceof ServerLevel serverLevel)) return false;
        return InfectionData.get(serverLevel).getPhase() >= 5;
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide()) return;
        if (!(this.level() instanceof ServerLevel serverLevel)) return;

        int phase = InfectionData.get(serverLevel).getPhase();

        // Fase 6: iniciar petrificación
        if (phase >= 6 && !isPetrifying) {
            isPetrifying = true;
            this.getNavigation().stop();
            this.setNoAi(true);
        }

        if (isPetrifying) {
            petrifyTimer++;
            if (petrifyTimer >= PETRIFY_TICKS) {
                this.level().setBlockAndUpdate(this.blockPosition(),
                        net.minecraft.world.level.block.Blocks.STONE.defaultBlockState());
                this.discard();
            }
        }
    }

    // -------------------------------------------------------------------------
    static class InfectNearbyBlocksGoal extends net.minecraft.world.entity.ai.goal.Goal {

        private final ChipsEntity chips;
        private int cooldown = 0;

        public InfectNearbyBlocksGoal(ChipsEntity chips) {
            this.chips = chips;
        }

        @Override
        public boolean canUse() {
            if (!(chips.level() instanceof ServerLevel serverLevel)) return false;
            int phase = InfectionData.get(serverLevel).getPhase();
            return phase >= 1 && phase <= 4;
        }

        @Override
        public boolean canContinueToUse() {
            return canUse();
        }

        @Override
        public void tick() {
            cooldown--;
            if (cooldown > 0) return;
            cooldown = 40;

            if (!(chips.level() instanceof ServerLevel level)) return;
            InfectionData data = InfectionData.get(level);
            int phase = data.getPhase();

            float effectiveness = switch (phase) {
                case 1 -> 1.00f;
                case 2 -> 0.80f;
                case 3 -> 0.65f;
                case 4 -> 0.35f;
                default -> 0f;
            };

            if (Math.random() > effectiveness) return;

            BlockPos pos = chips.blockPosition();
            BlockPos target = pos.offset(
                    (int)(Math.random() * 5) - 2,
                    (int)(Math.random() * 3) - 1,
                    (int)(Math.random() * 5) - 2
            );

            Block targetBlock = level.getBlockState(target).getBlock();
            if (CorruptedMineralBlock.canInfectStatic(targetBlock)) {
                level.setBlockAndUpdate(target,
                        ModBlocks.CORRUPTED_MINERAL.get().defaultBlockState());
                data.addPoints(InfectionData.getPointsForBlock(
                        net.minecraft.core.registries.BuiltInRegistries.BLOCK
                                .getKey(targetBlock).toString()
                ));
            }
        }
    }
}