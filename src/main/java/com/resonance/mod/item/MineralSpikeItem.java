package com.resonance.mod.item;

import com.resonance.mod.block.MineralSpikeBlock;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.block.Block;

public class MineralSpikeItem extends BlockItem {
    public MineralSpikeItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.EAT;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        // Primero intenta plantar (el bloque se colocará solo si la posición es válida)
        return super.useOn(context);
    }
}