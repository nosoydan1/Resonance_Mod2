package com.resonance.mod.entity;

import com.resonance.mod.MobSpawnHandler;
import com.resonance.mod.MobInfectionHandler;
import com.resonance.mod.registry.ModEntities;
import com.resonance.mod.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
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
import net.minecraft.world.phys.AABB;

public class MineralColossusEntity extends Monster {

    private int combatPhase = 1; // 1, 2 o 3

    public MineralColossusEntity(EntityType<? extends Monster> type, Level level) {
        super(type, level);
        this.setCanPickUpLoot(false);
        this.setPersistenceRequired();
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 1400.0)
                .add(Attributes.ATTACK_DAMAGE, 15.0)
                .add(Attributes.MOVEMENT_SPEED, 0.15)
                .add(Attributes.FOLLOW_RANGE, 64.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0)
                .add(Attributes.ARMOR, 10.0)
                .add(Attributes.ARMOR_TOUGHNESS, 4.0);
        // NOTA: El escalado de vida por jugadores cercanos se aplica en el método tick()
    }

    @Override
    protected void registerGoals() {
        // Ataque cuerpo a cuerpo con destrucción de bloques
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 0.8, true) {
            @Override
            public void tick() {
                super.tick();
                // Destruir bloques que bloqueen el camino
                BlockPos pos = MineralColossusEntity.this.blockPosition();
                for (int dy = 0; dy <= 4; dy++) {
                    BlockPos check = pos.above(dy);
                    var state = MineralColossusEntity.this.level().getBlockState(check);
                    if (!state.isAir()
                            && state.getBlock() != net.minecraft.world.level.block.Blocks.BEDROCK
                            && !(state.getBlock() instanceof com.resonance.mod.block.CorruptedMineralBlock)) {
                        MineralColossusEntity.this.level().destroyBlock(check, true);
                    }
                }
            }
        });

        // Escalado de vida por jugadores cercanos (cada 100 ticks)
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

    @Override
    public void tick() {
        super.tick();

        // Escalar vida según jugadores cercanos (cada 100 ticks)
        if (!this.level().isClientSide && this.tickCount % 100 == 0) {
            int nearbyPlayers = (int) this.level().getNearbyPlayers(
                    net.minecraft.world.entity.ai.targeting.TargetingConditions.forCombat(),
                    this, this.getBoundingBox().inflate(64)).size();
            double baseHealth = 1400.0;
            double scaledHealth = baseHealth + (nearbyPlayers - 1) * 700;
            this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(scaledHealth);
        }

        // Fase 3: invocar Echos
        if (this.combatPhase == 3 && this.level() instanceof ServerLevel serverLevel) {
            if (this.tickCount % 60 == 0 && this.getTarget() != null) {
                spawnEcho(serverLevel);
            }
        }

        updateCombatPhase();
    }

    private void spawnEcho(ServerLevel level) {
        if (level.getEntitiesOfClass(EchoEntity.class, this.getBoundingBox().inflate(10)).size() >= 3) {
            return;
        }
        EchoEntity echo = ModEntities.ECHO.get().create(level);
        if (echo == null) return;
        double angle = Math.random() * Math.PI * 2;
        double x = this.getX() + Math.cos(angle) * 5;
        double z = this.getZ() + Math.sin(angle) * 5;
        echo.moveTo(x, this.getY() + 3, z, 0, 0);
        echo.setParent(this);
        level.addFreshEntity(echo);
    }

    private void updateCombatPhase() {
        float maxHp = (float) this.getAttributeValue(Attributes.MAX_HEALTH);
        float currentHp = this.getHealth();
        float hpPercent = currentHp / maxHp;
        int newPhase;
        if (hpPercent > 0.60f) newPhase = 1;
        else if (hpPercent > 0.30f) newPhase = 2;
        else newPhase = 3;
        if (newPhase != combatPhase) {
            combatPhase = newPhase;
            applyPhaseAttributes();
            announcePhase();
        }
    }

    private void applyPhaseAttributes() {
        switch (combatPhase) {
            case 1 -> {
                this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0.15);
                // Sin efecto visual
            }
            case 2 -> {
                this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0.15);
                if (this.level() instanceof ServerLevel serverLevel) {
                    serverLevel.sendParticles(
                            net.minecraft.core.particles.ParticleTypes.DAMAGE_INDICATOR,
                            this.getX(), this.getY() + 2, this.getZ(),
                            30, 2, 2, 2, 0.1
                    );
                }
            }
            case 3 -> {
                this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0.12);
                if (this.level() instanceof ServerLevel serverLevel) {
                    serverLevel.sendParticles(
                            net.minecraft.core.particles.ParticleTypes.WITCH,
                            this.getX(), this.getY() + 2, this.getZ(),
                            50, 2, 2, 2, 0.15
                    );
                }
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
        this.level().players().forEach(p -> {
            p.sendSystemMessage(Component.literal(msg));
            p.playNotifySound(SoundEvents.WARDEN_NEARBY_CLOSE, SoundSource.HOSTILE, 1.0f, 1.0f);
        });
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        float reduced = switch (combatPhase) {
            case 2 -> amount * 0.75f;
            case 3 -> amount * 0.55f;
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
        this.spawnAtLocation(new ItemStack(ModItems.SILENT_SHARDS.get(), 1 + this.random.nextInt(3)));
    }

    @Override
    public void die(DamageSource source) {
        super.die(source);
        if (!this.level().isClientSide()) {
            this.level().players().forEach(p ->
                    p.sendSystemMessage(Component.literal("§5§l[EL COLOSO HA CAÍDO. LA INFECCIÓN SE DISIPA...]")));
            MobSpawnHandler.onColossusDefeated();
        }
    }

    public int getCombatPhase() {
        return combatPhase;
    }

    public void setCombatPhase(int phase) {
        this.combatPhase = Math.max(1, Math.min(3, phase));
        applyPhaseAttributes();
        announcePhase();
    }
}