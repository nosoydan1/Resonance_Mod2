package com.resonance.mod;

import com.mojang.logging.LogUtils;
import com.resonance.mod.entity.AshenKnightEntity;
import com.resonance.mod.entity.AshenKnightRenderer;
import com.resonance.mod.entity.ChipsEntity;
import com.resonance.mod.entity.ChipsRenderer;
import com.resonance.mod.entity.MineEntity;
import com.resonance.mod.entity.MineRenderer;
import com.resonance.mod.entity.MineralColossusEntity;
import com.resonance.mod.entity.MineralColossusRenderer;
import com.resonance.mod.entity.MineralGuardianEntity;
import com.resonance.mod.entity.MineralGuardianRenderer;
import com.resonance.mod.entity.RaliteEntity;
import com.resonance.mod.entity.RaliteRenderer;
import com.resonance.mod.registry.ModBlocks;
import com.resonance.mod.registry.ModEntities;
import com.resonance.mod.registry.ModItems;
import com.resonance.mod.network.NetworkHandler;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.core.BlockPos;
import java.util.Random;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.event.server.ServerStartingEvent;
import org.slf4j.Logger;

@Mod(ResonanceMod.MODID)
public class ResonanceMod {

    public static final String MODID = "resonance";
    private static final Logger LOGGER = LogUtils.getLogger();

    public ResonanceMod(FMLJavaModLoadingContext context) {
        IEventBus modEventBus = context.getModEventBus();

        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::onEntityAttributeCreation);

        ModBlocks.BLOCKS.register(modEventBus);
        ModItems.ITEMS.register(modEventBus);
        ModEntities.ENTITIES.register(modEventBus);

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
        // FIX: Coloso estaba sin registrar — causaba crash al spawnear
        event.put(ModEntities.MINERAL_COLOSSUS.get(), MineralColossusEntity.createAttributes().build());
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        LOGGER.info("Proyecto Resonance - Servidor iniciado.");
    }

    @SubscribeEvent
    public void onLivingDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof net.minecraft.world.entity.player.Player player)) return;

        Random random = new Random();
        boolean shouldPetrify = false;

        // Asesinado por Mineral Colossus → 100% petrificación
        if (event.getSource().getEntity() instanceof MineralColossusEntity) {
            shouldPetrify = true;
        }
        // Resonancia máxima → petrificación garantizada
        else if (ResonanceData.getResonance(player) >= 100.0f) {
            shouldPetrify = true;
        }
        // Asesinado por otra entidad del mod → 5% probabilidad
        else if (event.getSource().getEntity() != null) {
            String namespace = event.getSource().getEntity().getType().toString();
            if (namespace.contains(MODID)) {
                shouldPetrify = random.nextFloat() < 0.05f;
            }
        }

        if (shouldPetrify) {
            createPetrifiedStatue(player);
        }
    }

    private void createPetrifiedStatue(net.minecraft.world.entity.player.Player player) {
        if (player.level().isClientSide()) return;

        BlockPos pos = player.blockPosition();
        player.level().setBlock(pos, Blocks.GOLD_BLOCK.defaultBlockState(), 3);
        player.level().setBlock(pos.above(), Blocks.GOLD_BLOCK.defaultBlockState(), 3);
    }

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
        }
    }
}
