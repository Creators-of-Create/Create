package com.simibubi.create.foundation.placement;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import com.simibubi.create.foundation.gui.AllGuiTextures;
import com.simibubi.create.foundation.utility.AngleHelper;
import com.simibubi.create.foundation.utility.VecHelper;
import com.simibubi.create.foundation.utility.animation.LerpedFloat;
import com.simibubi.create.foundation.utility.animation.LerpedFloat.Chaser;
import com.simibubi.create.infrastructure.config.AllConfigs;
import com.simibubi.create.infrastructure.config.CClient;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class PlacementHelpers {

	private static final List<IPlacementHelper> helpers = new ArrayList<>();
	private static int animationTick = 0;
	private static final LerpedFloat angle = LerpedFloat.angular()
		.chase(0, 0.25f, Chaser.EXP);
	private static BlockPos target = null;
	private static BlockPos lastTarget = null;

	public static int register(IPlacementHelper helper) {
		helpers.add(helper);
		return helpers.size() - 1;
	}

	public static IPlacementHelper get(int id) {
		if (id < 0 || id >= helpers.size())
			throw new ArrayIndexOutOfBoundsException("id " + id + " for placement helper not known");

		return helpers.get(id);
	}

	@OnlyIn(Dist.CLIENT)
	public static void tick() {
		setTarget(null);
		checkHelpers();

		if (target == null) {
			if (animationTick > 0)
				animationTick = Math.max(animationTick - 2, 0);

			return;
		}

		if (animationTick < 10)
			animationTick++;

	}

	@OnlyIn(Dist.CLIENT)
	private static void checkHelpers() {
		Minecraft mc = Minecraft.getInstance();
		ClientLevel world = mc.level;

		if (world == null)
			return;

		if (!(mc.hitResult instanceof BlockHitResult))
			return;

		BlockHitResult ray = (BlockHitResult) mc.hitResult;

		if (mc.player == null)
			return;

		if (mc.player.isShiftKeyDown())// for now, disable all helpers when sneaking TODO add helpers that respect
										// sneaking but still show position
			return;

		for (InteractionHand hand : InteractionHand.values()) {

			ItemStack heldItem = mc.player.getItemInHand(hand);
			List<IPlacementHelper> filteredForHeldItem = helpers.stream()
				.filter(helper -> helper.matchesItem(heldItem))
				.collect(Collectors.toList());
			if (filteredForHeldItem.isEmpty())
				continue;

			BlockPos pos = ray.getBlockPos();
			BlockState state = world.getBlockState(pos);

			List<IPlacementHelper> filteredForState = filteredForHeldItem.stream()
				.filter(helper -> helper.matchesState(state))
				.collect(Collectors.toList());
			if (filteredForState.isEmpty())
				continue;

			boolean atLeastOneMatch = false;
			for (IPlacementHelper h : filteredForState) {
				PlacementOffset offset = h.getOffset(mc.player, world, state, pos, ray, heldItem);

				if (offset.isSuccessful()) {
					h.renderAt(pos, state, ray, offset);
					setTarget(offset.getBlockPos());
					atLeastOneMatch = true;
					break;
				}

			}

			// at least one helper activated, no need to check the offhand if we are still
			// in the mainhand
			if (atLeastOneMatch)
				return;

		}
	}

	static void setTarget(BlockPos target) {
		PlacementHelpers.target = target;

		if (target == null)
			return;

		if (lastTarget == null) {
			lastTarget = target;
			return;
		}

		if (!lastTarget.equals(target))
			lastTarget = target;
	}

	@SubscribeEvent
	@OnlyIn(Dist.CLIENT)
	public static void afterRenderOverlayLayer(RenderGuiOverlayEvent.Post event) {
		if (event.getOverlay() != VanillaGuiOverlay.CROSSHAIR.type())
			return;

		Minecraft mc = Minecraft.getInstance();
		Player player = mc.player;

		if (player != null && animationTick > 0) {
			Window res = event.getWindow();

			float screenY = res.getGuiScaledHeight() / 2f;
			float screenX = res.getGuiScaledWidth() / 2f;
			float progress = getCurrentAlpha();

			drawDirectionIndicator(event.getPoseStack(), event.getPartialTick(), screenX, screenY, progress);
		}
	}

	public static float getCurrentAlpha() {
		return Math.min(animationTick / 10f/* + event.getPartialTicks() */, 1f);
	}

	@OnlyIn(Dist.CLIENT)
	private static void drawDirectionIndicator(PoseStack ms, float partialTicks, float centerX, float centerY,
		float progress) {
		float r = .8f;
		float g = .8f;
		float b = .8f;
		float a = progress * progress;

		Vec3 projTarget = VecHelper.projectToPlayerView(VecHelper.getCenterOf(lastTarget), partialTicks);

		Vec3 target = new Vec3(projTarget.x, projTarget.y, 0);
		if (projTarget.z > 0)
			target = target.reverse();

		Vec3 norm = target.normalize();
		Vec3 ref = new Vec3(0, 1, 0);
		float targetAngle = AngleHelper.deg(Math.acos(norm.dot(ref)));

		if (norm.x < 0)
			targetAngle = 360 - targetAngle;

		if (animationTick < 10)
			angle.setValue(targetAngle);

		angle.chase(targetAngle, .25f, Chaser.EXP);
		angle.tickChaser();

		float snapSize = 22.5f;
		float snappedAngle = (snapSize * Math.round(angle.getValue(0f) / snapSize)) % 360f;

		float length = 10;

		CClient.PlacementIndicatorSetting mode = AllConfigs.client().placementIndicator.get();
		if (mode == CClient.PlacementIndicatorSetting.TRIANGLE)
			fadedArrow(ms, centerX, centerY, r, g, b, a, length, snappedAngle);
		else if (mode == CClient.PlacementIndicatorSetting.TEXTURE)
			textured(ms, centerX, centerY, a, snappedAngle);
	}

	private static void fadedArrow(PoseStack ms, float centerX, float centerY, float r, float g, float b, float a,
		float length, float snappedAngle) {
		RenderSystem.disableTexture();
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		RenderSystem.setShader(GameRenderer::getPositionColorShader);

		ms.pushPose();
		ms.translate(centerX, centerY, 5);
		ms.mulPose(Vector3f.ZP.rotationDegrees(angle.getValue(0)));
		// RenderSystem.rotatef(snappedAngle, 0, 0, 1);
		double scale = AllConfigs.client().indicatorScale.get();
		ms.scale((float) scale, (float) scale, 1);

		Tesselator tessellator = Tesselator.getInstance();
		BufferBuilder bufferbuilder = tessellator.getBuilder();
		bufferbuilder.begin(VertexFormat.Mode.TRIANGLE_FAN, DefaultVertexFormat.POSITION_COLOR);

		Matrix4f mat = ms.last()
			.pose();

		bufferbuilder.vertex(mat, 0, -(10 + length), 0)
			.color(r, g, b, a)
			.endVertex();

		bufferbuilder.vertex(mat, -9, -3, 0)
			.color(r, g, b, 0f)
			.endVertex();
		bufferbuilder.vertex(mat, -6, -6, 0)
			.color(r, g, b, 0f)
			.endVertex();
		bufferbuilder.vertex(mat, -3, -8, 0)
			.color(r, g, b, 0f)
			.endVertex();
		bufferbuilder.vertex(mat, 0, -8.5f, 0)
			.color(r, g, b, 0f)
			.endVertex();
		bufferbuilder.vertex(mat, 3, -8, 0)
			.color(r, g, b, 0f)
			.endVertex();
		bufferbuilder.vertex(mat, 6, -6, 0)
			.color(r, g, b, 0f)
			.endVertex();
		bufferbuilder.vertex(mat, 9, -3, 0)
			.color(r, g, b, 0f)
			.endVertex();

		tessellator.end();
		RenderSystem.disableBlend();
		RenderSystem.enableTexture();
		ms.popPose();
	}

	@OnlyIn(Dist.CLIENT)
	public static void textured(PoseStack ms, float centerX, float centerY, float alpha, float snappedAngle) {
		RenderSystem.enableTexture();
		AllGuiTextures.PLACEMENT_INDICATOR_SHEET.bind();
		RenderSystem.enableDepthTest();
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		RenderSystem.setShader(GameRenderer::getPositionColorTexShader);

		ms.pushPose();
		ms.translate(centerX, centerY, 50);
		float scale = AllConfigs.client().indicatorScale.get()
			.floatValue() * .75f;
		ms.scale(scale, scale, 1);
		ms.scale(12, 12, 1);

		float index = snappedAngle / 22.5f;
		float tex_size = 16f / 256f;

		float tx = 0;
		float ty = index * tex_size;
		float tw = 1f;
		float th = tex_size;

		Tesselator tessellator = Tesselator.getInstance();
		BufferBuilder buffer = tessellator.getBuilder();
		buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR_TEX);

		Matrix4f mat = ms.last()
			.pose();
		buffer.vertex(mat, -1, -1, 0)
			.color(1f, 1f, 1f, alpha)
			.uv(tx, ty)
			.endVertex();
		buffer.vertex(mat, -1, 1, 0)
			.color(1f, 1f, 1f, alpha)
			.uv(tx, ty + th)
			.endVertex();
		buffer.vertex(mat, 1, 1, 0)
			.color(1f, 1f, 1f, alpha)
			.uv(tx + tw, ty + th)
			.endVertex();
		buffer.vertex(mat, 1, -1, 0)
			.color(1f, 1f, 1f, alpha)
			.uv(tx + tw, ty)
			.endVertex();

		tessellator.end();

		RenderSystem.disableBlend();
		ms.popPose();
	}

}
