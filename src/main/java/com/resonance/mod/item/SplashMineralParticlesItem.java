package com.resonance.mod.item;

import com.resonance.mod.InfectionData;
import com.resonance.mod.MobInfectionHandler;
import com.resonance.mod.block.CorruptedMineralBlock;
import com.resonance.mod.network.InfectionSyncPacket;
import com.resonance.mod.network.NetworkHandler;
import com.resonance.mod.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrownPotion;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;
import java.util.Random;

/**
 * Splash Mineral Particles — al impactar (GDD §6.4):
 *  - Infecta el bloque que toca
 *  - 50–70% de probabilidad de infectar bloques en radio 2
 *  - Sube Resonancia de jugadores cercanos más rápido (estela residual)
 *  - Daña mobs en el área
 *  - Si un mob muere por impacto o estela → se infecta
 */
@Mod.EventBusSubscriber(modid = com.resonance.mod.ResonanceMod.MODID)
public class SplashMineralParticlesItem extends Item {

    private static final Random RANDOM = new Random();
    private static final int SPLASH_RADIUS = 2;
    private static final float INFECT_CHANCE = 0.60f; // 50–70%, usamos 60%
    private static final float RESONANCE_BONUS = 5.0f; // bonus Resonancia a jugadores cercanos
    private static final float SPLASH_DAMAGE = 4.0f;

    public SplashMineralParticlesItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (level.isClientSide()) return InteractionResultHolder.success(stack);

        // Lanzar como proyectil
        ThrownPotion projectile = new ThrownPotion(level, player);
        ItemStack potionStack = new ItemStack(net.minecraft.world.item.Items.SPLASH_POTION);
        PotionUtils.setPotion(potionStack, Potions.WATER);
        projectile.setItem(potionStack);
        projectile.shootFromRotation(player, player.getXRot(), player.getYRot(), -10.0f, 0.7f, 1.0f);

        // Marcar como Splash Mineral
        projectile.getPersistentData().putBoolean("ResonanceSplashMineral", true);

        level.addFreshEntity(projectile);

        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.SPLASH_POTION_THROW, SoundSource.PLAYERS, 0.5f, 0.4f);

        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }

        return InteractionResultHolder.success(stack);
    }

    @SubscribeEvent
    public static void onProjectileImpact(ProjectileImpactEvent event) {
        if (!(event.getProjectile() instanceof ThrownPotion potion)) return;
        if (!potion.getPersistentData().getBoolean("ResonanceSplashMineral")) return;

        event.setCanceled(true);
        potion.discard();

        // Declarar level y center ANTES de usarlos
        Level level = potion.level();
        if (!(level instanceof ServerLevel serverLevel)) return;

        BlockPos center = potion.blockPosition();
        InfectionData data = InfectionData.get(level);

        // Partículas visuales al impactar
        serverLevel.sendParticles(
                net.minecraft.core.particles.ParticleTypes.WITCH,
                center.getX(), center.getY() + 0.5, center.getZ(),
                30, 0.8, 0.5, 0.8, 0.1);
        serverLevel.sendParticles(
                net.minecraft.core.particles.ParticleTypes.ASH,
                center.getX(), center.getY() + 0.5, center.getZ(),
                20, 0.6, 0.3, 0.6, 0.05);
        serverLevel.playSound(null, center.getX(), center.getY(), center.getZ(),
                net.minecraft.sounds.SoundEvents.SPLASH_POTION_BREAK,
                net.minecraft.sounds.SoundSource.PLAYERS, 1.0f, 0.8f);

        // 1. Infectar bloque de impacto directo
        infectBlock(serverLevel, center, data);

        // 2. Infectar bloques en radio 2 con probabilidad
        for (BlockPos pos : BlockPos.betweenClosed(
                center.offset(-SPLASH_RADIUS, -SPLASH_RADIUS, -SPLASH_RADIUS),
                center.offset(SPLASH_RADIUS, SPLASH_RADIUS, SPLASH_RADIUS))) {
            if (pos.equals(center)) continue;
            if (RANDOM.nextFloat() < INFECT_CHANCE) {
                infectBlock(serverLevel, pos.immutable(), data);
            }
        }

        // 3. Dañar mobs en el área y posiblemente infectarlos
        List<LivingEntity> nearby = serverLevel.getEntitiesOfClass(LivingEntity.class,
                new AABB(center).inflate(SPLASH_RADIUS));
        for (LivingEntity entity : nearby) {
            if (entity instanceof Player) continue;
            entity.hurt(serverLevel.damageSources().magic(), SPLASH_DAMAGE);
            if (!entity.isAlive() || MobInfectionHandler.isInfected(entity)) continue;
            MobInfectionHandler.infectEntity(entity);
        }

        // 4. Subir Resonancia a jugadores cercanos
        List<Player> players = serverLevel.getEntitiesOfClass(Player.class,
                new AABB(center).inflate(SPLASH_RADIUS + 2));
        for (Player player : players) {
            if (!com.resonance.mod.ResonanceData.isMarked(player)) continue;
            com.resonance.mod.ResonanceData.addResonance(player, RESONANCE_BONUS);
        }

        // 5. Sincronizar puntos
        NetworkHandler.sendToAllClients(
                new InfectionSyncPacket(data.getPoints(), data.getPhase()));
    }

    private static void infectBlock(ServerLevel level, BlockPos pos, InfectionData data) {
        Block block = level.getBlockState(pos).getBlock();
        if (CorruptedMineralBlock.canInfectStatic(block)) {
            level.setBlockAndUpdate(pos,
                    ModBlocks.CORRUPTED_MINERAL.get().defaultBlockState());
            data.addPoints(InfectionData.getPointsForBlock(
                    net.minecraft.core.registries.BuiltInRegistries.BLOCK
                            .getKey(block).toString()));
        }
    }
}