package com.resonance.mod.entity;

import com.mojang.blaze3d.vertex.PoseStack;
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
public class RaliteRenderer extends EntityRenderer<RaliteEntity> {

    public RaliteRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.6f; // Sombra mediana - entidad robusta
    }

    @Override
    public ResourceLocation getTextureLocation(RaliteEntity entity) {
        return new ResourceLocation("minecraft", "textures/block/stone_bricks.png");
    }

    @Override
    public void render(RaliteEntity entity, float entityYaw, float partialTick,
                       PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        poseStack.pushPose();

        // Robusto y pesado
        poseStack.translate(-0.6, 0.0, -0.6);
        poseStack.scale(1.2f, 1.2f, 1.2f);

        BlockRenderDispatcher dispatcher = Minecraft.getInstance().getBlockRenderer();
        dispatcher.renderSingleBlock(
                Blocks.STONE_BRICKS.defaultBlockState(),
                poseStack, bufferSource, packedLight,
                OverlayTexture.NO_OVERLAY
        );

        poseStack.popPose();
        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
    }
}