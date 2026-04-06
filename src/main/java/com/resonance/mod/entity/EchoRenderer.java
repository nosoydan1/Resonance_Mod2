package com.resonance.mod.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class EchoRenderer extends EntityRenderer<EchoEntity> {

    public EchoRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.3f;
    }

    @Override
    public ResourceLocation getTextureLocation(EchoEntity entity) {
        return new ResourceLocation("minecraft", "textures/block/sculk_sensor.png");
    }

    @Override
    public void render(EchoEntity entity, float entityYaw, float partialTick,
                       PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        poseStack.pushPose();

        // Pequeño, flotante
        poseStack.translate(-0.25, 0.0, -0.25);
        poseStack.scale(0.5f, 0.5f, 0.5f);

        // Animación de rotación continua (gira sobre Y)
        poseStack.mulPose(Axis.YP.rotationDegrees(entity.tickCount * 2f));

        // Animación de escala basada en la edad (pulso)
        int age = entity.getAge();          // supone que EchoEntity tiene getAge()
        int maxAge = entity.getMaxAge();    // supone que EchoEntity tiene getMaxAge()
        if (maxAge > 0) {
            float progress = (float) age / maxAge;  // 0 = recién nacido, 1 = a punto de morir
            // Efecto de "latido": escala entre 0.5 y 0.7
            float scale = 0.5f + (float) Math.sin(age * 0.2f) * 0.1f;
            poseStack.scale(scale, scale, scale);
        }

        BlockRenderDispatcher dispatcher = Minecraft.getInstance().getBlockRenderer();
        dispatcher.renderSingleBlock(
                Blocks.SCULK_SENSOR.defaultBlockState(),
                poseStack, bufferSource, packedLight,
                OverlayTexture.NO_OVERLAY
        );

        poseStack.popPose();
        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
    }
}