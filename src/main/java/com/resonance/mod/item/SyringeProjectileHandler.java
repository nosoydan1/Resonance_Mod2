package com.resonance.mod;

import com.resonance.mod.entity.ChipsEntity;
import com.resonance.mod.entity.MineEntity;
import com.resonance.mod.entity.RaliteEntity;
import com.resonance.mod.entity.AshenKnightEntity;
import com.resonance.mod.entity.MineralGuardianEntity;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrownPotion;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Procesa el impacto del proyectil de la Syringe Gun.
 *
 * Si impacta a un jugador: aplica el mismo efecto que usar la
 * Dissonant Injection directamente (resetea Resonancia, limpia debuffs).
 *
 * Si impacta a un mob infectado: lo desinfecta (quita el tag y el nombre custom).
 *
 * Si impacta a un mob del mod: no hace nada (son entidades minerales,
 * no tienen Resonancia ni infección vanilla).
 */
@Mod.EventBusSubscriber(modid = ResonanceMod.MODID)
public class SyringeProjectileHandler {

    @SubscribeEvent
    public static void onProjectileImpact(ProjectileImpactEvent event) {
        if (!(event.getProjectile() instanceof ThrownPotion potion)) return;
        if (!potion.getPersistentData().getBoolean("ResonanceSyringe")) return;

        // Cancelar el comportamiento vanilla de la poción
        event.setCanceled(true);
        potion.discard();

        if (event.getRayTraceResult() instanceof net.minecraft.world.phys.EntityHitResult entityHit) {
            if (entityHit.getEntity() instanceof Player target) {
                applyToPlayer(target);
            } else if (entityHit.getEntity() instanceof LivingEntity le) {
                applyToMob(le);
            }
        }
    }

    private static void applyToPlayer(Player player) {
        if (!ResonanceData.isMarked(player)) return;

        ResonanceData.setResonance(player, 0f);
        player.removeEffect(MobEffects.MOVEMENT_SLOWDOWN);
        player.removeEffect(MobEffects.DIG_SLOWDOWN);
        player.removeEffect(MobEffects.WEAKNESS);

        player.sendSystemMessage(Component.literal(
                "§aLa inyección neutraliza tu resonancia mineral."));
    }

    private static void applyToMob(LivingEntity entity) {
        // No afecta a mobs propios del mod
        if (entity instanceof ChipsEntity || entity instanceof MineEntity
                || entity instanceof RaliteEntity || entity instanceof AshenKnightEntity
                || entity instanceof MineralGuardianEntity) return;

        // Desinfectar mob vanilla infectado
        if (MobInfectionHandler.isInfected(entity)) {
            entity.getPersistentData().putBoolean("Resonance_Infected", false);
            entity.setCustomName(null);
            entity.setCustomNameVisible(false);
            entity.sendSystemMessage(Component.literal("§aDesinfectado."));
        }
    }
}