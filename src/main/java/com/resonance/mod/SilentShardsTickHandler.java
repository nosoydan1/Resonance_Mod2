package com.resonance.mod;

import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;

/**
 * Reactiva la IA de mobs detenidos por Silent Shards tras 5 segundos.
 */
@Mod.EventBusSubscriber(modid = ResonanceMod.MODID)
public class SilentShardsTickHandler {

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        event.getServer().getAllLevels().forEach(level -> {
            List<LivingEntity> frozen = level.getEntitiesOfClass(
                    LivingEntity.class,
                    new net.minecraft.world.phys.AABB(
                            level.getWorldBorder().getMinX(), level.getMinBuildHeight(),
                            level.getWorldBorder().getMinZ(),
                            level.getWorldBorder().getMaxX(), level.getMaxBuildHeight(),
                            level.getWorldBorder().getMaxZ()
                    ),
                    e -> e.getPersistentData().contains("ResonanceFrozenTicks")
            );

            for (LivingEntity entity : frozen) {
                int ticks = entity.getPersistentData().getInt("ResonanceFrozenTicks");
                ticks--;
                if (ticks <= 0) {
                    if (entity instanceof net.minecraft.world.entity.Mob mob) {
                        mob.setNoAi(false);
                    }
                    entity.getPersistentData().remove("ResonanceFrozenTicks");
                } else {
                    entity.getPersistentData().putInt("ResonanceFrozenTicks", ticks);
                }
            }
        });
    }
}