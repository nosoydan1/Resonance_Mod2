package com.resonance.mod.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Blocks;

public class EchoRenderer extends EntityRenderer<EchoEntity> {

    public EchoRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public ResourceLocation getTextureLocation(EchoEntity entity) {
        return new ResourceLocation("minecraft", "textures/block/sculk_sensor.png");
    }

    @Override
    public void render(EchoEntity entity, float entityYaw, float partialTick,
                       PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        // Escala basada en la edad (opcional)
        int age = entity.getAge();
        int maxAge = entity.getMaxAge();
        float scale = 1.0f;
        if (maxAge > 0) {
            // Escala decrece linealmente desde 1.0 hasta 0.2 en los últimos 20 ticks
            float progress = (float) age / maxAge;
            scale = 1.0f - progress * 0.8f;
            if (scale < 0.2f) scale = 0.2f;
        }
        poseStack.pushPose();
        poseStack.scale(scale, scale, scale);
        // Aquí iría el modelo real. Como placeholder, dibujamos un bloque.
        // Nota: Para dibujar un bloque necesitas BlockRenderDispatcher, es más complejo.
        // Por ahora, simplemente aplicamos la escala y dejamos que el modelo por defecto (si existe) se dibuje.
        super.render(entity, entityYaw, partialTick, poseStack, buffer, packedLight);
        poseStack.popPose();
    }
}