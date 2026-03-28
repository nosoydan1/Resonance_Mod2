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
public class ChipsRenderer extends EntityRenderer<ChipsEntity> {

    public ChipsRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.3f; // Pequeño - slab aplastado
    }

    @Override
    public ResourceLocation getTextureLocation(ChipsEntity entity) {
        return new ResourceLocation("minecraft", "textures/block/deepslate.png");
    }

    @Override
    public void render(ChipsEntity entity, float entityYaw, float partialTick,
                       PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {

        poseStack.pushPose();

        // Centrar y achatar como una slab
        poseStack.translate(-0.5, 0.0, -0.5);
        poseStack.scale(1.0f, 0.5f, 1.0f);

        BlockRenderDispatcher dispatcher = Minecraft.getInstance().getBlockRenderer();
        dispatcher.renderSingleBlock(
                Blocks.DEEPSLATE.defaultBlockState(),
                poseStack,
                bufferSource,
                packedLight,
                OverlayTexture.NO_OVERLAY
        );

        poseStack.popPose();

        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
    }
}