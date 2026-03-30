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

/**
 * Renderer temporal del Coloso usando un bloque como placeholder.
 * Se reemplazará con modelo propio cuando haya texturas.
 */
@OnlyIn(Dist.CLIENT)
public class MineralColossusRenderer extends EntityRenderer<MineralColossusEntity> {

    public MineralColossusRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 1.5f;
    }

    @Override
    public ResourceLocation getTextureLocation(MineralColossusEntity entity) {
        return new ResourceLocation("minecraft", "textures/block/deepslate_bricks.png");
    }

    @Override
    public void render(MineralColossusEntity entity, float entityYaw, float partialTick,
                       PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        poseStack.pushPose();

        // Grande y pesado — 3x4 bloques de escala
        poseStack.translate(-1.5, 0.0, -1.5);
        poseStack.scale(3.0f, 4.0f, 3.0f);

        BlockRenderDispatcher dispatcher = Minecraft.getInstance().getBlockRenderer();
        dispatcher.renderSingleBlock(
                Blocks.DEEPSLATE_BRICKS.defaultBlockState(),
                poseStack, bufferSource, packedLight,
                OverlayTexture.NO_OVERLAY
        );

        poseStack.popPose();
        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
    }
}