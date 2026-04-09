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

    private static final String TAG_PETRIFYING = "Resonance_Petrifying";
    private static final String TAG_PETRIFY_TICKS = "Resonance_PetrifyTicks";

    public static boolean isPetrifying(Player player) {
        return player.getPersistentData().getBoolean(TAG_PETRIFYING);
    }

    public static void startPetrification(Player player, int ticks) {
        player.getPersistentData().putBoolean(TAG_PETRIFYING, true);
        player.getPersistentData().putInt(TAG_PETRIFY_TICKS, ticks);
    }

    public static int getPetrifyTicksLeft(Player player) {
        return player.getPersistentData().getInt(TAG_PETRIFY_TICKS);
    }

    public static void decrementPetrifyTicks(Player player) {
        int ticks = getPetrifyTicksLeft(player);
        if (ticks > 0) {
            player.getPersistentData().putInt(TAG_PETRIFY_TICKS, ticks - 1);
        }
    }

    public static void clearPetrification(Player player) {
        player.getPersistentData().remove(TAG_PETRIFYING);
        player.getPersistentData().remove(TAG_PETRIFY_TICKS);
    }
}