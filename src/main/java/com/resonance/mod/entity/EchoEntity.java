package com.resonance.mod.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class EchoEntity extends Monster {

    private MineralColossusEntity parent; // Referencia opcional
    private int age = 0;
    private static final int MAX_AGE = 200; // 10 segundos (20 ticks/segundo)

    public EchoEntity(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 6.0)
                .add(Attributes.ATTACK_DAMAGE, 0.1)
                .add(Attributes.MOVEMENT_SPEED, 0.35)   // 120% sprint ≈ 0.35 * 1.2? Ajustable
                .add(Attributes.FOLLOW_RANGE, 16.0);
    }

    @Override
    protected void registerGoals() {
        // Atacar al jugador más cercano
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, true));
        // Ataque cuerpo a cuerpo
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.2, true));
    }

    @Override
    public void tick() {
        super.tick();
        age++;
        if (age >= MAX_AGE) {
            this.discard(); // Elimina la entidad
        }
    }

    public int getAge() {
        return age;
    }

    public int getMaxAge() {
        return MAX_AGE;
    }

    public void setParent(MineralColossusEntity parent) {
        this.parent = parent;
    }

    // Opcional: lógica de enjambre (puede añadirse después)
}