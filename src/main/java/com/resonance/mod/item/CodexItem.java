package com.resonance.mod.item;
import net.minecraft.world.item.Item;

public class CodexItem extends Item {
    private final int level;
    public CodexItem(int level, Properties properties) {
        super(properties);
        this.level = level;
    }
}