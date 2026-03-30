package com.resonance.mod.entity;

import com.resonance.mod.registry.ModItems;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.ThrownPotion;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;

/**
 * Proyectil de Mineral Particles — se lanza como ThrownPotion para
 * aprovechar su física y renderer nativos. No crashea al summonear
 * porque ThrownPotion ya tiene renderer registrado en vanilla.
 *
 * FIX: la versión anterior extendía ThrowableItemProjectile directamente
 * sin renderer propio, causando crash al spawnear con /summon.
 */
public class MineralParticlesProjectileEntity extends ThrownPotion {

    public MineralParticlesProjectileEntity(EntityType<? extends ThrownPotion> type, Level level) {
        super(type, level);
        // Asignar item por defecto para que no crashee al serializar
        this.setItem(new ItemStack(ModItems.MINERAL_PARTICLES_VIAL.get()));
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);
        // El efecto real se maneja en SplashMineralParticlesItem.onProjectileImpact
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
    }
}