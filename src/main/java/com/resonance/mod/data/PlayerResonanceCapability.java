package com.resonance.mod.data;

import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;

public class PlayerResonanceCapability implements INBTSerializable<CompoundTag> {

    public static final Capability<PlayerResonanceCapability> CAPABILITY =
            CapabilityManager.get(new CapabilityToken<>() {});

    private float resonance = 0f;
    private boolean marked = false;

    public float getResonance() {
        return resonance;
    }

    public void setResonance(float value) {
        this.resonance = Math.max(0f, Math.min(100f, value));
    }

    public void addResonance(float amount) {
        setResonance(resonance + amount);
    }

    public void reduceResonance(float amount) {
        setResonance(resonance - amount);
    }

    public boolean isMarked() {
        return marked;
    }

    public void markPlayer() {
        this.marked = true;
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putFloat("Resonance", resonance);
        tag.putBoolean("Marked", marked);
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        this.resonance = tag.getFloat("Resonance");
        this.marked = tag.getBoolean("Marked");
    }
}