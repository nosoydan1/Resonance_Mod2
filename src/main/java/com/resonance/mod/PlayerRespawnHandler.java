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
    public static void onPlayerClone(PlayerEvent.Clone event) {
        Player original = event.getOriginal();
        Player newPlayer = event.getEntity();

        CompoundTag oldData = original.getPersistentData();
        CompoundTag newData = newPlayer.getPersistentData();

        // Copiar marcado (si quieres que se mantenga)
        if (oldData.contains(KEY_MARKED)) {
            newData.putBoolean(KEY_MARKED, oldData.getBoolean(KEY_MARKED));
        }

        // Reiniciar resonancia a 0 al morir
        newData.putFloat(KEY_RESONANCE, 0f);
    }
}