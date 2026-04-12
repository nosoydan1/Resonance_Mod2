package com.resonance.mod;

import com.resonance.mod.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;

/**
 * Genera el altar del núcleo tras el impacto del meteorito.
 *
 * Estructura:
 *  - Nivel 0: Círculo de radio 6 con bloque base según bioma (a ras del suelo).
 *  - Nivel +1: Plataforma 3x3 de corrupted_mineral en el centro del círculo.
 *  - Nivel +2: Núcleo en el bloque central de la plataforma.
 *
 * Los bloques que ya existían bajo el círculo NO se tocan; quedan cubiertos
 * por la plataforma y la infección los irá convirtiendo con el tiempo.
 */
public class NucleusAltarGenerator {

    // Radio del círculo base del altar
    private static final int CIRCLE_RADIUS = 6;

    // Radio de limpieza de vegetación/árboles antes de construir
    private static final int CLEAR_RADIUS = 8;

    /**
     * Punto de entrada: detecta bioma, limpia el área y construye el altar.
     *
     * @param level  nivel del servidor (overworld)
     * @param center posición de superficie donde impactó el meteorito
     */
    public static BlockPos generate(ServerLevel level, BlockPos center) {

        // 1. Bajar al suelo sólido real (por si center apunta a aire o vegetación)
        BlockPos groundPos = findSolidGround(level, center);

        // 2. Elegir bloque base según bioma
        Block baseBlock = getBiomeBaseBlock(level, groundPos);

        // 3. Limpiar vegetación/árboles en el área
        clearVegetation(level, groundPos, CLEAR_RADIUS);

        // 4. Construir círculo base (radio 6) a nivel del suelo
        buildCircle(level, groundPos, CIRCLE_RADIUS, baseBlock);

        // 5. Construir plataforma 3x3 de corrupted_mineral 1 bloque por encima del círculo
        BlockPos platformBase = groundPos.above(1);
        buildPlatform(level, platformBase);

        // 6. Colocar el núcleo en el centro de la plataforma (1 bloque encima del 3x3)
        BlockPos nucleusPos = groundPos.above(2);
        level.setBlockAndUpdate(nucleusPos, ModBlocks.NUCLEUS.get().defaultBlockState());

        // 7. Registrar el núcleo en InfectionData e inicializar la infección
        InfectionData data = InfectionData.get(level);
        data.setNucleus(nucleusPos);
        data.setCurrentRadius(10);   // radio inicial de infección
        data.setTargetRadius(10);
        data.setLastExpansionTick(level.getServer().getTickCount());
        data.setDirty();

        ResonanceMod.LOGGER.info("[Resonance] Altar generado en {} con bloque base {}",
                nucleusPos, baseBlock.getDescriptionId());

        return nucleusPos;
    }

    // -------------------------------------------------------------------------
    // Helpers de construcción
    // -------------------------------------------------------------------------

    /** Desciende desde center hasta encontrar un bloque sólido (no aire, no planta). */
    private static BlockPos findSolidGround(ServerLevel level, BlockPos center) {
        // Usar heightmap WORLD_SURFACE para obtener la Y exacta de la superficie
        int surfaceY = level.getHeight(Heightmap.Types.WORLD_SURFACE, center.getX(), center.getZ());
        BlockPos surface = new BlockPos(center.getX(), surfaceY - 1, center.getZ());

        // Bajar hasta encontrar algo sólido (por si hay agua/plantas encima)
        for (int dy = 0; dy >= -10; dy--) {
            BlockPos check = surface.above(dy);
            BlockState state = level.getBlockState(check);
            if (!state.isAir() && !isVegetation(state) && !state.liquid()) {
                return check;
            }
        }
        return surface;
    }

    /** Elimina plantas, hojas, troncos y vegetación en el radio indicado. */
    private static void clearVegetation(ServerLevel level, BlockPos center, int radius) {
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                if (x * x + z * z > radius * radius) continue;

                // Limpiar desde -2 hasta +20 en Y para eliminar árboles completos
                for (int dy = -2; dy <= 20; dy++) {
                    BlockPos pos = center.offset(x, dy, z);
                    BlockState state = level.getBlockState(pos);
                    if (isVegetation(state)) {
                        level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
                    }
                }
            }
        }
    }

    /** Construye un círculo relleno de radio r en la Y de center. */
    private static void buildCircle(ServerLevel level, BlockPos center, int radius, Block block) {
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                if (x * x + z * z <= radius * radius) {
                    BlockPos pos = new BlockPos(center.getX() + x, center.getY(), center.getZ() + z);
                    // Solo colocar en la superficie (no hundir en el suelo)
                    level.setBlockAndUpdate(pos, block.defaultBlockState());
                }
            }
        }
    }

    /** Construye la plataforma 3x3 de corrupted_mineral centrada en platformBase. */
    private static void buildPlatform(ServerLevel level, BlockPos platformBase) {
        Block platformBlock = ModBlocks.CORRUPTED_MINERAL.get();
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                BlockPos pos = platformBase.offset(x, 0, z);
                level.setBlockAndUpdate(pos, platformBlock.defaultBlockState());
            }
        }
    }

    // -------------------------------------------------------------------------
    // Lógica de bioma
    // -------------------------------------------------------------------------

    /**
     * Devuelve el bloque base del círculo según el bioma del punto de impacto.
     */
    private static Block getBiomeBaseBlock(ServerLevel level, BlockPos pos) {
        Holder<Biome> biomeHolder = level.getBiome(pos);
        ResourceKey<Biome> biomeKey = biomeHolder.unwrapKey().orElse(null);

        if (biomeKey == null) return ModBlocks.CORRUPTED_DIRT.get();

        String biomePath = biomeKey.location().getPath();

        // Desierto / badlands
        if (containsAny(biomePath, "desert", "badlands", "eroded_badlands", "wooded_badlands")) {
            return ModBlocks.CORRUPTED_SAND.get();
        }

        // Playa / río → arena/grava corrupta
        if (containsAny(biomePath, "beach", "river", "stony_shore")) {
            return ModBlocks.CORRUPTED_SAND.get();
        }

        // Montaña / pico de piedra
        if (containsAny(biomePath, "mountain", "peaks", "stony", "gravelly",
                "windswept_hills", "windswept_forest", "windswept_gravelly")) {
            return ModBlocks.CORRUPTED_STONE.get();
        }

        // Bosques, llanuras, pantanos, sabanas → hierba corrupta
        if (containsAny(biomePath, "plains", "forest", "birch", "dark_forest",
                "jungle", "savanna", "swamp", "meadow", "cherry_grove", "taiga")) {
            return ModBlocks.CORRUPTED_GRASS.get();
        }

        // Por defecto: tierra corrupta
        return ModBlocks.CORRUPTED_DIRT.get();
    }

    // -------------------------------------------------------------------------
    // Utilidades
    // -------------------------------------------------------------------------

    /** Comprueba si blockState es vegetación que debe eliminarse. */
    private static boolean isVegetation(BlockState state) {
        Block b = state.getBlock();
        return b == Blocks.OAK_LOG          || b == Blocks.OAK_LEAVES        ||
                b == Blocks.BIRCH_LOG        || b == Blocks.BIRCH_LEAVES       ||
                b == Blocks.SPRUCE_LOG       || b == Blocks.SPRUCE_LEAVES      ||
                b == Blocks.DARK_OAK_LOG     || b == Blocks.DARK_OAK_LEAVES    ||
                b == Blocks.JUNGLE_LOG       || b == Blocks.JUNGLE_LEAVES      ||
                b == Blocks.ACACIA_LOG       || b == Blocks.ACACIA_LEAVES      ||
                b == Blocks.MANGROVE_LOG     || b == Blocks.MANGROVE_LEAVES    ||
                b == Blocks.CHERRY_LOG       || b == Blocks.CHERRY_LEAVES      ||
                b == Blocks.GRASS            || b == Blocks.TALL_GRASS         ||
                b == Blocks.FERN             || b == Blocks.LARGE_FERN         ||
                b == Blocks.DEAD_BUSH        || b == Blocks.VINE               ||
                b == Blocks.SUGAR_CANE       || b == Blocks.BAMBOO             ||
                b == Blocks.POPPY            || b == Blocks.DANDELION          ||
                b == Blocks.AZURE_BLUET      || b == Blocks.ALLIUM             ||
                b == Blocks.LILY_OF_THE_VALLEY || b == Blocks.WITHER_ROSE      ||
                b == Blocks.SWEET_BERRY_BUSH || b == Blocks.CAVE_VINES         ||
                b == Blocks.BIG_DRIPLEAF     || b == Blocks.SMALL_DRIPLEAF     ||
                b == Blocks.WATER            || b == Blocks.LILY_PAD           ||
                state.isAir();
    }

    /** true si path contiene alguno de los fragmentos dados. */
    private static boolean containsAny(String path, String... fragments) {
        for (String fragment : fragments) {
            if (path.contains(fragment)) return true;
        }
        return false;
    }
}