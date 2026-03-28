package com.resonance.mod;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

public class WalkToNucleusGoal extends Goal {

    private final PathfinderMob mob;
    private final BlockPos nucleus;
    private final double speed;
    private int recalcTimer = 0;

    public WalkToNucleusGoal(PathfinderMob mob, BlockPos nucleus, double speed) {
        this.mob = mob;
        this.nucleus = nucleus;
        this.speed = speed;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        if (nucleus == null) return false;
        // Solo caminar si está a más de 3 bloques del núcleo
        return mob.blockPosition().distSqr(nucleus) > 9;
    }

    @Override
    public boolean canContinueToUse() {
        return canUse();
    }

    @Override
    public void tick() {
        recalcTimer--;
        if (recalcTimer <= 0) {
            recalcTimer = 20;
            mob.getNavigation().moveTo(
                    nucleus.getX(), nucleus.getY(), nucleus.getZ(), speed
            );
        }
    }

    @Override
    public void stop() {
        mob.getNavigation().stop();
    }
}