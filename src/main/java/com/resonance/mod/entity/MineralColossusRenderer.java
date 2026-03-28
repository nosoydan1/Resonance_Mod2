package com.resonance.mod.entity;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.model.IronGolemModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Mob;
import com.resonance.mod.ResonanceMod;

// Usamos Mob como parámetro del modelo para saltarnos la restricción del Iron Golem
public class MineralColossusRenderer extends MobRenderer<MineralColossusEntity, IronGolemModel<Mob>> {
    public MineralColossusRenderer(EntityRendererProvider.Context context) {
        // El casting (IronGolemModel) le dice a Java: "Yo sé lo que hago, úsalo"
        super(context, new IronGolemModel(context.bakeLayer(ModelLayers.IRON_GOLEM)), 0.7F);
    }

    @Override
    public ResourceLocation getTextureLocation(MineralColossusEntity entity) {
        return new ResourceLocation(ResonanceMod.MODID, "textures/entity/mineral_colossus.png");
    }
}