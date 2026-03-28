package com.resonance.mod.entity;

import com.resonance.mod.InfectionData;
import com.resonance.mod.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

public class ChipsEntity extends PathfinderMob {

    public ChipsEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20.0)
                .add(Attributes.MOVEMENT_SPEED, 0.25)
                .add(Attributes.ARMOR, 4.0);
    }

    @Override
    protected void registerGoals() {
        // Caminar aleatoriamente
        this.goalSelector.addGoal(1, new WaterAvoidingRandomStrollGoal(this, 1.0));

        // Infectar bloques cercanos en fases 1-4
        this.goalSelector.addGoal(2, new InfectNearbyBlocksGoal(this));
    }

    // Goal para infectar bloques cercanos
    static class InfectNearbyBlocksGoal extends net.minecraft.world.entity.ai.goal.Goal {

        private final ChipsEntity chips;
        private int cooldown = 0;

        public InfectNearbyBlocksGoal(ChipsEntity chips) {
            this.chips = chips;
        }

        @Override
        public boolean canUse() {
            if (!(chips.level() instanceof ServerLevel level)) return false;
            InfectionData data = InfectionData.get(level);
            int phase = data.getPhase();
            // Chips infecta bloques en fases 1-4
            return phase >= 1 && phase <= 4;
        }

        @Override
        public void tick() {
            cooldown--;
            if (cooldown > 0) return;
            cooldown = 40; // cada 2 segundos

            if (!(chips.level() instanceof ServerLevel level)) return;
            InfectionData data = InfectionData.get(level);
            int phase = data.getPhase();

            // Efectividad según fase del GDD
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
            if (targetBlock != net.minecraft.world.level.block.Blocks.AIR
                    && targetBlock != net.minecraft.world.level.block.Blocks.BEDROCK
                    && targetBlock != ModBlocks.CORRUPTED_MINERAL.get()
                    && targetBlock != ModBlocks.CORRUPTED_MINERAL_ORE.get()
                    && targetBlock != ModBlocks.NUCLEUS.get()) {

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