package com.resonance.mod;

import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;

private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(InfectionData.class);

@Mod.EventBusSubscriber(modid = ResonanceMod.MODID)
public class InfectionData {
}

    private static final Logger LOGGER = LoggerFactory.getLogger(NucleusReachHandler.class);

public class InfectionData extends SavedData {

    private static final String DATA_NAME = "resonance_infection";

    private int points = 0;
    private int phase = 1;
    private BlockPos nucleus = null; // Epicentro de la infección

    private static final int[] PHASE_THRESHOLDS = {
            0, 1500, 3225, 6881, 14090, 27478, 56108
    };

    public static InfectionData get(Level level) {
        return level.getServer().overworld()
                .getDataStorage()
                .computeIfAbsent(InfectionData::load, InfectionData::new, DATA_NAME);
    }

    public static InfectionData load(CompoundTag tag) {
        InfectionData data = new InfectionData();
        data.points = tag.getInt("Points");
        data.phase = tag.getInt("Phase");
        if (tag.contains("NucleusX")) {
            data.nucleus = new BlockPos(
                    tag.getInt("NucleusX"),
                    tag.getInt("NucleusY"),
                    tag.getInt("NucleusZ")
            );
        }
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        tag.putInt("Points", points);
        tag.putInt("Phase", phase);
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
}