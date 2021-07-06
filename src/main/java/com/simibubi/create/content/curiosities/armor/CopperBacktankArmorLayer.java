package com.simibubi.create.content.curiosities.armor;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import com.simibubi.create.CreateClient;
import com.simibubi.create.foundation.gui.GuiGameElement;
import com.simibubi.create.foundation.render.SuperByteBuffer;
import com.simibubi.create.foundation.utility.AngleHelper;
import com.simibubi.create.foundation.utility.AnimationTickHolder;
import com.simibubi.create.foundation.utility.ColorHelper;

import net.minecraft.block.BlockState;
import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.renderer.Atlases;
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
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Direction;
import net.minecraft.util.StringUtils;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

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
		RenderType renderType = Atlases.getEntityCutout();

		SuperByteBuffer backtank = CreateClient.BUFFER_CACHE.renderBlock(renderedState);
		SuperByteBuffer cogs =
				CreateClient.BUFFER_CACHE.renderPartial(AllBlockPartials.COPPER_BACKTANK_COGS, renderedState);

		model.bipedBody.rotate(ms);
		ms.translate(-1 / 2f, 10 / 16f, 1f);
		ms.scale(1, -1, -1);
		backtank.forEntityRender()
			.light(light)
			.renderInto(ms, buffer.getBuffer(renderType));

		cogs.matrixStacker()
			.centre()
			.rotateY(180)
			.unCentre()
			.translate(0, 6.5f / 16, 11f / 16)
			.rotate(Direction.EAST, AngleHelper.rad(2 * AnimationTickHolder.getRenderTime(entity.world) % 360))
			.translate(0, -6.5f / 16, -11f / 16);

		cogs.forEntityRender()
			.light(light)
			.renderInto(ms, buffer.getBuffer(renderType));

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

	public static void renderRemainingAirOverlay(MatrixStack ms, Impl buffers, int light, int overlay, float pt) {
		ClientPlayerEntity player = Minecraft.getInstance().player;
		if (player == null)
			return;
		if (player.isSpectator() || player.isCreative())
			return;
		if (!player.getPersistentData()
			.contains("VisualBacktankAir"))
			return;
		if (!player.areEyesInFluid(FluidTags.WATER))
			return;

		int timeLeft = player.getPersistentData()
			.getInt("VisualBacktankAir");

		ms.push();

		MainWindow window = Minecraft.getInstance()
			.getWindow();
		ms.translate(window.getScaledWidth() / 2 + 90, window.getScaledHeight() - 53, 0);

		ITextComponent text = new StringTextComponent(StringUtils.ticksToElapsedTime(timeLeft * 20));
		GuiGameElement.of(AllItems.COPPER_BACKTANK.asStack())
			.at(0, 0)
			.render(ms);
		int color = 0xFF_FFFFFF;
		if (timeLeft < 60 && timeLeft % 2 == 0) {
			color = ColorHelper.mixColors(0xFF_FF0000, color, Math.max(timeLeft / 60f, .25f));
		}
		Minecraft.getInstance().fontRenderer.drawWithShadow(ms, text, 16, 5, color);
		buffers.draw();

		ms.pop();
	}

}
