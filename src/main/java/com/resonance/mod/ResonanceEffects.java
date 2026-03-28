package com.resonance.mod;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ResonanceMod.MODID)
public class ResonanceEffects {

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (event.player.level().isClientSide()) return;

        Player player = event.player;
        float resonance = ResonanceData.getResonance(player);

        // Umbral 20% — Lentitud
        if (resonance >= 20f) {
            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 40, 0, false, false));
        }

        // Umbral 50% — Fatiga minera
        if (resonance >= 50f) {
            player.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 40, 1, false, false));
        }

        // Umbral 80% — Debilidad
        if (resonance >= 80f) {
            player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 40, 0, false, false));
        }

        // Umbral 100% — Petrificación (por ahora mata al jugador como placeholder)
        if (resonance >= 100f) {
            player.hurt(player.level().damageSources().magic(), Float.MAX_VALUE);
        }
    }
}