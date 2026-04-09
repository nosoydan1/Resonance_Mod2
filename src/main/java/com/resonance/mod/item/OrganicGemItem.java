package com.resonance.mod.item;

import com.resonance.mod.ResonanceData;
import com.resonance.mod.registry.ModBlocks;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class OrganicGemItem extends BlockItem {
    public OrganicGemItem(Properties properties) {
        super(ModBlocks.MINERAL_SPIKE.get(), properties);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (!level.isClientSide && entity instanceof Player player) {
            // +5% de resonancia al comer (como el GDD)
            float current = ResonanceData.getResonance(player);
            ResonanceData.setResonance(player, current + 5.0f);
        }
        return super.finishUsingItem(stack, level, entity);
    }
}