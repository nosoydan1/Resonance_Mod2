package com.resonance.mod.entity;

import com.resonance.mod.MobSpawnHandler;
import com.resonance.mod.registry.ModItems;
import net.minecraft.network.chat.Component;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.core.BlockPos;
import com.resonance.mod.MobInfectionHandler;

public class MineralColossusEntity extends Monster {

    // Fase de combate actual: 1, 2 o 3
    private int combatPhase = 1;

    public MineralColossusEntity(EntityType<? extends Monster> type, Level level) {
        super(type, level);
        // Permitir que el Coloso destruya bloques al moverse
        this.setCanPickUpLoot(false);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 1400.0)
                .add(Attributes.ATTACK_DAMAGE, 15.0)
                .add(Attributes.MOVEMENT_SPEED, 0.15)
                .add(Attributes.FOLLOW_RANGE, 64.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0)
                // FIX: forzar armor para que los hits se sientan correctamente
                .add(Attributes.ARMOR, 10.0)
                .add(Attributes.ARMOR_TOUGHNESS, 4.0);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new net.minecraft.world.entity.ai.goal.MeleeAttackGoal(
                this, 0.8, true) {
            @Override
            public void tick() {
                super.tick();
                // Destruir bloques que bloqueen el camino
                BlockPos pos = MineralColossusEntity.this.blockPosition();
                for (int dy = 0; dy <= 4; dy++) {
                    BlockPos check = pos.above(dy);
                    net.minecraft.world.level.block.state.BlockState state =
                            MineralColossusEntity.this.level().getBlockState(check);
                    if (!state.isAir()
                            && state.getBlock() != net.minecraft.world.level.block.Blocks.BEDROCK
                            && !(state.getBlock() instanceof com.resonance.mod.block.CorruptedMineralBlock)) {
                        MineralColossusEntity.this.level().destroyBlock(check, true);
                    }
                }
            }
        });
        this.goalSelector.addGoal(2, new WaterAvoidingRandomStrollGoal(this, 0.6));

        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(
                this, LivingEntity.class, true,
                e -> {
                    if (MobInfectionHandler.isModMob(e)) return false;
                    if (e instanceof net.minecraft.world.entity.Mob
                            && MobInfectionHandler.isInfected(e)
                            && !MobSpawnHandler.isColossusSpawned()) return false;
                    return true;
                }
        ));
    }

    public int getCombatPhase() {
        return combatPhase;
    }

    /** Permite forzar la fase desde comandos de debug. */
    public void setCombatPhase(int phase) {
        this.combatPhase = Math.max(1, Math.min(3, phase));
        applyPhaseAttributes();
        announcePhase();
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide()) return;

        updateCombatPhase();
    }

    private void updateCombatPhase() {
        float maxHp = (float) this.getAttributeValue(Attributes.MAX_HEALTH);
        float currentHp = this.getHealth();
        float hpPercent = currentHp / maxHp;

        int newPhase;
        if (hpPercent > 0.60f)      newPhase = 1;
        else if (hpPercent > 0.30f) newPhase = 2;
        else                         newPhase = 3;

        if (newPhase != combatPhase) {
            combatPhase = newPhase;
            applyPhaseAttributes();
            announcePhase();
        }
    }

    private void applyPhaseAttributes() {
        // GDD §5.6: reducción de daño recibido y cambios por fase
        switch (combatPhase) {
            case 1 -> {
                // Sin bonus de reducción
                this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0.15);
            }
            case 2 -> {
                // +25% reducción de daño — se maneja en ColossusHurtHandler
                this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0.15);
            }
            case 3 -> {
                // +45% reducción, ligeramente más lento
                this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0.12);
            }
        }
    }

    private void announcePhase() {
        String msg = switch (combatPhase) {
            case 1 -> "§c§l[COLOSO — FASE 1: El Coloso comienza a despertar]";
            case 2 -> "§4§l[COLOSO — FASE 2: ¡El Coloso desata su poder!]";
            case 3 -> "§5§l[COLOSO — FASE 3: ¡El Coloso agoniza... y se vuelve más peligroso!]";
            default -> "";
        };
        this.level().players().forEach(p ->
                p.sendSystemMessage(Component.literal(msg)));
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        // Reducción de daño por fase (GDD §5.6)
        float reduced = switch (combatPhase) {
            case 2 -> amount * 0.75f; // 25% reducción
            case 3 -> amount * 0.55f; // 45% reducción
            default -> amount;
        };
        return super.hurt(source, reduced);
    }

    @Override
    protected void dropCustomDeathLoot(DamageSource source, int looting, boolean recentlyHit) {
        super.dropCustomDeathLoot(source, looting, recentlyHit);
        this.spawnAtLocation(new ItemStack(ModItems.HEART_OF_COLOSSUS.get(), 1));
        int crystalCount = 3 + this.random.nextInt(4);
        this.spawnAtLocation(new ItemStack(ModItems.COMPACT_CRYSTAL.get(), crystalCount));
        this.spawnAtLocation(new ItemStack(ModItems.SILENT_SHARDS.get(),
                1 + this.random.nextInt(3)));
    }

    @Override
    public void die(DamageSource source) {
        super.die(source);
        if (!this.level().isClientSide()) {
            this.level().players().forEach(p ->
                    p.sendSystemMessage(Component.literal(
                            "§5§l[EL COLOSO HA CAÍDO. LA INFECCIÓN SE DISIPA...]")));
            MobSpawnHandler.onColossusDefeated();
        }
    }
}