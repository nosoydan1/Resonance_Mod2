package com.resonance.mod.block;

import com.resonance.mod.InfectionData;
import com.resonance.mod.MeteoriteEventHandler;
import com.resonance.mod.ResonanceData;
import com.resonance.mod.ResonanceMod;
import com.resonance.mod.network.InfectionSyncPacket;
import com.resonance.mod.network.NetworkHandler;
import com.resonance.mod.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Random;

@Mod.EventBusSubscriber(modid = ResonanceMod.MODID)
public class MeteoriteRockBlock extends Block {

    private static final Random RANDOM = new Random();

    public MeteoriteRockBlock() {
        super(BlockBehaviour.Properties.of()
                .mapColor(MapColor.COLOR_GRAY)
                .requiresCorrectToolForDrops()
                .strength(2.0f, 6.0f)
                .sound(SoundType.DEEPSLATE)
        );
    }

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (!(event.getState().getBlock() instanceof MeteoriteRockBlock)) return;
        if (event.getLevel().isClientSide()) return;

        Player player = event.getPlayer();
        Level level = (Level) event.getLevel();
        BlockPos pos = event.getPos();

        // Drop de 1 Meteorite Fragment
        Block.popResource(level, pos,
                new ItemStack(com.resonance.mod.registry.ModItems.METEORITE_FRAGMENT.get(), 1));

        InfectionData data = InfectionData.get(level);

        // Solo al picar el PRIMER bloque del meteorito
        if (data.getNucleus() == null) {

            // Generar Núcleo a 5-10 bloques del meteorito en superficie
            BlockPos nucleusPos = findNucleusPosition(level, pos);
            data.setNucleus(nucleusPos);

            // Colocar bloque de Núcleo
            level.setBlockAndUpdate(nucleusPos,
                    ModBlocks.NUCLEUS.get().defaultBlockState());

            // Generar radio pequeño de Corrupted Mineral alrededor del Núcleo
            generateInitialInfection((ServerLevel) level, nucleusPos, 3);

            // Sincronizar
            NetworkHandler.sendToAllClients(
                    new InfectionSyncPacket(data.getPoints(), data.getPhase())
            );

            player.sendSystemMessage(Component.literal(
                    "§5Algo en tu interior vibra... el meteorito te ha marcado."
            ));
            player.sendSystemMessage(Component.literal(
                    "§8[La infección ha comenzado...]"
            ));
        }

        // Marcar al jugador
        if (!ResonanceData.isMarked(player)) {
            ResonanceData.markPlayer(player);
            if (player instanceof ServerPlayer sp) {
                sp.sendSystemMessage(Component.literal(
                        "§8[La barra de Resonancia se ha activado]"
                ));
            }
        }
    }

    private static BlockPos findNucleusPosition(Level level, BlockPos meteoritePos) {
        int offsetX = (RANDOM.nextInt(21) + 20) * (RANDOM.nextBoolean() ? 1 : -1);
        int offsetZ = (RANDOM.nextInt(21) + 20) * (RANDOM.nextBoolean() ? 1 : -1);

        BlockPos candidate = level.getHeightmapPos(
                net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING,
                meteoritePos.offset(offsetX, 0, offsetZ)
        ).below();

        return candidate;
    }

    private static void generateInitialInfection(ServerLevel level, BlockPos center, int radius) {
        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    double distance = Math.sqrt(x*x + y*y + z*z);
                    if (distance <= radius) {
                        BlockPos pos = center.offset(x, y, z);
                        BlockState current = level.getBlockState(pos);
                        if (!current.isAir()
                                && current.getBlock() != net.minecraft.world.level.block.Blocks.BEDROCK
                                && current.getBlock() != ModBlocks.NUCLEUS.get()) {
                            level.setBlockAndUpdate(pos,
                                    ModBlocks.CORRUPTED_MINERAL.get().defaultBlockState());
                        }
                    }
                }
            }
        }
    }
}