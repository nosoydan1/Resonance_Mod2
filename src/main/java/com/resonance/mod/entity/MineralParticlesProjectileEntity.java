package com.resonance.mod.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import com.resonance.mod.registry.ModItems;

public class MineralParticlesProjectileEntity extends ThrowableItemProjectile {
    public MineralParticlesProjectileEntity(EntityType<? extends ThrowableItemProjectile> type, Level level) {
        super(type, level);
    }

    @Override
    protected Item getDefaultItem() {
        return ModItems.MINERAL_PARTICLES_VIAL.get();
    }
}