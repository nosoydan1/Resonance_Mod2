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
public class AshenKnightRenderer extends EntityRenderer<AshenKnightEntity> {

    public AshenKnightRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.5f; // Sombra pequeña para entidad etérea
    }

    @Override
    public ResourceLocation getTextureLocation(AshenKnightEntity entity) {
        return new ResourceLocation("minecraft", "textures/block/soul_sand.png");
    }

    @Override
    public void render(AshenKnightEntity entity, float entityYaw, float partialTick,
                       PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        poseStack.pushPose();

        // Forma etérea — delgado y alto como una sombra
        poseStack.translate(-0.3, 0.0, -0.3);
        poseStack.scale(0.6f, 1.8f, 0.6f);

        BlockRenderDispatcher dispatcher = Minecraft.getInstance().getBlockRenderer();
        dispatcher.renderSingleBlock(
                Blocks.SOUL_SAND.defaultBlockState(),
                poseStack, bufferSource, packedLight,
                OverlayTexture.NO_OVERLAY
        );

        poseStack.popPose();
        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
    }
}