package com.resonance.mod;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Copia los datos de Resonancia del jugador original al nuevo jugador
 * tras respawnear o cambiar de dimensión.
 *
 * FIX: getPersistentData() NO se copia automáticamente al respawnear
 * en Forge 1.20.1 — hay que hacerlo manualmente en PlayerRespawnEvent.
 */
@Mod.EventBusSubscriber(modid = ResonanceMod.MODID)
public class PlayerRespawnHandler {

    private static final String KEY_RESONANCE = "ResonanceLevel";
    private static final String KEY_MARKED = "ResonanceMarked";

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        Player original = event.getOriginalPlayer();
        Player newPlayer = event.getEntity();

        CompoundTag oldData = original.getPersistentData();
        CompoundTag newData = newPlayer.getPersistentData();

        // Copiar marcado y resonancia al jugador respawneado
        if (oldData.contains(KEY_MARKED)) {
            newData.putBoolean(KEY_MARKED, oldData.getBoolean(KEY_MARKED));
        }
        if (oldData.contains(KEY_RESONANCE)) {
            // Al morir la resonancia baja pero no se pierde el marcado
            float resonance = oldData.getFloat(KEY_RESONANCE);
            // Penalización por muerte: reduce 20% de la resonancia actual
            float penalty = resonance * 0.20f;
            newData.putFloat(KEY_RESONANCE, Math.max(0f, resonance - penalty));
        }
    }

    @SubscribeEvent
    public static void onPlayerChangeDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        // Los datos persisten al cambiar dimensión — no necesita acción
        // pero lo dejamos como hook para futuras dimensiones infectadas
    }
}