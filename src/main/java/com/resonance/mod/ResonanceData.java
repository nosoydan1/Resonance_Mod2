package com.resonance.mod;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;

public class ResonanceData {

    private static final String KEY_RESONANCE = "ResonanceLevel";
    private static final String KEY_MARKED = "ResonanceMarked";

    // El método getTag erróneo ha sido eliminado.

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