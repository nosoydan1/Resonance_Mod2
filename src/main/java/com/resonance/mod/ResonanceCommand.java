package com.resonance.mod;

import com.mojang.brigadier.arguments.FloatArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ResonanceMod.MODID)
public class ResonanceCommand {

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(
                Commands.literal("resonance")
                        // Subcomando /resonance set <valor>
                        .then(Commands.literal("set")
                                .then(Commands.argument("value", FloatArgumentType.floatArg(0, 100))
                                        .executes(ctx -> {
                                            float value = FloatArgumentType.getFloat(ctx, "value");
                                            CommandSourceStack source = ctx.getSource();
                                            Player player = source.getPlayer();
                                            if (player != null) {
                                                ResonanceData.setResonance(player, value);
                                                source.sendSuccess(() -> Component.literal(
                                                        "§aResonancia establecida a " + value + "%"), false);
                                            }
                                            return 1;
                                        })))
                        // Subcomando /resonance get
                        .then(Commands.literal("get")
                                .executes(ctx -> {
                                    CommandSourceStack source = ctx.getSource();
                                    Player player = source.getPlayer();
                                    if (player != null) {
                                        float resonance = ResonanceData.getResonance(player);
                                        source.sendSuccess(() -> Component.literal(
                                                "§5Resonancia actual: " + resonance + "%"), false);
                                    }
                                    return 1;
                                }))
                        // Subcomando /resonance reset
                        .then(Commands.literal("reset")
                                .executes(ctx -> {
                                    CommandSourceStack source = ctx.getSource();
                                    Player player = source.getPlayer();
                                    if (player != null) {
                                        ResonanceData.setResonance(player, 0f);
                                        ResonanceData.markPlayer(player);
                                        source.sendSuccess(() -> Component.literal(
                                                "§aResonancia reseteada a 0%"), false);
                                    }
                                    return 1;
                                }))
        );
    }
}