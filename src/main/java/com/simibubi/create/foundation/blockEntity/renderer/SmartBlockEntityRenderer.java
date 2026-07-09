package com.simibubi.create.foundation.blockEntity.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.content.redstone.link.LinkRenderer;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.filtering.FilteringRenderer;

import net.minecraft.client.Minecraft;
import net.createmod.catnip.api.client.render.MultiBufferSource;
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

		// 26.2 moved world text rendering away from the old immediate Font.drawInBatch path.
		// Keep the hover/nameplate gate intact until this is ported to the new render-state API.
	}

}
