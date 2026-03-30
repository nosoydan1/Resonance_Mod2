package com.resonance.mod.entity;

import com.resonance.mod.registry.ModItems;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import com.resonance.mod.MobInfectionHandler;

public class MineEntity extends PathfinderMob {

    public MineEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 40.0)
                .add(Attributes.MOVEMENT_SPEED, 0.28)
                .add(Attributes.ATTACK_DAMAGE, 6.0)
                .add(Attributes.ARMOR, 6.0)
                .add(Attributes.FOLLOW_RANGE, 24.0);
    }

    @Override
    protected void registerGoals() {
        // Atacar mobs pequeños y al jugador
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.0, true));
        this.goalSelector.addGoal(2, new WaterAvoidingRandomStrollGoal(this, 0.8));

        // Targets: Atacar animales pero no otros mobs del mod Resonance
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(
                this, Animal.class, true,
                e -> !(e instanceof ChipsEntity) && !(e instanceof MineEntity)
                        && !(e instanceof RaliteEntity) && !(e instanceof AshenKnightEntity)
                        && !(e instanceof MineralGuardianEntity)
                        && !MobInfectionHandler.isInfected(e)));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(
                this, Player.class, true));
    }

    @Override
    protected void dropCustomDeathLoot(DamageSource source, int looting, boolean recentlyHit) {
        super.dropCustomDeathLoot(source, looting, recentlyHit);

        // Mine Fragment (prob. baja - 30%)
        if (this.random.nextFloat() < 0.3f) {
            this.spawnAtLocation(new ItemStack(ModItems.MINE_FRAGMENT.get(), 1));
        }

        // Mine Essence (prob. extremadamente baja - 5%)
        if (this.random.nextFloat() < 0.05f) {
            this.spawnAtLocation(new ItemStack(ModItems.MINE_ESSENCE.get(), 1));
        }
    }
}