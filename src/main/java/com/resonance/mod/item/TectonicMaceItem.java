package com.resonance.mod.item;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class TectonicMaceItem extends Item {

    public TectonicMaceItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        boolean result = super.hurtEnemy(stack, target, attacker);

        if (result && target instanceof net.minecraft.world.entity.LivingEntity) {
            // Knockback extra
            double knockbackStrength = 1.2;
            target.knockback(
                    knockbackStrength,
                    attacker.getX() - target.getX(),
                    attacker.getZ() - target.getZ()
            );
        }

        return result;
    }
}