package com.resonance.mod.item;

import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * Códice — ítem narrativo en 3 niveles (GDD §6.6).
 *
 * Nivel 1 — Incomprensible: símbolos ilegibles, el jugador no entiende nada.
 * Nivel 2 — Sintonizado:    revela las frases del Núcleo. El jugador entiende que algo piensa.
 * Nivel 3 — Revelación:     revela la Amenaza Real y el propósito protector del Núcleo.
 */
public class CodexItem extends Item {

    private final int level;

    public CodexItem(int level, Properties properties) {
        super(properties);
        this.level = level;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
        if (!world.isClientSide()) {
            switch (this.level) {
                case 1 -> showLevel1(player);
                case 2 -> showLevel2(player);
                case 3 -> showLevel3(player);
            }
        }
        return InteractionResultHolder.success(player.getItemInHand(hand));
    }

    // -------------------------------------------------------------------------
    // Nivel 1 — Incomprensible
    // -------------------------------------------------------------------------
    private static void showLevel1(Player player) {
        player.sendSystemMessage(Component.literal("§8[ Códice — Nivel 1: Incomprensible ]"));
        player.sendSystemMessage(Component.literal("§5◈ ◇ ▲ ◈ ✦ ▽ ◈ ◇ ✦"));
        player.sendSystemMessage(Component.literal("§5▲ ✦ ◈ ◇ ▽ ▲ ◇ ✦ ◈"));
        player.sendSystemMessage(Component.literal("§5◇ ▽ ✦ ◈ ▲ ◇ ◈ ▽ ✦"));
        player.sendSystemMessage(Component.literal("§8[ Los símbolos vibran pero no dicen nada que puedas entender... ]"));
        player.sendSystemMessage(Component.literal("§8[ Quizás necesitas sintonizarte más con la infección. ]"));
    }

    // -------------------------------------------------------------------------
    // Nivel 2 — Sintonizado: frases del Núcleo del GDD §1.1
    // -------------------------------------------------------------------------
    private static void showLevel2(Player player) {
        player.sendSystemMessage(Component.literal("§5§l[ Códice — Nivel 2: Sintonizado ]"));
        player.sendSystemMessage(Component.literal("§7Los símbolos cobran forma. Algo ha estado pensando."));
        player.sendSystemMessage(Component.literal(""));
        player.sendSystemMessage(Component.literal(
                "§5\"La tierra está cansada de ser pisoteada..."));
        player.sendSystemMessage(Component.literal(
                "§5 ahora ella se levantará.\""));
        player.sendSystemMessage(Component.literal(""));
        player.sendSystemMessage(Component.literal(
                "§5\"Siento el peso del cielo,"));
        player.sendSystemMessage(Component.literal(
                "§5 y el cielo debe caer.\""));
        player.sendSystemMessage(Component.literal(""));
        player.sendSystemMessage(Component.literal(
                "§5\"Pequeños ruidos de carne..."));
        player.sendSystemMessage(Component.literal(
                "§5 pronto todo será el silencio perfecto de la roca.\""));
        player.sendSystemMessage(Component.literal(""));
        player.sendSystemMessage(Component.literal(
                "§8[ Hay un intelecto detrás de la infección. Y te ha visto. ]"));
    }

    // -------------------------------------------------------------------------
    // Nivel 3 — Revelación: la Amenaza Real y el giro narrativo del GDD §1.2
    // -------------------------------------------------------------------------
    private static void showLevel3(Player player) {
        player.sendSystemMessage(Component.literal("§5§l[ Códice — Nivel 3: Revelación ]"));
        player.sendSystemMessage(Component.literal(""));
        player.sendSystemMessage(Component.literal(
                "§7La verdad se despliega como capas de roca bajo presión."));
        player.sendSystemMessage(Component.literal(""));
        player.sendSystemMessage(Component.literal(
                "§5\"No eres el enemigo que conozco."));
        player.sendSystemMessage(Component.literal(
                "§5 Pero llevas su marca. El meteorito te tocó."));
        player.sendSystemMessage(Component.literal(
                "§5 Y yo no puedo distinguir al portador de la amenaza.\""));
        player.sendSystemMessage(Component.literal(""));
        player.sendSystemMessage(Component.literal(
                "§7El Núcleo no despertó por capricho."));
        player.sendSystemMessage(Component.literal(
                "§7Algo se aproxima desde el exterior — algo que el Núcleo"));
        player.sendSystemMessage(Component.literal(
                "§7reconoce como una amenaza capaz de consumirlo todo."));
        player.sendSystemMessage(Component.literal(
                "§7El Coloso era su respuesta. Tú eras la confusión."));
        player.sendSystemMessage(Component.literal(""));
        player.sendSystemMessage(Component.literal(
                "§5\"Construí un cuerpo para enfrentar lo que viene."));
        player.sendSystemMessage(Component.literal(
                "§5 Tú lo destruiste. Ahora lo que viene..."));
        player.sendSystemMessage(Component.literal(
                "§5 vendrá sin que nadie lo detenga.\""));
        player.sendSystemMessage(Component.literal(""));
        player.sendSystemMessage(Component.literal(
                "§8[ ¿Ganaste? ¿O solo abriste una puerta que no debía abrirse? ]"));
    }
}