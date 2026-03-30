package com.resonance.mod.item;

import com.resonance.mod.registry.ModItems;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrownPotion;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.Level;

/**
 * Syringe Gun — dispara una Dissonant Injection como proyectil (GDD §6.3).
 * Requiere tener al menos una Dissonant Injection en el inventario para disparar.
 * El proyectil al impactar aplica el efecto de la inyección al mob o jugador golpeado.
 *
 * Nota: usa ThrownPotion como proyectil base porque tiene física de arco
 * nativa. La lógica de impacto se maneja en SyringeProjectileHandler.
 */
public class SyringeGunItem extends Item {

    private static final int COOLDOWN_TICKS = 20; // 1 segundo entre disparos

    public SyringeGunItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack gun = player.getItemInHand(hand);

        if (level.isClientSide()) return InteractionResultHolder.success(gun);

        // Buscar Dissonant Injection en el inventario
        ItemStack injection = findInjection(player);
        if (injection.isEmpty()) {
            player.sendSystemMessage(Component.literal(
                    "§cNecesitas una Dissonant Injection para disparar."));
            return InteractionResultHolder.fail(gun);
        }

        // Consumir una inyección (excepto en modo creativo)
        if (!player.getAbilities().instabuild) {
            injection.shrink(1);
        }

        // Disparar proyectil
        ThrownPotion projectile = new ThrownPotion(level, player);
        // Usamos una poción de agua como placeholder visual hasta tener textura propia
        ItemStack potionStack = new ItemStack(net.minecraft.world.item.Items.SPLASH_POTION);
        PotionUtils.setPotion(potionStack, Potions.WATER);
        projectile.setItem(potionStack);
        projectile.shootFromRotation(player, player.getXRot(), player.getYRot(), -20.0f, 0.5f, 1.0f);

        // Marcar el proyectil para que el handler sepa que es una inyección
        projectile.getPersistentData().putBoolean("ResonanceSyringe", true);

        level.addFreshEntity(projectile);

        // Sonido de disparo
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.CROSSBOW_SHOOT, SoundSource.PLAYERS, 0.8f, 1.2f);

        // Cooldown visual en la barra
        player.getCooldowns().addCooldown(this, COOLDOWN_TICKS);

        return InteractionResultHolder.success(gun);
    }

    private ItemStack findInjection(Player player) {
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.getItem() == ModItems.DISSONANT_INJECTION.get()) {
                return stack;
            }
        }
        return ItemStack.EMPTY;
    }
}