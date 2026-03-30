package com.resonance.mod;

import com.resonance.mod.network.InfectionSyncPacket;
import com.resonance.mod.network.NetworkHandler;
import com.resonance.mod.registry.ModBlocks;
import com.resonance.mod.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.event.level.ExplosionEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Random;

@Mod.EventBusSubscriber(modid = ResonanceMod.MODID)
public class InfectedCreeperHandler {

    private static final Random RANDOM = new Random();
    private static final int CORRUPT_RADIUS = 3;
    private static final int PARTICLE_DROPS = 3;

    @SubscribeEvent
    public static void onExplosion(ExplosionEvent.Detonate event) {
        if (event.getLevel().isClientSide()) return;

        // FIX: verificar ambos métodos de obtener la fuente
        Creeper creeper = null;
        if (event.getExplosion().getIndirectSourceEntity() instanceof Creeper c) {
            creeper = c;
        } else if (event.getExplosion().getDirectSourceEntity() instanceof Creeper c) {
            creeper = c;
        }

        if (creeper == null) return;
        if (!MobInfectionHandler.isInfected(creeper)) return;

        ServerLevel level = (ServerLevel) event.getLevel();
        BlockPos center = BlockPos.containing(event.getExplosion().getPosition());
        InfectionData data = InfectionData.get(level);

        // 1. Convertir bloques en radio a Corrupted Mineral
        for (BlockPos pos : BlockPos.betweenClosed(
                center.offset(-CORRUPT_RADIUS, -CORRUPT_RADIUS, -CORRUPT_RADIUS),
                center.offset(CORRUPT_RADIUS, CORRUPT_RADIUS, CORRUPT_RADIUS))) {

            Block block = level.getBlockState(pos).getBlock();

            if (block == Blocks.AIR || block == Blocks.BEDROCK
                    || block == ModBlocks.CORRUPTED_MINERAL.get()
                    || block == ModBlocks.CORRUPTED_MINERAL_ORE.get()) continue;

            if (RANDOM.nextFloat() < 0.6f) {
                level.setBlockAndUpdate(pos.immutable(),
                        ModBlocks.CORRUPTED_MINERAL.get().defaultBlockState());
                data.addPoints(InfectionData.getPointsForBlock(
                        net.minecraft.core.registries.BuiltInRegistries.BLOCK
                                .getKey(block).toString()));
            }
        }

        // 2. Dejar estela de Mineral Particles sueltas
        for (int i = 0; i < PARTICLE_DROPS; i++) {
            double offsetX = (RANDOM.nextDouble() - 0.5) * CORRUPT_RADIUS * 2;
            double offsetZ = (RANDOM.nextDouble() - 0.5) * CORRUPT_RADIUS * 2;

            ItemEntity particleDrop = new ItemEntity(
                    level,
                    center.getX() + offsetX + 0.5,
                    center.getY() + 1.0,
                    center.getZ() + offsetZ + 0.5,
                    new ItemStack(ModItems.MINERAL_PARTICLES.get(), 1)
            );
            particleDrop.setDeltaMovement(
                    offsetX * 0.15,
                    0.3 + RANDOM.nextDouble() * 0.2,
                    offsetZ * 0.15
            );
            level.addFreshEntity(particleDrop);
        }

        // 3. Registrar estela para obtención de Mineral Particles Vial
        MineralParticlesVialHandler.registerTrail(center);

        NetworkHandler.sendToAllClients(
                new InfectionSyncPacket(data.getPoints(), data.getPhase()));
    }
}