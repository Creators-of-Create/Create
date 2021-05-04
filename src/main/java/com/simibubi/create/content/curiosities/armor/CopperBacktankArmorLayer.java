package com.simibubi.create.content.curiosities.armor;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import com.simibubi.create.CreateClient;
import com.simibubi.create.foundation.render.SuperByteBuffer;

import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.IRenderTypeBuffer.Impl;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.LivingRenderer;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.Pose;
import net.minecraft.util.Direction;

public class CopperBacktankArmorLayer<T extends LivingEntity, M extends EntityModel<T>> extends LayerRenderer<T, M> {

	private LivingRenderer<T, M> renderer;

	public CopperBacktankArmorLayer(LivingRenderer<T, M> renderer) {
		super(renderer);
		this.renderer = renderer;
		renderer.addLayer(this);
	}

	@Override
	public void render(MatrixStack ms, IRenderTypeBuffer buffer, int light, LivingEntity entity, float yaw, float pitch,
		float pt, float p_225628_8_, float p_225628_9_, float p_225628_10_) {

		if (entity.getPose() == Pose.SLEEPING)
			return;
		if (!AllItems.COPPER_BACKTANK.get()
			.isWornBy(entity))
			return;

		M entityModel = renderer.getEntityModel();
		if (!(entityModel instanceof BipedModel))
			return;

		ms.push();
		BipedModel<?> model = (BipedModel<?>) entityModel;
		BlockState renderedState = AllBlocks.COPPER_BACKTANK.getDefaultState()
			.with(CopperBacktankBlock.HORIZONTAL_FACING, Direction.SOUTH);
		RenderType renderType = RenderType.getSolid();
		SuperByteBuffer backtank = CreateClient.bufferCache.renderBlock(renderedState);

		model.bipedBody.rotate(ms);
		ms.translate(-1 / 2f, 10 / 16f, 1f);
		ms.scale(1, -1, -1);

		backtank.light(light)
			.renderInto(ms, buffer.getBuffer(renderType));
		if (buffer instanceof Impl)
			((Impl) buffer).draw(renderType);
		ms.pop();

	}

	public static void register() {
		EntityRendererManager renderManager = Minecraft.getInstance()
			.getRenderManager();
		registerOn(renderManager.playerRenderer);
		for (EntityRenderer<?> renderer : renderManager.renderers.values())
			registerOn(renderer);
	}

	private static void registerOn(EntityRenderer<?> entityRenderer) {
		if (!(entityRenderer instanceof LivingRenderer))
			return;
		new CopperBacktankArmorLayer<>((LivingRenderer<?, ?>) entityRenderer);
	}

}
