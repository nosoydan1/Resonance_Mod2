package com.resonance.mod;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

// An example config class. This is not required, but it's a good idea to have one to keep your config organized.
// Demonstrates how to use Forge's config APIs
@Mod.EventBusSubscriber(modid = ResonanceMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class Config
{
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    private static final ForgeConfigSpec.BooleanValue LOG_DIRT_BLOCK = BUILDER
            .comment("Whether to log the dirt block on common setup")
            .define("logDirtBlock", true);

    private static final ForgeConfigSpec.IntValue MAGIC_NUMBER = BUILDER
            .comment("A magic number")
            .defineInRange("magicNumber", 42, 0, Integer.MAX_VALUE);

    public static final ForgeConfigSpec.ConfigValue<String> MAGIC_NUMBER_INTRODUCTION = BUILDER
            .comment("What you want the introduction message to be for the magic number")
            .define("magicNumberIntroduction", "The magic number is... ");

    // a list of strings that are treated as resource locations for items
    private static final ForgeConfigSpec.ConfigValue<List<? extends String>> ITEM_STRINGS = BUILDER
            .comment("A list of items to log on common setup.")
            .defineListAllowEmpty("items", List.of("minecraft:iron_ingot"), Config::validateItemName);

    static final ForgeConfigSpec SPEC = BUILDER.build();

    public static boolean logDirtBlock;
    public static int magicNumber;
    public static String magicNumberIntroduction;
    public static Set<Item> items;

    private static boolean validateItemName(final Object obj)
    {
        return obj instanceof final String itemName && ForgeRegistries.ITEMS.containsKey(new ResourceLocation(itemName));
    }

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event)
    {
        logDirtBlock = LOG_DIRT_BLOCK.get();
        magicNumber = MAGIC_NUMBER.get();
        magicNumberIntroduction = MAGIC_NUMBER_INTRODUCTION.get();

        // convert the list of strings into a set of items
        items = ITEM_STRINGS.get().stream()
                .map(itemName -> ForgeRegistries.ITEMS.getValue(new ResourceLocation(itemName)))
                .collect(Collectors.toSet());
    }

    @Mod.EventBusSubscriber(modid = ResonanceMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
    public class Config {

        private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

        // RESONANCIA
        private static final ForgeConfigSpec.IntValue RESONANCE_THRESHOLD_SLOWNESS = BUILDER
                .comment("Resonancia threshold para Lentitud")
                .defineInRange("resonance.threshold_slowness", 20, 0, 100);

        private static final ForgeConfigSpec.IntValue RESONANCE_THRESHOLD_FATIGUE = BUILDER
                .comment("Resonancia threshold para Fatiga Minera")
                .defineInRange("resonance.threshold_fatigue", 50, 0, 100);

        private static final ForgeConfigSpec.IntValue RESONANCE_THRESHOLD_WEAKNESS = BUILDER
                .comment("Resonancia threshold para Debilidad")
                .defineInRange("resonance.threshold_weakness", 80, 0, 100);

        // INFECCIÓN
        private static final ForgeConfigSpec.IntValue INFECTION_PHASE_1_POINTS = BUILDER
                .comment("Puntos para pasar a Fase 2")
                .defineInRange("infection.phase_1_points", 1500, 100, 10000);

        private static final ForgeConfigSpec.IntValue INFECTION_PHASE_2_POINTS = BUILDER
                .comment("Puntos para pasar a Fase 3")
                .defineInRange("infection.phase_2_points", 3225, 100, 10000);

        // COLOSO
        private static final ForgeConfigSpec.DoubleValue COLOSSUS_BASE_HP = BUILDER
                .comment("HP base del Coloso")
                .defineInRange("colossus.base_hp", 1400.0, 100.0, 10000.0);

        private static final ForgeConfigSpec.DoubleValue COLOSSUS_HP_MULTIPLAYER = BUILDER
                .comment("Multiplicador de HP por jugador adicional")
                .defineInRange("colossus.hp_multiplayer", 700.0, 100.0, 5000.0);

        static final ForgeConfigSpec SPEC = BUILDER.build();

        // Variables públicas
        public static int resonanceThresholdSlowness;
        public static int resonanceThresholdFatigue;
        public static int resonanceThresholdWeakness;
        public static int infectionPhase1Points;
        public static int infectionPhase2Points;
        public static double colossusBaseHP;
        public static double colossusHPMultiplayer;

        @SubscribeEvent
        static void onLoad(final ModConfigEvent event) {
            resonanceThresholdSlowness = RESONANCE_THRESHOLD_SLOWNESS.get();
            resonanceThresholdFatigue = RESONANCE_THRESHOLD_FATIGUE.get();
            resonanceThresholdWeakness = RESONANCE_THRESHOLD_WEAKNESS.get();
            infectionPhase1Points = INFECTION_PHASE_1_POINTS.get();
            infectionPhase2Points = INFECTION_PHASE_2_POINTS.get();
            colossusBaseHP = COLOSSUS_BASE_HP.get();
            colossusHPMultiplayer = COLOSSUS_HP_MULTIPLAYER.get();
        }
    }
}
