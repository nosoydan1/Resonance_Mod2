package com.resonance.mod.entity;

import com.resonance.mod.registry.ModItems;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.ai.goal.RangedAttackGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.SmallFireball;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

import java.util.EnumSet;
import java.util.Random;

public class MineralGuardianEntity extends PathfinderMob implements RangedAttackMob {

    private static final Random RANDOM = new Random();

    public MineralGuardianEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
        this.setPersistenceRequired();
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 100.0)
                .add(Attributes.MOVEMENT_SPEED, 0.22)
                .add(Attributes.ARMOR, 10.0)
                .add(Attributes.FOLLOW_RANGE, 32.0);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new IrregularRangedAttackGoal(this));
        this.goalSelector.addGoal(2, new WaterAvoidingRandomStrollGoal(this, 0.8));
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(
                this, Player.class, true));
    }

    @Override
    public void performRangedAttack(LivingEntity target, float power) {
        if (this.level().isClientSide()) return;

        double dx = target.getX() - this.getX();
        double dy = target.getY() + target.getEyeHeight() - this.getY() - 1.0;
        double dz = target.getZ() - this.getZ();
        double dist = Math.sqrt(dx * dx + dz * dz);

        // Crear bola de fuego que solo causa daño por impacto
        SmallFireball fireball = new SmallFireball(
                this.level(),
                this,
                dx / dist * 0.8,
                dy / dist * 0.5 + 0.3,
                dz / dist * 0.8
        );

        // Posicionar la bola de fuego ligeramente por encima del guardian
        fireball.setPos(
                this.getX(),
                this.getY() + 2.0,
                this.getZ()
        );

        // Configurar para que no propague fuego (solo daño por impacto)
        // SmallFireball por defecto no propaga fuego, solo causa daño

        this.level().addFreshEntity(fireball);
    }

    // Goal personalizado con intervalos irregulares para ataques impredecibles
    private static class IrregularRangedAttackGoal extends Goal {
        private final MineralGuardianEntity guardian;
        private LivingEntity target;
        private int attackCooldown;
        private final Random random = new Random();

        public IrregularRangedAttackGoal(MineralGuardianEntity guardian) {
            this.guardian = guardian;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            LivingEntity livingentity = this.guardian.getTarget();
            if (livingentity != null && livingentity.isAlive()) {
                this.target = livingentity;
                return true;
            }
            return false;
        }

        @Override
        public boolean canContinueToUse() {
            return this.canUse() || !this.guardian.getNavigation().isDone();
        }

        @Override
        public void stop() {
            this.target = null;
            this.attackCooldown = 0;
        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }

        @Override
        public void tick() {
            double distanceToTarget = this.guardian.distanceToSqr(this.target);

            // Mantener distancia óptima
            if (distanceToTarget <= 256.0) { // 16 bloques
                this.guardian.getNavigation().stop();
            } else {
                this.guardian.getNavigation().moveTo(this.target, 1.0);
            }

            // Mirar al objetivo
            this.guardian.getLookControl().setLookAt(this.target, 30.0F, 30.0F);

            // Sistema de cooldown irregular
            if (this.attackCooldown > 0) {
                --this.attackCooldown;
            } else if (distanceToTarget <= 400.0) { // 20 bloques
                // Intervalos irregulares: entre 2-5 segundos (40-100 ticks)
                int baseCooldown = 40 + random.nextInt(61); // 40-100 ticks

                // Añadir variabilidad adicional para romper patrones
                if (random.nextFloat() < 0.3f) { // 30% de probabilidad
                    baseCooldown += random.nextInt(20); // +0-19 ticks adicionales
                }

                this.attackCooldown = baseCooldown;
                this.guardian.performRangedAttack(this.target, 1.0F);
            }
        }
    }

    @Override
    protected void dropCustomDeathLoot(DamageSource source, int looting, boolean recentlyHit) {
        super.dropCustomDeathLoot(source, looting, recentlyHit);

        // Petrified Core (prob. media - 40%)
        if (this.random.nextFloat() < 0.4f) {
            this.spawnAtLocation(new ItemStack(ModItems.PETRIFIED_CORE.get(), 1));
        }
    }
}