package com.resonance.mod.block;

import com.resonance.mod.InfectionData;
import com.resonance.mod.network.InfectionSyncPacket;
import com.resonance.mod.network.NetworkHandler;
import com.resonance.mod.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;

public class CorruptedMineralBlock extends Block {

    public CorruptedMineralBlock() {
        super(BlockBehaviour.Properties.of()
                .mapColor(MapColor.COLOR_PURPLE)
                .requiresCorrectToolForDrops()
                .strength(2.5f, 6.0f)
                .sound(SoundType.STONE)
                .randomTicks()
        );
    }

//    @Override
//    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
//        InfectionData data = InfectionData.get(level);
//
//        // Fase 3+: generar Spikes ocasionalmente
//        if (data.getPhase() >= 3 && random.nextFloat() < 0.15f) {
//            int dx = random.nextInt(3) - 1;
//            int dy = random.nextInt(2);
//            int dz = random.nextInt(3) - 1;
//
//            BlockPos spikePos = pos.offset(dx, dy, dz);
//            Block spikeBlock = level.getBlockState(spikePos).getBlock();
//
//            if (spikeBlock == Blocks.AIR || spikeBlock == Blocks.TALL_GRASS) {
//                level.setBlockAndUpdate(spikePos, ModBlocks.MINERAL_SPIKE.get().defaultBlockState());
//            }
//        }
//
//        if (data.getPhase() >= 6) return;
//
//        int radius = 3;
//
//        for (int attempts = 0; attempts < 5; attempts++) {
//            int dx = random.nextInt(radius * 2 + 1) - radius;
//            int dy = random.nextInt(radius * 2 + 1) - radius;
//            int dz = random.nextInt(radius * 2 + 1) - radius;
//
//            BlockPos target = pos.offset(dx, dy, dz);
//            Block targetBlock = level.getBlockState(target).getBlock();
//
//            if (canInfectStatic(targetBlock)) {
//                level.setBlockAndUpdate(target,
//                        ModBlocks.CORRUPTED_MINERAL.get().defaultBlockState());
//
//                int points = InfectionData.getPointsForBlock(
//                        net.minecraft.core.registries.BuiltInRegistries.BLOCK
//                                .getKey(targetBlock).toString()
//                );
//                data.addPoints(points);
//
//                NetworkHandler.sendToAllClients(
//                        new InfectionSyncPacket(data.getPoints(), data.getPhase())
//                );
//                return;
//            }
//        }
//    }

    /**
     * Instancia — delega al método estático para mantener compatibilidad.
     */
    private boolean canInfect(Block block) {
        return canInfectStatic(block);
    }

    /**
     * Método estático reutilizable desde ChipsEntity y cualquier otro lugar
     * que necesite saber si un bloque puede ser infectado.
     *
     * FIX: la versión original solo aceptaba hojas y tierra, haciendo la
     * infección prácticamente invisible en terreno normal. Ahora cubre todos
     * los materiales orgánicos, rocosos y de suelo que tiene sentido infectar.
     */
    public static boolean canInfectStatic(Block block) {
        // Bloques nunca infectables
        if (block == Blocks.AIR
                || block == Blocks.VOID_AIR
                || block == Blocks.CAVE_AIR
                || block == Blocks.BEDROCK
                || block == Blocks.WATER
                || block == Blocks.LAVA
                || block == Blocks.OBSIDIAN
                || block == Blocks.CRYING_OBSIDIAN
                || block == Blocks.ANCIENT_DEBRIS
                || block == ModBlocks.CORRUPTED_MINERAL.get()
                || block == ModBlocks.CORRUPTED_MINERAL_ORE.get()
                || block == ModBlocks.NUCLEUS.get()) {
            return false;
        }

        // Tierra y suelo
        if (block == Blocks.GRASS_BLOCK
                || block == Blocks.DIRT
                || block == Blocks.COARSE_DIRT
                || block == Blocks.ROOTED_DIRT
                || block == Blocks.PODZOL
                || block == Blocks.MYCELIUM
                || block == Blocks.MUD
                || block == Blocks.MUDDY_MANGROVE_ROOTS
                || block == Blocks.FARMLAND) return true;

        // Roca y minerales — la infección debe ser visible en terreno normal
        if (block == Blocks.STONE
                || block == Blocks.GRANITE
                || block == Blocks.DIORITE
                || block == Blocks.ANDESITE
                || block == Blocks.DEEPSLATE
                || block == Blocks.TUFF
                || block == Blocks.CALCITE
                || block == Blocks.GRAVEL
                || block == Blocks.SAND
                || block == Blocks.RED_SAND
                || block == Blocks.SANDSTONE
                || block == Blocks.RED_SANDSTONE) return true;
        // Terracota y variantes
        if (block == Blocks.TERRACOTTA
                || block == Blocks.WHITE_TERRACOTTA
                || block == Blocks.ORANGE_TERRACOTTA
                || block == Blocks.MAGENTA_TERRACOTTA
                || block == Blocks.LIGHT_BLUE_TERRACOTTA
                || block == Blocks.YELLOW_TERRACOTTA
                || block == Blocks.LIME_TERRACOTTA
                || block == Blocks.PINK_TERRACOTTA
                || block == Blocks.GRAY_TERRACOTTA
                || block == Blocks.LIGHT_GRAY_TERRACOTTA
                || block == Blocks.CYAN_TERRACOTTA
                || block == Blocks.PURPLE_TERRACOTTA
                || block == Blocks.BLUE_TERRACOTTA
                || block == Blocks.BROWN_TERRACOTTA
                || block == Blocks.GREEN_TERRACOTTA
                || block == Blocks.RED_TERRACOTTA
                || block == Blocks.BLACK_TERRACOTTA) return true;

// Arcilla
        if (block == Blocks.CLAY) return true;

        // Hojas de todos los árboles
        if (block == Blocks.OAK_LEAVES
                || block == Blocks.BIRCH_LEAVES
                || block == Blocks.SPRUCE_LEAVES
                || block == Blocks.JUNGLE_LEAVES
                || block == Blocks.ACACIA_LEAVES
                || block == Blocks.DARK_OAK_LEAVES
                || block == Blocks.MANGROVE_LEAVES
                || block == Blocks.CHERRY_LEAVES
                || block == Blocks.AZALEA_LEAVES
                || block == Blocks.FLOWERING_AZALEA_LEAVES) return true;

        // Troncos y madera
        if (block == Blocks.OAK_LOG
                || block == Blocks.BIRCH_LOG
                || block == Blocks.SPRUCE_LOG
                || block == Blocks.JUNGLE_LOG
                || block == Blocks.ACACIA_LOG
                || block == Blocks.DARK_OAK_LOG
                || block == Blocks.MANGROVE_LOG
                || block == Blocks.CHERRY_LOG
                || block == Blocks.OAK_WOOD
                || block == Blocks.BIRCH_WOOD
                || block == Blocks.SPRUCE_WOOD
                || block == Blocks.JUNGLE_WOOD
                || block == Blocks.ACACIA_WOOD
                || block == Blocks.DARK_OAK_WOOD
                || block == Blocks.MANGROVE_WOOD) return true;

        // Nieve y hielo (biomas fríos)
        if (block == Blocks.SNOW
                || block == Blocks.SNOW_BLOCK
                || block == Blocks.ICE
                || block == Blocks.PACKED_ICE
                || block == Blocks.BLUE_ICE) return true;

        // Vegetación y decoración natural
        if (block == Blocks.GRASS
                || block == Blocks.TALL_GRASS
                || block == Blocks.FERN
                || block == Blocks.LARGE_FERN
                || block == Blocks.DEAD_BUSH
                || block == Blocks.VINE
                || block == Blocks.MOSS_BLOCK
                || block == Blocks.MOSS_CARPET) return true;

        return false;
    }
}
