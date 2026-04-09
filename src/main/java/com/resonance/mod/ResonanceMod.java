package com.resonance.mod;

import com.mojang.logging.LogUtils;
import com.resonance.mod.client.ResonanceHUD;
import com.resonance.mod.entity.*;
import com.resonance.mod.network.NetworkHandler;
import com.resonance.mod.registry.*;
import com.resonance.mod.registry.ModFeatures;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

import java.util.Random;

import static com.resonance.mod.registry.ModBlocks.FLUIDS;

@Mod(ResonanceMod.MODID)
public class ResonanceMod {

    public static final String MODID = "resonance";
    static final Logger LOGGER = LogUtils.getLogger();

    public ResonanceMod(FMLJavaModLoadingContext context) {
        IEventBus modEventBus = context.getModEventBus();

        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::onEntityAttributeCreation);

        ModBlocks.BLOCKS.register(modEventBus);
        ModItems.ITEMS.register(modEventBus);
        ModEntities.ENTITIES.register(modEventBus);
        // ModBiomes.BIOMES.register(modEventBus);
        FLUIDS.register(modEventBus);
        ModFeatures.FEATURES.register(modEventBus);

        MinecraftForge.EVENT_BUS.register(this);
        context.registerConfig(ModConfig.Type.COMMON, Config.SPEC);

        LOGGER.info("Proyecto Resonance iniciando...");
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        NetworkHandler.register();
        LOGGER.info("Proyecto Resonance - Common Setup completado.");
    }

    private void onEntityAttributeCreation(EntityAttributeCreationEvent event) {
        event.put(ModEntities.CHIPS.get(), ChipsEntity.createAttributes().build());
        event.put(ModEntities.MINE.get(), MineEntity.createAttributes().build());
        event.put(ModEntities.RALITE.get(), RaliteEntity.createAttributes().build());
        event.put(ModEntities.ASHEN_KNIGHT.get(), AshenKnightEntity.createAttributes().build());
        event.put(ModEntities.MINERAL_GUARDIAN.get(), MineralGuardianEntity.createAttributes().build());
        event.put(ModEntities.MINERAL_COLOSSUS.get(), MineralColossusEntity.createAttributes().build());
        event.put(ModEntities.ECHO.get(), EchoEntity.createAttributes().build());
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        LOGGER.info("Proyecto Resonance - Servidor iniciado.");
    }

    @SubscribeEvent
    public void onLivingDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;

        Random random = new Random();
        boolean shouldPetrify = false;

        if (event.getSource().getEntity() instanceof MineralColossusEntity) {
            shouldPetrify = true;
        } else if (ResonanceData.getResonance(player) >= 100.0f) {
            shouldPetrify = true;
        } else if (event.getSource().getEntity() != null) {
            String namespace = event.getSource().getEntity().getType().toString();
            if (namespace.contains(MODID)) {
                shouldPetrify = random.nextFloat() < 0.05f;
            }
        }

        if (shouldPetrify) {
            createPetrifiedStatue(player);
        }
    }

    public static void createPetrifiedStatue(Player player) {
        if (player.level().isClientSide()) return;
        BlockPos pos = player.blockPosition();
        player.level().setBlock(pos, Blocks.GOLD_BLOCK.defaultBlockState(), 3);
        player.level().setBlock(pos.above(), Blocks.GOLD_BLOCK.defaultBlockState(), 3);
    }

    // ========== CLASE CLIENTE ==========
    @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientEvents {

        @SubscribeEvent
        public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
            event.registerEntityRenderer(ModEntities.CHIPS.get(), ChipsRenderer::new);
            event.registerEntityRenderer(ModEntities.MINE.get(), MineRenderer::new);
            event.registerEntityRenderer(ModEntities.RALITE.get(), RaliteRenderer::new);
            event.registerEntityRenderer(ModEntities.ASHEN_KNIGHT.get(), AshenKnightRenderer::new);
            event.registerEntityRenderer(ModEntities.MINERAL_GUARDIAN.get(), MineralGuardianRenderer::new);
            event.registerEntityRenderer(ModEntities.MINERAL_COLOSSUS.get(), MineralColossusRenderer::new);
            event.registerEntityRenderer(ModEntities.ECHO.get(), EchoRenderer::new);
            event.registerEntityRenderer(ModEntities.MINERAL_PARTICLES_PROJECTILE.get(),
                    net.minecraft.client.renderer.entity.ThrownItemRenderer::new);
        }

        @SubscribeEvent
        public static void registerOverlays(RegisterGuiOverlaysEvent event) {
            System.out.println("DEBUG: Registrando overlay de resonancia");
            event.registerAbove(VanillaGuiOverlay.HOTBAR.id(), "resonance_bar", ResonanceHUD.INSTANCE);
        }
    }
}