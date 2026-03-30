package com.resonance.mod;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;

public class ResonanceData {

    private static final String KEY_RESONANCE = "ResonanceLevel";
    private static final String KEY_MARKED = "ResonanceMarked";

    /**
     * Forge provee getPersistentData() que persiste entre muertes.
     * Sin embargo para datos que deben sobrevivir la muerte usamos
     * el tag raíz del jugador directamente via getExistingData().
     * FIX: los datos se perdían al morir porque se guardaban en el
     * CompoundTag temporal del jugador, no en el persistente de Forge.
     */
    private static CompoundTag getTag(Player player) {
        return player.getPersistentData()
                .getCompound(net.minecraftforge.common.util.Constants.NBT.TAG_COMPOUND + "");
    }

    public static float getResonance(Player player) {
        CompoundTag tag = player.getPersistentData();
        return tag.contains(KEY_RESONANCE) ? tag.getFloat(KEY_RESONANCE) : 0f;
    }

    public static void setResonance(Player player, float value) {
        float clamped = Math.max(0f, Math.min(100f, value));
        player.getPersistentData().putFloat(KEY_RESONANCE, clamped);
    }

    public static void addResonance(Player player, float amount) {
        setResonance(player, getResonance(player) + amount);
    }

    public static void reduceResonance(Player player, float amount) {
        setResonance(player, getResonance(player) - amount);
    }

    public static boolean isMarked(Player player) {
        return player.getPersistentData().getBoolean(KEY_MARKED);
    }

    public static void markPlayer(Player player) {
        player.getPersistentData().putBoolean(KEY_MARKED, true);
    }
}