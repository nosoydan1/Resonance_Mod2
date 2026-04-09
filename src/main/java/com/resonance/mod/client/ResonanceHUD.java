package com.resonance.mod.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.resonance.mod.ClientResonanceData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;

public class ResonanceHUD implements IGuiOverlay {
    public static final ResonanceHUD INSTANCE = new ResonanceHUD();

    private static final int BAR_WIDTH = 100;
    private static final int BAR_HEIGHT = 10;

    @Override
    public void render(ForgeGui gui, GuiGraphics guiGraphics, float partialTick, int screenWidth, int screenHeight) {
        // DEBUG: verificar que el método se ejecuta
        System.out.println("DEBUG: ResonanceHUD.render() called");

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            System.out.println("DEBUG: mc.player es null, saliendo");
            return;
        }

        if (!ClientResonanceData.isMarked()) {
            System.out.println("DEBUG: jugador NO está marcado, saliendo");
            return;
        }
        System.out.println("DEBUG: jugador SÍ está marcado");

        float resonance = ClientResonanceData.getResonance();
        System.out.println("DEBUG: resonance value = " + resonance);
        resonance = Math.min(100f, Math.max(0f, resonance));

        int x = screenWidth - BAR_WIDTH - 10;
        int y = 10;
        int fillWidth = (int) (BAR_WIDTH * (resonance / 100f));

        guiGraphics.fill(x, y, x + BAR_WIDTH, y + BAR_HEIGHT, 0xFF333333);
        guiGraphics.fill(x, y, x + fillWidth, y + BAR_HEIGHT, getBarColor(resonance));

        // Borde
        guiGraphics.fill(x - 1, y - 1, x + BAR_WIDTH + 1, y, 0xFF000000);
        guiGraphics.fill(x - 1, y + BAR_HEIGHT, x + BAR_WIDTH + 1, y + BAR_HEIGHT + 1, 0xFF000000);
        guiGraphics.fill(x - 1, y - 1, x, y + BAR_HEIGHT + 1, 0xFF000000);
        guiGraphics.fill(x + BAR_WIDTH, y - 1, x + BAR_WIDTH + 1, y + BAR_HEIGHT + 1, 0xFF000000);

        String text = "Resonancia: " + (int) resonance + "%";
        guiGraphics.drawString(mc.font, text, x, y - 10, 0xFFAA00FF);
        System.out.println("DEBUG: HUD dibujado correctamente");
    }

    private static int getBarColor(float resonance) {
        if (resonance < 20f) return 0xFF00FF00;
        if (resonance < 50f) return 0xFFFFFF00;
        if (resonance < 80f) return 0xFFFF8800;
        return 0xFFFF0000;
    }
}