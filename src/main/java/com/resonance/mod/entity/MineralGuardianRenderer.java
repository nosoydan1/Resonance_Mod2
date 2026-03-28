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
public class MineralGuardianRenderer extends EntityRenderer<MineralGuardianEntity> {

    public MineralGuardianRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.8f; // Sombra grande - entidad robusta
    }

    @Override
    public ResourceLocation getTextureLocation(MineralGuardianEntity entity) {
        return new ResourceLocation("minecraft", "textures/block/calcite.png");
    }

    @Override
    public void render(MineralGuardianEntity entity, float entityYaw, float partialTick,
                       PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        poseStack.pushPose();

        // Robusto y alto
        poseStack.translate(-0.75, 0.0, -0.75);
        poseStack.scale(1.5f, 1.8f, 1.5f);

        BlockRenderDispatcher dispatcher = Minecraft.getInstance().getBlockRenderer();
        dispatcher.renderSingleBlock(
                Blocks.CALCITE.defaultBlockState(),
                poseStack, bufferSource, packedLight,
                OverlayTexture.NO_OVERLAY
        );

        poseStack.popPose();
        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
    }
}