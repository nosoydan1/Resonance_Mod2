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
 * Dissonant Injection — evita o revierte la petrificación antes del 100% (GDD §6.4).
 * Al usarse: resetea Resonancia a 0, limpia los efectos de debuff activos.
 */
public class DissonantInjectionItem extends Item {

    public DissonantInjectionItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (level.isClientSide()) return InteractionResultHolder.success(stack);
        if (!ResonanceData.isMarked(player)) return InteractionResultHolder.pass(stack);

        // Resetear Resonancia a 0
        float current = ResonanceData.getResonance(player);
        ResonanceData.setResonance(player, current * 0.30f); // reduce 70%

        // Limpiar debuffs activos causados por la Resonancia
        player.removeEffect(MobEffects.MOVEMENT_SLOWDOWN);
        player.removeEffect(MobEffects.DIG_SLOWDOWN);
        player.removeEffect(MobEffects.WEAKNESS);

        player.sendSystemMessage(Component.literal(
                "§aLa inyección disonante neutraliza la resonancia mineral."));

        // Consumir uno del stack
        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }

        return InteractionResultHolder.success(stack);
    }
}