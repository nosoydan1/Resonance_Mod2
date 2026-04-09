package com.resonance.mod.registry;

import com.resonance.mod.InfectionData;
import com.resonance.mod.ResonanceMod;
import com.resonance.mod.block.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModBlocks {

    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, ResonanceMod.MODID);

    // Bloque base del bioma
    public static final RegistryObject<Block> CORRUPTED_MINERAL =
            BLOCKS.register("corrupted_mineral",
                    () -> new CorruptedMineralBlock());

    // Bloques corruptos básicos
    public static final RegistryObject<Block> CORRUPTED_DIRT = BLOCKS.register("corrupted_dirt",
            () -> new CorruptedBlock(BlockBehaviour.Properties.copy(Blocks.DIRT).mapColor(MapColor.COLOR_PURPLE)));
    public static final RegistryObject<Block> CORRUPTED_GRASS = BLOCKS.register("corrupted_grass",
            () -> new CorruptedGrassBlock(BlockBehaviour.Properties.copy(Blocks.GRASS_BLOCK).mapColor(MapColor.COLOR_PURPLE)));
    public static final RegistryObject<Block> CORRUPTED_STONE = BLOCKS.register("corrupted_stone",
            () -> new CorruptedBlock(BlockBehaviour.Properties.copy(Blocks.STONE).mapColor(MapColor.COLOR_PURPLE)));
    public static final RegistryObject<Block> CORRUPTED_SAND = BLOCKS.register("corrupted_sand",
            () -> new CorruptedBlock(BlockBehaviour.Properties.copy(Blocks.SAND).mapColor(MapColor.COLOR_PURPLE)));
    public static final RegistryObject<Block> CORRUPTED_OAK_LOG = BLOCKS.register("corrupted_oak_log",
            () -> new RotatedPillarBlock(BlockBehaviour.Properties.copy(Blocks.OAK_LOG).mapColor(MapColor.COLOR_PURPLE)));
    public static final RegistryObject<Block> CORRUPTED_GRAVEL = BLOCKS.register("corrupted_gravel",
            () -> new CorruptedBlock(BlockBehaviour.Properties.copy(Blocks.GRAVEL).mapColor(MapColor.COLOR_PURPLE))
            );

    // Mineral ore dentro del bioma
    public static final RegistryObject<Block> CORRUPTED_MINERAL_ORE =
            BLOCKS.register("corrupted_mineral_ore",
                    () -> new CorruptedMineralOreBlock());

    // Catalizador del evento
    public static final RegistryObject<Block> METEORITE_ROCK =
            BLOCKS.register("meteorite_rock",
                    () -> new MeteoriteRockBlock());

    // Combustible de alta duración
    public static final RegistryObject<Block> FOSSILIZED_CARBON_BLOCK =
            BLOCKS.register("fossilized_carbon_block",
                    () -> new FossilizedCarbonBlock());

    // Núcleo de la infección
    public static final RegistryObject<Block> NUCLEUS =
            BLOCKS.register("nucleus",
                    () -> new net.minecraft.world.level.block.Block(
                            net.minecraft.world.level.block.state.BlockBehaviour.Properties.of()
                                    .mapColor(net.minecraft.world.level.material.MapColor.COLOR_PURPLE)
                                    .strength(-1.0f, 3600000.0f) // irrompible como bedrock
                                    .sound(net.minecraft.world.level.block.SoundType.STONE)
                                    .lightLevel(s -> 7)
                    ));

    // Mecanismo de amortiguación
    public static final RegistryObject<Block> DAMPING_MECHANISM =
            BLOCKS.register("damping_mechanism",
                    () -> new DampingMechanismBlock());

    // Trofeo de Chips — bloque decorativo colocable
    public static final RegistryObject<Block> TROPHY_CHIPS =
            BLOCKS.register("trophy_chips",
                    () -> new net.minecraft.world.level.block.Block(
                            net.minecraft.world.level.block.state.BlockBehaviour.Properties.of()
                                    .mapColor(net.minecraft.world.level.material.MapColor.STONE)
                                    .strength(0.5f, 1.0f)
                                    .sound(net.minecraft.world.level.block.SoundType.STONE)
                                    .noOcclusion()
                    ));

    // Mineral de spike - arbusto con frutos
    public static final RegistryObject<Block> MINERAL_SPIKE = BLOCKS.register("mineral_spike", MineralSpikeBlock::new);

    // Fluidos
    public static final DeferredRegister<Fluid> FLUIDS =
            DeferredRegister.create(ForgeRegistries.FLUIDS, ResonanceMod.MODID);

    public static final RegistryObject<FlowingFluid> MINERALIZED_WATER = FLUIDS.register("mineralized_water", MineralizedWaterFluid::new);
    public static final RegistryObject<FlowingFluid> MINERALIZED_WATER_FLOWING = FLUIDS.register("mineralized_water_flowing", MineralizedWaterFluid::new);
    public static final RegistryObject<LiquidBlock> MINERALIZED_WATER_BLOCK = BLOCKS.register("mineralized_water_block",
            () -> new MineralizedWaterBlock(MINERALIZED_WATER.get()));

}
