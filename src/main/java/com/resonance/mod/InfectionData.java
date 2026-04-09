package com.resonance.mod;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod.EventBusSubscriber(modid = ResonanceMod.MODID)
public class InfectionData extends SavedData {

    private static final Logger LOGGER = LoggerFactory.getLogger(InfectionData.class);
    private static final String DATA_NAME = "resonance_infection";

    private int points = 0;
    private int phase = 1;
    private BlockPos nucleus = null;

    private static final int[] PHASE_THRESHOLDS = {
            0, 1500, 3225, 6881, 14090, 27478, 56108
    };

    private int currentRadius = 0;      // radio actual (en bloques)
    private int targetRadius = 0;       // radio al que queremos llegar (se actualiza con cada incremento)
    private long lastExpansionTick = 0; // tick del servidor cuando se expandió por última vez

    public static InfectionData get(Level level) {
        return level.getServer().overworld()
                .getDataStorage()
                .computeIfAbsent(InfectionData::load, InfectionData::new, DATA_NAME);
    }

    public static InfectionData load(CompoundTag tag) {
        InfectionData data = new InfectionData();
        data.points = tag.getInt("Points");
        data.phase = tag.getInt("Phase");
        data.currentRadius = tag.getInt("CurrentRadius");
        data.targetRadius = tag.getInt("TargetRadius");
        data.lastExpansionTick = tag.getLong("LastExpansionTick");

        if (tag.contains("NucleusX")) {
            data.nucleus = new BlockPos(
                    tag.getInt("NucleusX"),
                    tag.getInt("NucleusY"),
                    tag.getInt("NucleusZ")
            );
        }
        return data;
    }

    public static InfectionData get() {
        return null;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        tag.putInt("Points", points);
        tag.putInt("Phase", phase);
        tag.putInt("CurrentRadius", currentRadius);
        tag.putInt("TargetRadius", targetRadius);
        tag.putLong("LastExpansionTick", lastExpansionTick);
        if (nucleus != null) {
            tag.putInt("NucleusX", nucleus.getX());
            tag.putInt("NucleusY", nucleus.getY());
            tag.putInt("NucleusZ", nucleus.getZ());
        }
        return tag;
    }

    public int getPoints() { return points; }
    public int getPhase() { return phase; }
    public BlockPos getNucleus() { return nucleus; }

    public void setNucleus(BlockPos pos) {
        this.nucleus = pos;
        setDirty();
    }

    public void addPoints(int amount) {
        points += amount;
        updatePhase();
        setDirty();
    }

    private void updatePhase() {
        for (int i = PHASE_THRESHOLDS.length - 1; i >= 0; i--) {
            if (points >= PHASE_THRESHOLDS[i]) {
                phase = Math.min(i + 1, 6);
                break;
            }
        }
    }

    public static int getPointsForBlock(String blockId) {
        return switch (blockId) {
            case "minecraft:stone", "minecraft:dirt", "minecraft:sand" -> 1;
            case "minecraft:coal_ore", "minecraft:iron_ore" -> 3;
            case "minecraft:gold_ore", "minecraft:diamond_ore" -> 8;
            case "resonance:meteorite_rock" -> 15;
            default -> 1;
        };
    }

    public boolean tryExpandRadius(long currentTick, int intervalSeconds, int incrementBlocks, int maxRadius) {
        long ticksNeeded = intervalSeconds * 20L;
        if (currentTick - lastExpansionTick >= ticksNeeded && currentRadius < maxRadius) {
            int newRadius = Math.min(currentRadius + incrementBlocks, maxRadius);
            setCurrentRadius(newRadius);
            setLastExpansionTick(currentTick);
            return true;
        }
        return false;
    }

    public int getCurrentRadius() { return currentRadius; }
    public void setCurrentRadius(int radius) { this.currentRadius = radius; setDirty(); }
    public int getTargetRadius() { return targetRadius; }
    public void setTargetRadius(int radius) { this.targetRadius = radius; setDirty(); }
    public long getLastExpansionTick() { return lastExpansionTick; }
    public void setLastExpansionTick(long tick) { this.lastExpansionTick = tick; setDirty(); }
}