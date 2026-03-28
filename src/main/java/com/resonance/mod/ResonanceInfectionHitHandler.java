package com.resonance.mod;

import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Aumenta la Resonancia del jugador cuando recibe daño de un mob infectado.
 *
 * Según el GDD §2.4: "Al golpear al jugador aumentan la Resonancia
 * incluso con Breathing Mask".
 *
 * Ganancia: 3 pts por golpe de infectado vanilla, 1 pt por golpe de
 * entidad del propio mod (ya que estas son consecuencia, no combustible).
 */
@Mod.EventBusSubscriber(modid = ResonanceMod.MODID)
public class ResonanceInfectionHitHandler {

    /** Resonancia ganada por golpe de mob vanilla infectado. */
    private static final float RESONANCE_PER_HIT_INFECTED_VANILLA = 3.0f;
    /** Resonancia ganada por golpe de entidad del propio mod. */
    private static final float RESONANCE_PER_HIT_MOD_ENTITY = 1.0f;

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        // Solo nos interesa cuando el que recibe daño es el jugador
        if (!(event.getEntity() instanceof Player player)) return;
        if (player.level().isClientSide()) return;
        if (!ResonanceData.isMarked(player)) return;

        var attacker = event.getSource().getEntity();
        if (attacker == null) return;

        String namespace = attacker.getType().toString();
        boolean isModEntity = namespace.contains(ResonanceMod.MODID);

        if (isModEntity) {
            // Golpe de entidad del mod (Mine, Ralite, etc.)
            ResonanceData.addResonance(player, RESONANCE_PER_HIT_MOD_ENTITY);
        } else if (attacker instanceof net.minecraft.world.entity.LivingEntity le
                && MobInfectionHandler.isInfected(le)) {
            // Golpe de mob vanilla infectado (incluye con Breathing Mask equipada)
            ResonanceData.addResonance(player, RESONANCE_PER_HIT_INFECTED_VANILLA);
        }
    }
}
