package com.resonance.mod.entity;

import com.resonance.mod.registry.ModItems;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;

public class AshenKnightEntity extends PathfinderMob {

    public AshenKnightEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
        this.setNoGravity(true); // vuela
        this.setPersistenceRequired();
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 60.0)
                .add(Attributes.MOVEMENT_SPEED, 0.32)
                .add(Attributes.ATTACK_DAMAGE, 8.0)
                .add(Attributes.ARMOR, 8.0)
                .add(Attributes.FOLLOW_RANGE, 20.0);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.0, true));
        this.goalSelector.addGoal(2, new WaterAvoidingRandomStrollGoal(this, 0.8));
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(
                this, Player.class, true));
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (!isVulnerable()) {
            // Invulnerable — mostrar daño rechazado sin perder HP
            this.setLastHurtByMob(null); // No actualizar último mob que hirió
            if (this.level().isClientSide()) {
                this.hurtMarked = true;
            }
            return false;
        }
        // Es vulnerable - permitir daño normal
        return super.hurt(source, amount);
    }

    private boolean isVulnerable() {
        // De día siempre vulnerable
        if (!this.level().isNight()) return true;

        // Con luz nivel 8+ vulnerable
        int lightLevel = this.level().getBrightness(LightLayer.BLOCK, this.blockPosition());
        return lightLevel >= 8;
    }

    @Override
    public void tick() {
        super.tick();
        // NoGravity ya previene la caída, solo asegurar altura mínima
        if (!this.level().isClientSide() && this.getY() < this.blockPosition().getY() + 0.5) {
            this.setPos(this.getX(), this.blockPosition().getY() + 0.5, this.getZ());
        }
    }

    @Override
    protected void dropCustomDeathLoot(DamageSource source, int looting, boolean recentlyHit) {
        super.dropCustomDeathLoot(source, looting, recentlyHit);

        // Ashes (alta probabilidad - 80%)
        if (this.random.nextFloat() < 0.8f) {
            this.spawnAtLocation(new ItemStack(ModItems.ASHES.get(), 1));
        }

        // Fossilized Carbon (alta probabilidad - 80%)
        if (this.random.nextFloat() < 0.8f) {
            this.spawnAtLocation(new ItemStack(ModItems.FOSSILIZED_CARBON.get(), 1));
        }
    }
}