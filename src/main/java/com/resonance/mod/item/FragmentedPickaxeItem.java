package com.resonance.mod.item;

import com.resonance.mod.MobInfectionHandler;
import com.resonance.mod.entity.*;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

/**
 * Fragmented Pickaxe — mayor daño a entidades del mod que cualquier pico vanilla.
 * Mina Corrupted Mineral más rápido. Única herramienta capaz de obtener Chips Statue.
 *
 * FIX: antes extendía Item genérico sin tier, haciendo 1 de daño siempre.
 * Ahora extiende PickaxeItem con tier custom superior al diamante.
 */
public class FragmentedPickaxeItem extends PickaxeItem {

    // Multiplicador de daño extra contra mobs del mod
    private static final float MOD_MOB_DAMAGE_MULTIPLIER = 2.5f;

    public FragmentedPickaxeItem(Properties properties) {
        super(FRAGMENTED_TIER, 5, -2.8f, properties);
    }

    @Override
    public float getAttackDamageBonus(LivingEntity entity, float damage) {
        // Daño extra contra mobs del mod
        if (entity instanceof ChipsEntity
                || entity instanceof MineEntity
                || entity instanceof RaliteEntity
                || entity instanceof AshenKnightEntity
                || entity instanceof MineralGuardianEntity
                || entity instanceof MineralColossusEntity) {
            return damage * MOD_MOB_DAMAGE_MULTIPLIER;
        }
        return damage;
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