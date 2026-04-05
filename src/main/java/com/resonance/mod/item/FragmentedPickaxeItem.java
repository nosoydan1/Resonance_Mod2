package com.resonance.mod.item;

import com.resonance.mod.MobInfectionHandler;
import com.resonance.mod.entity.*;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.entity.ai.attributes.Attributes;

/**
 * Fragmented Pickaxe — mayor daño a entidades del mod que cualquier pico vanilla.
 * Mina Corrupted Mineral más rápido. Única herramienta capaz de obtener Chips Statue.
 *
 * FIX: antes extendía Item genérico sin tier, haciendo 1 de daño siempre.
 * Ahora extiende PickaxeItem con tier custom superior al diamante.
 */
public class FragmentedPickaxeItem extends PickaxeItem {

    // Multiplicador de daño extra contra mobs del mod
    private static final float MOD_MOB_DAMAGE_MULTIPLIER = 0.5f; // +50% de daño

    public FragmentedPickaxeItem(Properties properties) {
        super(FRAGMENTED_TIER, 5, -2.8f, properties);
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        // 1. Aplica el daño normal del arma
        boolean result = super.hurtEnemy(stack, target, attacker);

        // 2. Si el golpe fue exitoso y es un mob del mod, aplica daño extra proporcional
        if (result && (target instanceof ChipsEntity
                || target instanceof MineEntity
                || target instanceof RaliteEntity
                || target instanceof AshenKnightEntity
                || target instanceof MineralGuardianEntity
                || target instanceof MineralColossusEntity)) {

            // Obtener el daño base del arma (ej: 6.0 para pico de diamante)
            double weaponDamage = attacker.getAttributeValue(Attributes.ATTACK_DAMAGE);

            // Calcular daño extra: weaponDamage * MULTIPLICADOR (ej: 6.0 * 0.5 = +3.0)
            float extraDamage = (float) (weaponDamage * MOD_MOB_DAMAGE_MULTIPLIER);

            // Aplicar el daño extra (misma fuente de daño que el ataque normal)
            target.hurt(attacker.damageSources().mobAttack(attacker), extraDamage);
        }

        return result;
    }

    // Tier custom: mejor que diamante, peor que netherite en minado
    // pero con daño superior contra mobs del mod
    private static final Tier FRAGMENTED_TIER = new Tier() {
        @Override public int getUses() { return 1500; }
        @Override public float getSpeed() { return 9.0f; }
        @Override public float getAttackDamageBonus() { return 5.0f; } // +5 base = 10 daño total
        @Override public int getLevel() { return 4; } // nivel diamante+
        @Override public int getEnchantmentValue() { return 15; }
        @Override public Ingredient getRepairIngredient() {
            return Ingredient.of(
                    net.minecraft.world.item.Items.DIAMOND); // placeholder hasta tener item propio
        }
    };
}