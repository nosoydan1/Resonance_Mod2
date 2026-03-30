package com.resonance.mod;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Skeleton;
import net.minecraft.world.entity.monster.Blaze;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.monster.ElderGuardian;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Mecánicas especiales de mobs vanilla infectados (GDD §2.4):
 *
 * Skeleton infectado:  flechas aplican Slow 30–40% durante 1–2s
 * Blaze infectado:     proyectiles aplican Slow 5% durante 1–2s
 * Bosses infectados:   75% de resistencia al daño recibido
 */
@Mod.EventBusSubscriber(modid = ResonanceMod.MODID)
public class InfectedMobBehaviorHandler {

    // -------------------------------------------------------------------------
    // Flechas de Skeleton infectado → Slow al impactar
    // -------------------------------------------------------------------------
    @SubscribeEvent
    public static void onProjectileImpact(ProjectileImpactEvent event) {
        Projectile projectile = event.getProjectile();

        // Solo flechas
        if (!(projectile instanceof AbstractArrow arrow)) return;

        // El dueño debe ser un Skeleton infectado
        if (!(arrow.getOwner() instanceof Skeleton skeleton)) return;
        if (!MobInfectionHandler.isInfected(skeleton)) return;

        // Solo si impacta a una entidad viva
        if (!(event.getRayTraceResult() instanceof net.minecraft.world.phys.EntityHitResult hit)) return;
        if (!(hit.getEntity() instanceof LivingEntity target)) return;

        // Slow entre 1 y 2 segundos (20–40 ticks), amplificador 0 = Slow I
        int duration = 20 + (int)(Math.random() * 20); // 20–40 ticks
        target.addEffect(new MobEffectInstance(
                MobEffects.MOVEMENT_SLOWDOWN, duration, 0, false, true));
    }

    // -------------------------------------------------------------------------
    // Blaze infectado → proyectil aplica Slow al impactar
    // -------------------------------------------------------------------------
    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        LivingEntity victim = event.getEntity();

        // Verificar si la fuente de daño es un Blaze infectado
        if (event.getSource().getEntity() instanceof Blaze blaze
                && MobInfectionHandler.isInfected(blaze)) {

            // Slow 5% = Slow I muy corto (1–2 segundos)
            int duration = 20 + (int)(Math.random() * 20);
            victim.addEffect(new MobEffectInstance(
                    MobEffects.MOVEMENT_SLOWDOWN, duration, 0, false, true));
        }

        // -------------------------------------------------------------------------
        // Bosses infectados: 75% de resistencia al daño recibido
        // -------------------------------------------------------------------------
        if (victim instanceof WitherBoss || victim instanceof ElderGuardian
                || victim instanceof EnderDragon) {

            if (!MobInfectionHandler.isInfected(victim)) return;

            // 75% de probabilidad de reducir el daño a 0
            if (Math.random() < 0.75) {
                event.setAmount(0f);
            }
        }
    }
}