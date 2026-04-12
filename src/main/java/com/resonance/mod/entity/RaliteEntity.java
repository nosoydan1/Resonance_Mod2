package com.resonance.mod.entity;

import com.resonance.mod.registry.ModItems;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import com.resonance.mod.entity.AshenKnightEntity;
import com.resonance.mod.entity.ChipsEntity;
import com.resonance.mod.entity.MineEntity;
import com.resonance.mod.entity.MineralGuardianEntity;
import com.resonance.mod.MobInfectionHandler;

public class RaliteEntity extends PathfinderMob {

    public RaliteEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
        this.setPersistenceRequired();
    }


    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 80.0)
                .add(Attributes.MOVEMENT_SPEED, 0.18) // lento pero resistente
                .add(Attributes.ATTACK_DAMAGE, 10.0)
                .add(Attributes.ARMOR, 12.0)
                .add(Attributes.FOLLOW_RANGE, 20.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.8);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.0, true));
        this.goalSelector.addGoal(2, new WaterAvoidingRandomStrollGoal(this, 0.8));

        // Ralite ataca cualquier LivingEntity except otros mobs del mod
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(
                this, net.minecraft.world.entity.LivingEntity.class, true,
                e -> !(e instanceof RaliteEntity) && !(e instanceof MineEntity)
                        && !(e instanceof ChipsEntity) && !(e instanceof AshenKnightEntity)
                        && !(e instanceof MineralGuardianEntity)
                        && !MobInfectionHandler.isInfected(e)));
    }

    @Override
    protected void dropCustomDeathLoot(DamageSource source, int looting, boolean recentlyHit) {
        super.dropCustomDeathLoot(source, looting, recentlyHit);

        // Ralite Fragment (prob. baja - 30%)
        if (this.random.nextFloat() < 0.3f) {
            this.spawnAtLocation(new ItemStack(ModItems.RALITE_FRAGMENT.get(), 1));
        }

        // Ralite Essence (prob. extremadamente baja - 5%)
        if (this.random.nextFloat() < 0.05f) {
            this.spawnAtLocation(new ItemStack(ModItems.RALITE_ESSENCE.get(), 1));
        }
    }
}