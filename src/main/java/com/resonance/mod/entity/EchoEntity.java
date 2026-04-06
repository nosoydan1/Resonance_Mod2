package com.resonance.mod.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.projectile.SmallFireball;
import net.minecraft.world.level.Level;

public class EchoEntity extends PathfinderMob {

    private static final int LIFESPAN_TICKS = 300; // 15 segundos
    private int ageTicks = 0;
    private MineralColossusEntity parent;
    private double orbitRadius = 3.0;
    private double orbitAngle = 0;

    public EchoEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
        this.setNoGravity(true);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 15.0)
                .add(Attributes.MOVEMENT_SPEED, 0.0)
                .add(Attributes.FOLLOW_RANGE, 32.0);
    }

    public void setParent(MineralColossusEntity parent) {
        this.parent = parent;
    }

    @Override
    protected void registerGoals() {
        // Sin goals — movimiento controlado manualmente
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide()) return;

        ageTicks++;
        if (ageTicks >= LIFESPAN_TICKS) {
            this.discard();
            return;
        }

        if (parent != null && parent.isAlive()) {
            // Órbita alrededor del Coloso
            orbitAngle += 0.05; // Velocidad de rotación
            double x = parent.getX() + Math.cos(orbitAngle) * orbitRadius;
            double y = parent.getY() + 3.0; // Altura sobre el Coloso
            double z = parent.getZ() + Math.sin(orbitAngle) * orbitRadius;

            this.setPos(x, y, z);

            // Cada 40 ticks, disparar un proyectil
            if (ageTicks % 40 == 0 && parent.getTarget() != null) {
                fireProjectile(parent.getTarget());
            }
        } else {
            // Parent muerto, desaparecer
            this.discard();
        }
    }

    private void fireProjectile(net.minecraft.world.entity.LivingEntity target) {
        if (this.level().isClientSide()) return;

        double dx = target.getX() - this.getX();
        double dy = target.getY() + target.getEyeHeight() - this.getY();
        double dz = target.getZ() - this.getZ();
        double distance = Math.sqrt(dx * dx + dz * dz);

        SmallFireball fireball = new SmallFireball(
                this.level(),
                this,
                dx / distance * 0.6,
                dy / distance * 0.4,
                dz / distance * 0.6
        );

        fireball.setPos(this.getX(), this.getY() + 0.5, this.getZ());
        this.level().addFreshEntity(fireball);
    }

    @Override
    protected void dropCustomDeathLoot(DamageSource source, int looting, boolean recentlyHit) {
        super.dropCustomDeathLoot(source, looting, recentlyHit);
        // Drop: Echo Residue (10% probability)
        if (this.random.nextFloat() < 0.1f) {
            this.spawnAtLocation(
                    new net.minecraft.world.item.ItemStack(
                            com.resonance.mod.registry.ModItems.ECHO_RESIDUE.get(), 1
                    )
            );
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("AgeTicks", ageTicks);
        tag.putDouble("OrbitAngle", orbitAngle);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        ageTicks = tag.getInt("AgeTicks");
        orbitAngle = tag.getDouble("OrbitAngle");
    }

    // ===== MÉTODOS NUEVOS PARA EL RENDERIZADOR =====
    public int getAge() {
        return ageTicks;
    }

    public int getMaxAge() {
        return LIFESPAN_TICKS;
    }
}