package com.resonance.mod;

public class ClientResonanceData {
    private static float resonance = 0f;
    private static boolean marked = false;

    public static float getResonance() { return resonance; }
    public static boolean isMarked() { return marked; }
    public static void setResonance(float value) { resonance = value; }
    public static void setMarked(boolean marked) { ClientResonanceData.marked = marked; }
}