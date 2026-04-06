package com.resonance.mod.item;

import com.resonance.mod.InfectionData;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class FrequencySynchronizerItem extends Item {

    public FrequencySynchronizerItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (level.isClientSide()) return InteractionResultHolder.success(stack);

        InfectionData data = InfectionData.get(level);
        BlockPos nucleus = data.getNucleus();

        if (nucleus == null) {
            player.sendSystemMessage(Component.literal("§cNo se detecta Núcleo aún."));
            return InteractionResultHolder.fail(stack);
        }

        // Radio de activación: 20 bloques del Núcleo
        if (player.blockPosition().distSqr(nucleus) > 400) {
            player.sendSystemMessage(Component.literal("§7Aún demasiado lejos..."));
            return InteractionResultHolder.fail(stack);
        }

        // Emitir pulso y mensaje (solo si el nivel es ServerLevel)
        if (level instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(
                    net.minecraft.core.particles.ParticleTypes.WITCH,
                    nucleus.getX() + 0.5, nucleus.getY() + 0.5, nucleus.getZ() + 0.5,
                    30, 1.5, 1.5, 1.5, 0.2
            );

            serverLevel.playSound(null, nucleus.getX(), nucleus.getY(), nucleus.getZ(),
                    SoundEvents.RESPAWN_ANCHOR_CHARGE,
                    SoundSource.BLOCKS, 1.0f, 0.8f);
        }

        // Mostrar fragmento del Códice según fase
        int phase = data.getPhase();
        String codexText = switch (phase) {
            case 1 -> "§5◈ ◇ ▲ ◈ ✦ ▽ ◈";
            case 2 -> "§5\"La tierra está cansada de ser pisoteada...\"";
            case 3 -> "§5\"Siento el peso del cielo...\"";
            case 4 -> "§5\"Pequeños ruidos de carne...\"";
            case 5, 6 -> "§5\"No eres el enemigo que conozco.\"";
            default -> "§8[Sin lectura]";
        };

        player.sendSystemMessage(Component.literal(codexText));

        return InteractionResultHolder.success(stack);
    }
}