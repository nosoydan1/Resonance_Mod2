package com.resonance.mod;

import com.resonance.mod.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Random;

@Mod.EventBusSubscriber(modid = ResonanceMod.MODID)
public class MeteoriteEventHandler {

    private static final Random RANDOM = new Random();
    private static final String TAG_METEOR_SPAWNED = "Resonance_MeteorSpawned";
    private static final int METEOR_DELAY = 20 * 60; // 1 minuto
    private static int tickCounter = 0;
    private static boolean meteorScheduled = false;
    private static BlockPos targetPos = null;

    // Ticks para esperar que el bloque caiga antes de generar la esfera
    private static int impactCountdown = -1;

    @SubscribeEvent
    public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (player.getPersistentData().getBoolean(TAG_METEOR_SPAWNED)) return;

        meteorScheduled = true;
        tickCounter = 0;

        player.sendSystemMessage(Component.literal(
                "§8[Un objeto extraño se aproxima desde el cielo...]"
        ));
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        // Countdown para el impacto
        if (impactCountdown > 0) {
            impactCountdown--;
            if (impactCountdown == 0) {
                ServerLevel level = event.getServer().overworld();
                if (targetPos != null) {
                    generateImpact(level, targetPos);
                }
                impactCountdown = -1;
            }
        }

        if (!meteorScheduled) return;

        tickCounter++;

        // Aviso a los 30 segundos
        if (tickCounter == 20 * 30) {
            event.getServer().getAllLevels().forEach(level ->
                    level.players().forEach(p -> p.sendSystemMessage(
                            Component.literal("§c[El cielo tiembla... algo se acerca]")
                    ))
            );
        }

        // Aviso a los 50 segundos
        if (tickCounter == 20 * 50) {
            event.getServer().getAllLevels().forEach(level ->
                    level.players().forEach(p -> p.sendSystemMessage(
                            Component.literal("§4[¡IMPACTO INMINENTE!]")
                    ))
            );
        }

        if (tickCounter < METEOR_DELAY) return;
        meteorScheduled = false;
        tickCounter = 0;

        ServerLevel level = event.getServer().overworld();
        spawnMeteorite(level);
    }

    private static void spawnMeteorite(ServerLevel level) {
        BlockPos spawn = level.getSharedSpawnPos();
        int offsetX = RANDOM.nextInt(100) - 50;
        int offsetZ = RANDOM.nextInt(100) - 50;

        BlockPos surface = level.getHeightmapPos(
                Heightmap.Types.MOTION_BLOCKING,
                spawn.offset(offsetX, 0, offsetZ)
        );

        targetPos = surface;

        // Spawnear bloque cayendo desde muy alto
        BlockPos spawnPos = surface.above(200);
        FallingBlockEntity falling = FallingBlockEntity.fall(
                level, spawnPos,
                ModBlocks.METEORITE_ROCK.get().defaultBlockState()
        );
        falling.setHurtsEntities(10.0f, 80);
        falling.time = 1;

        // Programar impacto — el bloque tarda ~7 segundos en caer desde y+200
        impactCountdown = 20 * 7;

        level.players().forEach(p -> p.sendSystemMessage(
                Component.literal("§6[¡Un meteorito cae del cielo!]")
        ));
    }

    private static void generateImpact(ServerLevel level, BlockPos center) {
        // Destruir y deformar terreno en el impacto (radio más amplio pero menos profundo)
        for (int x = -6; x <= 6; x++) {
            for (int y = -1; y <= 3; y++) { // Menos profundidad
                for (int z = -6; z <= 6; z++) {
                    double distance = Math.sqrt(x*x + y*y + z*z);
                    if (distance <= 5) { // Radio más amplio
                        BlockPos pos = center.offset(x, y, z);
                        BlockState current = level.getBlockState(pos);

                        // Destruir árboles, hojas, vegetación y tierra blanda
                        if (current.getBlock() == Blocks.OAK_LOG ||
                            current.getBlock() == Blocks.OAK_LEAVES ||
                            current.getBlock() == Blocks.BIRCH_LOG ||
                            current.getBlock() == Blocks.BIRCH_LEAVES ||
                            current.getBlock() == Blocks.SPRUCE_LOG ||
                            current.getBlock() == Blocks.SPRUCE_LEAVES ||
                            current.getBlock() == Blocks.DARK_OAK_LOG ||
                            current.getBlock() == Blocks.DARK_OAK_LEAVES ||
                            current.getBlock() == Blocks.JUNGLE_LOG ||
                            current.getBlock() == Blocks.JUNGLE_LEAVES ||
                            current.getBlock() == Blocks.ACACIA_LOG ||
                            current.getBlock() == Blocks.ACACIA_LEAVES ||
                            current.getBlock() == Blocks.MANGROVE_LOG ||
                            current.getBlock() == Blocks.MANGROVE_LEAVES ||
                            current.getBlock() == Blocks.GRASS ||
                            current.getBlock() == Blocks.TALL_GRASS ||
                            current.getBlock() == Blocks.FERN ||
                            current.getBlock() == Blocks.LARGE_FERN ||
                            current.getBlock() == Blocks.DIRT ||
                            current.getBlock() == Blocks.GRASS_BLOCK) {

                            level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());

                            // Generar fragmentos voladores para vegetación destruida
                            if (distance <= 3 && RANDOM.nextFloat() < 0.3f) {
                                spawnDebris(level, pos, current);
                            }
                        }
                        // Deformar terreno - crear cráter poco profundo
                        else if (y <= 0 && distance <= 4 && current.getBlock() != Blocks.BEDROCK) {
                            if (RANDOM.nextFloat() < 0.6f) { // 60% chance de deformar
                                level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
                            } else {
                                // Crear terreno irregular
                                level.setBlockAndUpdate(pos, Blocks.COARSE_DIRT.defaultBlockState());
                            }
                        }
                    }
                }
            }
        }

        // Generar fragmentos voladores alrededor del impacto
        for (int i = 0; i < 15; i++) {
            double angle = RANDOM.nextDouble() * Math.PI * 2;
            double distance = 3 + RANDOM.nextDouble() * 4;
            double x = center.getX() + Math.cos(angle) * distance;
            double z = center.getZ() + Math.sin(angle) * distance;
            BlockPos debrisPos = new BlockPos((int)x, center.getY() + 1, (int)z);

            if (level.getBlockState(debrisPos).isAir()) {
                // Crear fragmento volador
                FallingBlockEntity debris = FallingBlockEntity.fall(
                    level, debrisPos,
                    RANDOM.nextBoolean() ? Blocks.DIRT.defaultBlockState() : Blocks.STONE.defaultBlockState()
                );
                debris.setDeltaMovement(
                    (RANDOM.nextFloat() - 0.5f) * 0.8,
                    0.3 + RANDOM.nextFloat() * 0.4,
                    (RANDOM.nextFloat() - 0.5f) * 0.8
                );
                level.addFreshEntity(debris);
            }
        }

        // Generar esfera de meteorito (radio más pequeño)
        generateMeteoriteSphere(level, center, 2);

        // Sonido de impacto más dramático
        level.playSound(null, center, SoundEvents.GENERIC_EXPLODE,
                SoundSource.BLOCKS, 15.0f, 0.3f);

        // Partículas de explosión más grandes
        level.sendParticles(
                net.minecraft.core.particles.ParticleTypes.EXPLOSION_EMITTER,
                center.getX(), center.getY(), center.getZ(),
                5, 3, 3, 3, 0.2
        );

        // Partículas de polvo y debris
        for (int i = 0; i < 50; i++) {
            level.sendParticles(
                net.minecraft.core.particles.ParticleTypes.CAMPFIRE_COSY_SMOKE,
                center.getX() + (RANDOM.nextFloat() - 0.5) * 10,
                center.getY() + RANDOM.nextFloat() * 3,
                center.getZ() + (RANDOM.nextFloat() - 0.5) * 10,
                1, 0, 0.1, 0, 0.01
            );
        }

        // Marcar jugadores
        level.players().forEach(p -> {
            if (p instanceof ServerPlayer) {
                ServerPlayer sp = (ServerPlayer) p;
                sp.getPersistentData().putBoolean(TAG_METEOR_SPAWNED, true);
                sp.sendSystemMessage(Component.literal(
                        "§5[El meteorito ha impactado. Algo ha despertado...]"
                ));
            }
        });
    }

    private static void spawnDebris(ServerLevel level, BlockPos pos, BlockState originalBlock) {
        // Crear entidad de item que representa debris
        net.minecraft.world.entity.item.ItemEntity debris = new net.minecraft.world.entity.item.ItemEntity(
            level,
            pos.getX() + 0.5,
            pos.getY() + 0.5,
            pos.getZ() + 0.5,
            new net.minecraft.world.item.ItemStack(originalBlock.getBlock().asItem())
        );

        // Dar movimiento aleatorio
        debris.setDeltaMovement(
            (RANDOM.nextFloat() - 0.5f) * 0.6,
            0.2 + RANDOM.nextFloat() * 0.3,
            (RANDOM.nextFloat() - 0.5f) * 0.6
        );

        // Hacer que no dure mucho
        debris.setPickUpDelay(40); // 2 segundos

        level.addFreshEntity(debris);
    }

    public static void generateMeteoriteSphere(ServerLevel level, BlockPos center, int radius) {
        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    double distance = Math.sqrt(x*x + y*y + z*z);
                    if (distance <= radius) {
                        BlockPos pos = center.offset(x, y, z);
                        BlockState current = level.getBlockState(pos);
                        if (current.getBlock() != Blocks.BEDROCK) {
                            level.setBlockAndUpdate(pos,
                                    ModBlocks.METEORITE_ROCK.get().defaultBlockState());
                        }
                    }
                }
            }
        }
    }
}