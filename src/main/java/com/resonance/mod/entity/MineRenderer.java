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
public class MineRenderer extends EntityRenderer<MineEntity> {

    public MineRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.4f; // Sombra pequeña - entidad delgada
    }

    @Override
    public ResourceLocation getTextureLocation(MineEntity entity) {
        return new ResourceLocation("minecraft", "textures/block/iron_block.png");
    }

    @Override
    public void render(MineEntity entity, float entityYaw, float partialTick,
                       PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        poseStack.pushPose();

        // Alto y delgado
        poseStack.translate(-0.25, 0.0, -0.25);
        poseStack.scale(0.5f, 2.0f, 0.5f);

        BlockRenderDispatcher dispatcher = Minecraft.getInstance().getBlockRenderer();
        dispatcher.renderSingleBlock(
                Blocks.IRON_BLOCK.defaultBlockState(),
                poseStack, bufferSource, packedLight,
                OverlayTexture.NO_OVERLAY
        );

        poseStack.popPose();
        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
    }
}