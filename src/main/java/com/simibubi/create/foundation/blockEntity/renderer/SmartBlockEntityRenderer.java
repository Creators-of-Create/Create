package com.simibubi.create.foundation.blockEntity.renderer;

import org.joml.Matrix4f;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.content.redstone.link.LinkRenderer;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.filtering.FilteringRenderer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.HitResult.Type;
import net.minecraft.world.phys.Vec3;

public class SmartBlockEntityRenderer<T extends SmartBlockEntity> extends SafeBlockEntityRenderer<T> {

	public SmartBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
	}

	@Override
	protected void renderSafe(T blockEntity, float partialTicks, PoseStack ms, MultiBufferSource buffer, int light,
			int overlay) {
		FilteringRenderer.renderOnBlockEntity(blockEntity, partialTicks, ms, buffer, light, overlay);
		LinkRenderer.renderOnBlockEntity(blockEntity, partialTicks, ms, buffer, light, overlay);
	}

	protected void renderNameplateOnHover(T blockEntity, Component tag, float yOffset, PoseStack ms,
		MultiBufferSource buffer, int light) {
		Minecraft mc = Minecraft.getInstance();
		if (blockEntity.isVirtual())
			return;
		if (mc.player.distanceToSqr(Vec3.atCenterOf(blockEntity.getBlockPos())) > 4096.0f)
			return;
		HitResult hitResult = mc.hitResult;
		if (!(hitResult instanceof BlockHitResult bhr) || bhr.getType() == Type.MISS || !bhr.getBlockPos()
			.equals(blockEntity.getBlockPos()))
			return;

		float f = yOffset + 0.25f;
		ms.pushPose();
		ms.translate(0.5, f, 0.5);
		ms.mulPose(mc.getEntityRenderDispatcher()
			.cameraOrientation());
		ms.scale(0.025F, -0.025F, 0.025F);
		Matrix4f matrix4f = ms.last()
			.pose();
		float f2 = Minecraft.getInstance().options.getBackgroundOpacity(0.25F);
		int j = (int) (f2 * 255.0F) << 24;
		Font font = mc.font;
		float f1 = (float) (-font.width(tag) / 2);
		font.drawInBatch(tag, f1, (float) 0, 553648127, false, matrix4f, buffer, Font.DisplayMode.SEE_THROUGH, j,
			light);
		font.drawInBatch(tag, f1, (float) 0, -1, false, matrix4f, buffer, Font.DisplayMode.NORMAL, 0, light);
		ms.popPose();
	}

}
