package com.resonance.mod.item;

import com.resonance.mod.MobInfectionHandler;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.Random;

/**
 * Silent Shards — clic derecho sobre un mob infectado para detenerlo (GDD §6.4).
 * Probabilidad variable de éxito, similar al hueso de polvo.
 * No garantiza detener al mob en cada uso.
 */
public class SilentShardsItem extends Item {

    private static final Random RANDOM = new Random();
    // Probabilidad base de éxito: 40%
    private static final float BASE_SUCCESS_CHANCE = 0.40f;

    public SilentShardsItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player,
                                                  LivingEntity target, InteractionHand hand) {
        if (player.level().isClientSide()) return InteractionResult.SUCCESS;

        // Solo funciona sobre mobs infectados
        if (!MobInfectionHandler.isInfected(target)) {
            player.sendSystemMessage(Component.literal(
                    "§7Este mob no está infectado."));
            return InteractionResult.PASS;
        }

        // Consumir el ítem
        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }

        if (RANDOM.nextFloat() < BASE_SUCCESS_CHANCE) {
            // Éxito: detener al mob temporalmente (quitar IA 5 segundos)
            if (target instanceof net.minecraft.world.entity.Mob mob) {
                mob.setNoAi(true);
            }
            target.getPersistentData().putInt("ResonanceFrozenTicks", 100);

            player.sendSystemMessage(Component.literal(
                    "§aLos fragmentos silenciosos detienen al infectado."));

            // Registrar tick para reactivar la IA después
            scheduleReactivation(target);
        } else {
            player.sendSystemMessage(Component.literal(
                    "§cLos fragmentos no surtieron efecto..."));
        }

        return InteractionResult.SUCCESS;
    }

    private static void scheduleReactivation(LivingEntity entity) {
        // La reactivación se maneja en SilentShardsTickHandler
    }
}