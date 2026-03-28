package com.resonance.mod;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ResonanceMod.MODID, value = Dist.CLIENT)
public class ResonanceHUD {

    @SubscribeEvent
    public static void onRenderHUD(RenderGuiOverlayEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null) return;

        // Solo mostrar si el jugador está marcado
        if (!ResonanceData.isMarked(player)) return;

        float resonance = ResonanceData.getResonance(player);

        GuiGraphics gui = event.getGuiGraphics();
        int screenWidth = mc.getWindow().getGuiScaledWidth();

        int barWidth = 100;
        int barHeight = 8;
        int x = screenWidth - barWidth - 10;
        int y = 10;

        // Fondo
        gui.fill(x, y, x + barWidth, y + barHeight, 0xFF333333);

        // Relleno
        int fillWidth = (int) (barWidth * (resonance / 100f));
        int color = getBarColor(resonance);
        gui.fill(x, y, x + fillWidth, y + barHeight, color);

        // Borde
        gui.fill(x - 1, y - 1, x + barWidth + 1, y, 0xFF000000);
        gui.fill(x - 1, y + barHeight, x + barWidth + 1, y + barHeight + 1, 0xFF000000);
        gui.fill(x - 1, y - 1, x, y + barHeight + 1, 0xFF000000);
        gui.fill(x + barWidth, y - 1, x + barWidth + 1, y + barHeight + 1, 0xFF000000);

        // Texto
        String text = "Resonancia: " + (int) resonance + "%";
        gui.drawString(mc.font, text, x, y - 10, 0xFFAA00FF);
    }

    private static int getBarColor(float resonance) {
        if (resonance < 20f) return 0xFF00FF00;
        if (resonance < 50f) return 0xFFFFFF00;
        if (resonance < 80f) return 0xFFFF8800;
        return 0xFFFF0000;
    }
}