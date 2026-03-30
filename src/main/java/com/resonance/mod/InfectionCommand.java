package com.resonance.mod;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.resonance.mod.network.InfectionSyncPacket;
import com.resonance.mod.network.NetworkHandler;
import com.resonance.mod.registry.ModBlocks;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.List;

@Mod.EventBusSubscriber(modid = ResonanceMod.MODID)
public class InfectionCommand {

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(
                Commands.literal("infection")
                        .then(Commands.literal("get")
                                .executes(ctx -> {
                                    CommandSourceStack source = ctx.getSource();
                                    InfectionData data = InfectionData.get(source.getLevel());
                                    source.sendSuccess(() -> Component.literal(
                                            "§5Fase: " + data.getPhase() + " | Puntos: " + data.getPoints()
                                    ), false);
                                    return 1;
                                }))
                        .then(Commands.literal("addpoints")
                                .then(Commands.argument("amount", IntegerArgumentType.integer(1))
                                        .executes(ctx -> {
                                            int amount = IntegerArgumentType.getInteger(ctx, "amount");
                                            CommandSourceStack source = ctx.getSource();
                                            InfectionData data = InfectionData.get(source.getLevel());
                                            data.addPoints(amount);
                                            NetworkHandler.sendToAllClients(
                                                    new InfectionSyncPacket(data.getPoints(), data.getPhase())
                                            );
                                            source.sendSuccess(() -> Component.literal(
                                                    "§5Puntos añadidos: " + amount + " | Fase actual: " + data.getPhase()
                                            ), false);
                                            return 1;
                                        })))
                        .then(Commands.literal("scan")
                                .executes(ctx -> {
                                    CommandSourceStack source = ctx.getSource();
                                    ServerLevel level = source.getLevel();
                                    BlockPos center = BlockPos.containing(source.getPosition());
                                    int radius = 32;
                                    List<BlockPos> found = new ArrayList<>();

                                    for (int x = -radius; x <= radius; x++) {
                                        for (int z = -radius; z <= radius; z++) {
                                            for (int y = -radius; y <= radius; y++) {
                                                BlockPos checkPos = center.offset(x, y, z);
                                                Block block = level.getBlockState(checkPos).getBlock();
                                                if (block == ModBlocks.CORRUPTED_MINERAL.get()
                                                        || block == ModBlocks.CORRUPTED_MINERAL_ORE.get()) {
                                                    found.add(checkPos);
                                                }
                                            }
                                        }
                                    }

                                    int count = found.size();
                                    source.sendSuccess(() -> Component.literal(
                                            "§5Bloques corruptos encontrados cerca: " + count
                                    ), false);
                                    return 1;
                                }))
                        .then(Commands.literal("nucleus")
                                .executes(ctx -> {
                                    CommandSourceStack source = ctx.getSource();
                                    InfectionData data = InfectionData.get(source.getLevel());
                                    BlockPos nucleus = data.getNucleus();
                                    if (nucleus == null) {
                                        source.sendSuccess(() -> Component.literal(
                                                "§cNúcleo no encontrado aún."
                                        ), false);
                                    } else {
                                        source.sendSuccess(() -> Component.literal(
                                                "§5Núcleo en: X=" + nucleus.getX() +
                                                        " Y=" + nucleus.getY() +
                                                        " Z=" + nucleus.getZ()
                                        ), false);
                                    }
                                    return 1;
                                }))
                        .then(Commands.literal("spawn")
                                .executes(ctx -> {
                                    CommandSourceStack source = ctx.getSource();
                                    ServerLevel level = source.getLevel();
                                    InfectionData data = InfectionData.get(level);
                                    BlockPos nucleus = data.getNucleus();
                                    int phase = data.getPhase();

                                    if (nucleus == null) {
                                        source.sendSuccess(() -> Component.literal(
                                                "§cNo hay núcleo definido."
                                        ), false);
                                        return 0;
                                    }

                                    BlockPos spawnPos = BlockPos.containing(source.getPosition());

                                    var mob = switch (phase) {
                                        case 1 -> com.resonance.mod.registry.ModEntities.CHIPS.get().create(level);
                                        case 2 -> com.resonance.mod.registry.ModEntities.MINE.get().create(level);
                                        case 3 -> com.resonance.mod.registry.ModEntities.RALITE.get().create(level);
                                        case 4 -> com.resonance.mod.registry.ModEntities.ASHEN_KNIGHT.get().create(level);
                                        case 5 -> com.resonance.mod.registry.ModEntities.MINERAL_GUARDIAN.get().create(level);
                                        default -> null;
                                    };

                                    if (mob == null) {
                                        source.sendSuccess(() -> Component.literal(
                                                "§cNo hay mob para esta fase."
                                        ), false);
                                        return 0;
                                    }

                                    mob.moveTo(spawnPos.getX(), spawnPos.getY(), spawnPos.getZ(), 0, 0);
                                    level.addFreshEntity(mob);

                                    source.sendSuccess(() -> Component.literal(
                                            "§5Mob spawneado para fase " + phase
                                    ), false);
                                    return 1;
                                }))
                        .then(Commands.literal("colossus")
                                .then(Commands.literal("status")
                                        .executes(ctx -> {
                                            CommandSourceStack source = ctx.getSource();
                                            ServerLevel level = source.getLevel();

                                            var colossusList = level.getEntitiesOfClass(
                                                    com.resonance.mod.entity.MineralColossusEntity.class,
                                                    new net.minecraft.world.phys.AABB(
                                                            level.getWorldBorder().getMinX(), level.getMinBuildHeight(),
                                                            level.getWorldBorder().getMinZ(),
                                                            level.getWorldBorder().getMaxX(), level.getMaxBuildHeight(),
                                                            level.getWorldBorder().getMaxZ()
                                                    ));

                                            if (colossusList.isEmpty()) {
                                                source.sendSuccess(() -> Component.literal(
                                                        "§cNo hay Coloso activo en el mundo."), false);
                                            } else {
                                                var colossus = colossusList.get(0);
                                                float hp = colossus.getHealth();
                                                float maxHp = (float) colossus.getAttributeValue(
                                                        net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH);
                                                int phase = colossus.getCombatPhase();
                                                source.sendSuccess(() -> Component.literal(
                                                        "§5Coloso — Fase: " + phase +
                                                                " | HP: " + (int)hp + "/" + (int)maxHp +
                                                                " (" + (int)(hp/maxHp*100) + "%)"), false);
                                            }
                                            return 1;
                                        }))
                                .then(Commands.literal("setphase")
                                        .then(Commands.argument("phase",
                                                        IntegerArgumentType.integer(1, 3))
                                                .executes(ctx -> {
                                                    int targetPhase = IntegerArgumentType.getInteger(ctx, "phase");
                                                    CommandSourceStack source = ctx.getSource();
                                                    ServerLevel level = source.getLevel();

                                                    var colossusList = level.getEntitiesOfClass(
                                                            com.resonance.mod.entity.MineralColossusEntity.class,
                                                            new net.minecraft.world.phys.AABB(
                                                                    level.getWorldBorder().getMinX(), level.getMinBuildHeight(),
                                                                    level.getWorldBorder().getMinZ(),
                                                                    level.getWorldBorder().getMaxX(), level.getMaxBuildHeight(),
                                                                    level.getWorldBorder().getMaxZ()
                                                            ));

                                                    if (colossusList.isEmpty()) {
                                                        source.sendSuccess(() -> Component.literal(
                                                                "§cNo hay Coloso activo."), false);
                                                    } else {
                                                        colossusList.get(0).setCombatPhase(targetPhase);
                                                        source.sendSuccess(() -> Component.literal(
                                                                "§5Fase del Coloso cambiada a " + targetPhase), false);
                                                    }
                                                    return 1;
                                                }))))
        );
    }

}