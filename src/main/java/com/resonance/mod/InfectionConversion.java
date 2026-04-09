package com.resonance.mod;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import com.resonance.mod.registry.ModBlocks;
import java.util.HashMap;
import java.util.Map;

public class InfectionConversion {
    private static final Map<Block, Block> CONVERSION_MAP = new HashMap<>();

    static {
        // Terreno
        CONVERSION_MAP.put(Blocks.DIRT, ModBlocks.CORRUPTED_DIRT.get());
        CONVERSION_MAP.put(Blocks.GRASS_BLOCK, ModBlocks.CORRUPTED_GRASS.get());
        CONVERSION_MAP.put(Blocks.STONE, ModBlocks.CORRUPTED_STONE.get());
        CONVERSION_MAP.put(Blocks.SAND, ModBlocks.CORRUPTED_SAND.get());
        CONVERSION_MAP.put(Blocks.GRAVEL, ModBlocks.CORRUPTED_GRAVEL.get()); // si lo incluyes luego
        // Tronco de roble
        CONVERSION_MAP.put(Blocks.OAK_LOG, ModBlocks.CORRUPTED_OAK_LOG.get());
        // Puedes añadir más aquí cuando tengas los otros troncos
    }

    public static Block getCorruptedVersion(Block original) {
        return CONVERSION_MAP.getOrDefault(original, null);
    }

    public static boolean canBeInfected(Block block) {
        return CONVERSION_MAP.containsKey(block);
    }
}