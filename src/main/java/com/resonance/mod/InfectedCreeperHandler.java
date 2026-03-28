package com.resonance.mod;

import com.resonance.mod.network.InfectionSyncPacket;
import com.resonance.mod.network.NetworkHandler;
import com.resonance.mod.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.event.entity.EntityStruckByLightningEvent;
import net.minecraftforge.event.level.ExplosionEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Random;

@Mod.EventBusSubscriber(modid = ResonanceMod.MODID)
public class InfectedCreeperHandler {

    private static final Random RANDOM = new Random();
    private static final int CORRUPT_RADIUS = 3;

    @SubscribeEvent
    public static void onExplosion(ExplosionEvent.Detonate event) {
        if (event.getLevel().isClientSide()) return;
        if (!(event.getExplosion().getIndirectSourceEntity() instanceof Creeper creeper)) return;
        if (!MobInfectionHandler.isInfected(creeper)) return;

        ServerLevel level = (ServerLevel) event.getLevel();
        BlockPos center = BlockPos.containing(event.getExplosion().getPosition());
        InfectionData data = InfectionData.get(level);

        // Convertir bloques en el radio a Corrupted Mineral
        for (BlockPos pos : BlockPos.betweenClosed(
                center.offset(-CORRUPT_RADIUS, -CORRUPT_RADIUS, -CORRUPT_RADIUS),
                center.offset(CORRUPT_RADIUS, CORRUPT_RADIUS, CORRUPT_RADIUS))) {

            Block block = level.getBlockState(pos).getBlock();

            if (block == Blocks.AIR || block == Blocks.BEDROCK
                    || block == ModBlocks.CORRUPTED_MINERAL.get()
                    || block == ModBlocks.CORRUPTED_MINERAL_ORE.get()) continue;

            // 60% probabilidad de infectar cada bloque
            if (RANDOM.nextFloat() < 0.6f) {
                level.setBlockAndUpdate(pos.immutable(),
                        ModBlocks.CORRUPTED_MINERAL.get().defaultBlockState());

                data.addPoints(InfectionData.getPointsForBlock(
                        net.minecraft.core.registries.BuiltInRegistries.BLOCK
                                .getKey(block).toString()
                ));
            }
        }

        NetworkHandler.sendToAllClients(
                new InfectionSyncPacket(data.getPoints(), data.getPhase())
        );
    }
}