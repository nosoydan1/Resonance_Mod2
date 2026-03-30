package com.resonance.mod.item;

import com.resonance.mod.ResonanceData;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * Antídoto — componente intermedio para craftear la Dissonant Injection (GDD §6.4).
 * También puede usarse directamente para reducir la Resonancia 25 puntos,
 * sin el efecto completo de la inyección.
 */
public class AntidoteItem extends Item {

    private static final float RESONANCE_REDUCTION = 15.0f;

    public AntidoteItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (level.isClientSide()) return InteractionResultHolder.success(stack);
        if (!ResonanceData.isMarked(player)) return InteractionResultHolder.pass(stack);

        float current = ResonanceData.getResonance(player);

        if (current <= 0f) {
            player.sendSystemMessage(Component.literal(
                    "§7No tienes resonancia que reducir."));
            return InteractionResultHolder.pass(stack);
        }

        // Reducir Resonancia 25 puntos
        ResonanceData.reduceResonance(player, RESONANCE_REDUCTION);

        // Limpiar debuffs activos si la Resonancia bajó por debajo de los umbrales
        float newResonance = ResonanceData.getResonance(player);
        if (newResonance < 80f) player.removeEffect(MobEffects.WEAKNESS);
        if (newResonance < 50f) player.removeEffect(MobEffects.DIG_SLOWDOWN);
        if (newResonance < 20f) player.removeEffect(MobEffects.MOVEMENT_SLOWDOWN);

        player.sendSystemMessage(Component.literal(
                "§aEl antídoto reduce tu resonancia en " + RESONANCE_REDUCTION + " puntos."));

        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }

        return InteractionResultHolder.success(stack);
    }
}