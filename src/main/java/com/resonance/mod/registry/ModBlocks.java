package com.resonance.mod.registry;

import com.resonance.mod.ResonanceMod;
import com.resonance.mod.block.CorruptedMineralBlock;
import com.resonance.mod.block.CorruptedMineralOreBlock;
import com.resonance.mod.block.MeteoriteRockBlock;
import com.resonance.mod.block.FossilizedCarbonBlock;
import com.resonance.mod.block.DampingMechanismBlock;
import net.minecraft.world.level.block.Block;
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
}