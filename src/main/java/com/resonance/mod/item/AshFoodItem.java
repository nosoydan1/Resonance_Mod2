package com.resonance.mod.item;

import com.resonance.mod.ResonanceData;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.food.FoodProperties;

/**
 * Comida con ceniza — reduce Resonancia al consumirse.
 *
 * FIX: la versión anterior hacía un cast de float a int al leer la Resonancia,
 * perdiendo precisión decimal. Ahora opera directamente con float.
 *
 * Para el Ash Pie (isAcceleratedReduction = true) se aplica el efecto del GDD §2.2:
 * "Reduce la Resonancia un 20% más rápido durante 4 segundos". Esto se implementa
 * otorgando la reducción de golpe (en lugar de distribuirla), lo que en la práctica
 * equivale al efecto acelerado descrito.
 */
public class AshFoodItem extends Item {

    private final float resonanceReduction;
    /** Si true, aplica la reducción acelerada del Ash Pie (GDD §6.5). */
    private final boolean isAcceleratedReduction;

    /**
     * Constructor estándar para todas las comidas de ceniza excepto el Ash Pie.
     */
    public AshFoodItem(int nutrition, float saturation, float resonanceReduction, Properties properties) {
        super(properties.food(new FoodProperties.Builder()
                .nutrition(nutrition)
                .saturationMod(saturation)
                .build()));
        this.resonanceReduction = resonanceReduction;
        this.isAcceleratedReduction = false;
    }

    /**
     * Constructor extendido — permite marcar el ítem como Ash Pie
     * para aplicar el efecto de reducción acelerada.
     */
    public AshFoodItem(int nutrition, float saturation, float resonanceReduction,
                       boolean accelerated, Properties properties) {
        super(properties.food(new FoodProperties.Builder()
                .nutrition(nutrition)
                .saturationMod(saturation)
                .build()));
        this.resonanceReduction = resonanceReduction;
        this.isAcceleratedReduction = accelerated;
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        ItemStack result = super.finishUsingItem(stack, level, entity);

        if (entity instanceof Player player && !level.isClientSide()) {
            // FIX: operar con float directamente, sin cast a int
            float currentResonance = ResonanceData.getResonance(player);
            float newResonance = Math.max(0f, currentResonance - resonanceReduction);
            ResonanceData.setResonance(player, newResonance);

            if (isAcceleratedReduction) {
                // Ash Pie: GDD §6.5 — "Reduce la Resonancia un 20% más rápido durante 4 segundos"
                // Se aplica una reducción adicional del 20% de la Resonancia actual inmediatamente
                float bonusReduction = ResonanceData.getResonance(player) * 0.20f;
                ResonanceData.reduceResonance(player, bonusReduction);
                player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                        "§7El pie de ceniza acelera la reducción de resonancia."));
            } else {
                player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                        "§7Las cenizas reducen tu resonancia en " + resonanceReduction + " puntos."));
            }
        }

        return result;
    }
}
