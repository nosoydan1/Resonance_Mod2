package com.resonance.mod.block;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.MapColor;

public class DampingMechanismBlock extends Block {

    /**
     * Constructor sin argumentos para que ModBlocks.java no de error.
     * Definimos las propiedades aquí mismo (GDD §2.4 - Bloque metálico pesado).
     */
    public DampingMechanismBlock() {
        super(Properties.of()
                .mapColor(MapColor.METAL)
                .strength(5.0f, 6.0f) // Resistencia similar al hierro
                .requiresCorrectToolForDrops()
                .sound(SoundType.METAL));
    }

    /**
     * Lógica de protección (GDD §2.4).
     * Por ahora devuelve false, pero el compilador ya puede "ver" el método.
     */
    public static boolean playerHasDampingProtection(net.minecraft.world.level.Level level, net.minecraft.core.BlockPos pos) {
        // Aquí programaremos luego el radio de 16 bloques que mencionaste
        return false;
    }
}