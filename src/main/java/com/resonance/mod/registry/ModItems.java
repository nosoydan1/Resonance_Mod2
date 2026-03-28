package com.resonance.mod.registry;

import com.resonance.mod.entity.MineralParticlesProjectileEntity;
import com.resonance.mod.ResonanceMod;
import com.resonance.mod.item.BreathingMaskItem;
import com.resonance.mod.item.DissonantInjectionItem;
import com.resonance.mod.item.LocatingCompassItem;
import com.resonance.mod.item.FragmentedPickaxeItem;
import com.resonance.mod.item.AnchorShieldItem;
import com.resonance.mod.item.SyringeGunItem;
import com.resonance.mod.item.MineralParticlesVialItem;
import com.resonance.mod.item.AshFoodItem;
import com.resonance.mod.item.CodexItem;
import com.resonance.mod.item.SplashMineralParticlesItem;
import net.minecraft.world.item.ArmorMaterials;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import com.resonance.mod.ResonanceInfectionHitHandler;

public class ModItems {

    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, ResonanceMod.MODID);

    // -------------------------------------------------------------------------
    // Items de bloques
    // -------------------------------------------------------------------------
    public static final RegistryObject<Item> CORRUPTED_MINERAL =
            ITEMS.register("corrupted_mineral",
                    () -> new BlockItem(ModBlocks.CORRUPTED_MINERAL.get(), new Item.Properties()));

    public static final RegistryObject<Item> CORRUPTED_MINERAL_ORE =
            ITEMS.register("corrupted_mineral_ore",
                    () -> new BlockItem(ModBlocks.CORRUPTED_MINERAL_ORE.get(), new Item.Properties()));

    public static final RegistryObject<Item> METEORITE_ROCK =
            ITEMS.register("meteorite_rock",
                    () -> new BlockItem(ModBlocks.METEORITE_ROCK.get(), new Item.Properties()));

    public static final RegistryObject<Item> FOSSILIZED_CARBON_BLOCK =
            ITEMS.register("fossilized_carbon_block",
                    () -> new BlockItem(ModBlocks.FOSSILIZED_CARBON_BLOCK.get(), new Item.Properties()));

    // -------------------------------------------------------------------------
    // Materiales sueltos
    // -------------------------------------------------------------------------
    public static final RegistryObject<Item> METEORITE_FRAGMENT =
            ITEMS.register("meteorite_fragment", () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> MINERAL_ORE =
            ITEMS.register("mineral_ore", () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> MINERAL_CRYSTAL =
            ITEMS.register("mineral_crystal", () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> FOSSILIZED_CARBON =
            ITEMS.register("fossilized_carbon", () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> ASHES =
            ITEMS.register("ashes", () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> COMPACT_CRYSTAL =
            ITEMS.register("compact_crystal", () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> MINE_FRAGMENT =
            ITEMS.register("mine_fragment", () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> MINE_ESSENCE =
            ITEMS.register("mine_essence", () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> RALITE_FRAGMENT =
            ITEMS.register("ralite_fragment", () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> RALITE_ESSENCE =
            ITEMS.register("ralite_essence", () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> COMPRESSED_ESSENCE =
            ITEMS.register("compressed_essence", () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> PETRIFIED_CORE =
            ITEMS.register("petrified_core", () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> SILENT_SHARD =
            ITEMS.register("silent_shard", () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> SILENT_SHARDS =
            ITEMS.register("silent_shards", () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> CHARGED_PETRIFIED_CORE =
            ITEMS.register("charged_petrified_core", () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> HEART_OF_COLOSSUS =
            ITEMS.register("heart_of_colossus", () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> MINERAL_PARTICLES =
            ITEMS.register("mineral_particles", () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> SYRINGE_GUN =
            ITEMS.register("syringe_gun",
                    () -> new SyringeGunItem(new Item.Properties().stacksTo(1)));

    public static final RegistryObject<Item> ANTIDOTE =
            ITEMS.register("antidote", () -> new Item(new Item.Properties()));

    // -------------------------------------------------------------------------
    // Consumibles
    // -------------------------------------------------------------------------
    public static final RegistryObject<Item> DISSONANT_INJECTION =
            ITEMS.register("dissonant_injection",
                    () -> new DissonantInjectionItem(new Item.Properties().stacksTo(16)));

    public static final RegistryObject<Item> MINERAL_PARTICLES_VIAL =
            ITEMS.register("mineral_particles_vial",
                    () -> new MineralParticlesVialItem(new Item.Properties().stacksTo(16)));

    public static final RegistryObject<Item> SPLASH_MINERAL_PARTICLES =
            ITEMS.register("splash_mineral_particles",
                    () -> new SplashMineralParticlesItem(new Item.Properties().stacksTo(16)));

    // -------------------------------------------------------------------------
    // Comidas con ceniza — FIX: resonanceReduction como float
    // GDD §6.5: cada comida de ceniza reduce 4 pts de Resonancia
    // -------------------------------------------------------------------------
    public static final RegistryObject<Item> ASH_CHICKEN =
            ITEMS.register("ash_chicken",
                    () -> new AshFoodItem(5, 0.6f, 4.0f, new Item.Properties()));

    public static final RegistryObject<Item> ASH_RABBIT =
            ITEMS.register("ash_rabbit",
                    () -> new AshFoodItem(4, 0.6f, 4.0f, new Item.Properties()));

    public static final RegistryObject<Item> ASH_PORKCHOP =
            ITEMS.register("ash_porkchop",
                    () -> new AshFoodItem(7, 0.8f, 4.0f, new Item.Properties()));

    public static final RegistryObject<Item> ASH_STEAK =
            ITEMS.register("ash_steak",
                    () -> new AshFoodItem(7, 0.8f, 4.0f, new Item.Properties()));

    public static final RegistryObject<Item> ASH_SALMON =
            ITEMS.register("ash_salmon",
                    () -> new AshFoodItem(5, 0.6f, 4.0f, new Item.Properties()));

    public static final RegistryObject<Item> ASH_COD =
            ITEMS.register("ash_cod",
                    () -> new AshFoodItem(4, 0.6f, 4.0f, new Item.Properties()));

    // FIX: Ash Pie usa constructor extendido con accelerated=true (GDD §6.5)
    // Hambre: 2.5 pts (equivale a nutrition=2, saturation=0.3 en la API de Forge)
    public static final RegistryObject<Item> ASH_PIE =
            ITEMS.register("ash_pie",
                    () -> new AshFoodItem(2, 0.3f, 4.0f, true,
                            new Item.Properties().stacksTo(1)));

    // -------------------------------------------------------------------------
    // Herramientas y equipamiento
    // -------------------------------------------------------------------------
    public static final RegistryObject<Item> FOSSILIZED_CARBON_TORCH =
            ITEMS.register("fossilized_carbon_torch",
                    () -> new Item(new Item.Properties().stacksTo(64)));

    public static final RegistryObject<Item> LOCATING_COMPASS =
            ITEMS.register("locating_compass",
                    () -> new LocatingCompassItem(new Item.Properties().stacksTo(1)));

    public static final RegistryObject<Item> BREATHING_MASK =
            ITEMS.register("breathing_mask",
                    () -> new BreathingMaskItem(ArmorMaterials.LEATHER,
                            ArmorItem.Type.HELMET, new Item.Properties().stacksTo(1)));

    public static final RegistryObject<Item> CARBON_FILTER =
            ITEMS.register("carbon_filter",
                    () -> new Item(new Item.Properties().stacksTo(16)));

    public static final RegistryObject<Item> ANCHOR_SHIELD =
            ITEMS.register("anchor_shield",
                    () -> new AnchorShieldItem(new Item.Properties().stacksTo(1)));

    public static final RegistryObject<Item> FRAGMENTED_PICKAXE =
            ITEMS.register("fragmented_pickaxe",
                    () -> new FragmentedPickaxeItem(new Item.Properties().stacksTo(1)));

    // -------------------------------------------------------------------------
    // Items narrativos — Códice
    // -------------------------------------------------------------------------
    public static final RegistryObject<Item> CODEX_LEVEL_1 =
            ITEMS.register("codex_level_1",
                    () -> new CodexItem(1, new Item.Properties().stacksTo(1)));

    public static final RegistryObject<Item> CODEX_LEVEL_2 =
            ITEMS.register("codex_level_2",
                    () -> new CodexItem(2, new Item.Properties().stacksTo(1)));

    public static final RegistryObject<Item> CODEX_LEVEL_3 =
            ITEMS.register("codex_level_3",
                    () -> new CodexItem(3, new Item.Properties().stacksTo(1)));

    // -------------------------------------------------------------------------
    // Coleccionables
    // -------------------------------------------------------------------------
    public static final RegistryObject<Item> CHIPS_STATUE =
            ITEMS.register("chips_statue",
                    () -> new Item(new Item.Properties().stacksTo(1)));

    public static final RegistryObject<Item> TROPHY_CHIPS =
            ITEMS.register("trophy_chips",
                    () -> new Item(new Item.Properties().stacksTo(1)));

    public static final RegistryObject<Item> DAMPING_MECHANISM =
            ITEMS.register("damping_mechanism",
                    () -> new BlockItem(ModBlocks.DAMPING_MECHANISM.get(), new Item.Properties()));

    // Item del Núcleo (no obtenible normalmente)
    public static final RegistryObject<Item> NUCLEUS =
            ITEMS.register("nucleus",
                    () -> new BlockItem(ModBlocks.NUCLEUS.get(), new Item.Properties()));
}
