package com.simibubi.create.foundation.utility.placement;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.simibubi.create.foundation.config.AllConfigs;
import com.simibubi.create.foundation.config.CClient;
import com.simibubi.create.foundation.gui.AllGuiTextures;
import com.simibubi.create.foundation.gui.widgets.InterpolatedChasingAngle;
import com.simibubi.create.foundation.gui.widgets.InterpolatedChasingValue;
import com.simibubi.create.foundation.utility.AngleHelper;
import com.simibubi.create.foundation.utility.VecHelper;

import net.minecraft.block.BlockState;
import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class PlacementHelpers {

	private static final List<IPlacementHelper> helpers = new ArrayList<>();
	private static int animationTick = 0;
	private static final InterpolatedChasingValue angle = new InterpolatedChasingAngle().withSpeed(0.25f);
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
		ClientWorld world = mc.level;

		if (world == null)
			return;

		if (!(mc.hitResult instanceof BlockRayTraceResult))
			return;

		BlockRayTraceResult ray = (BlockRayTraceResult) mc.hitResult;

		if (mc.player == null)
			return;

		if (mc.player.isShiftKeyDown())//for now, disable all helpers when sneaking TODO add helpers that respect sneaking but still show position
			return;

		for (Hand hand : Hand.values()) {

			ItemStack heldItem = mc.player.getItemInHand(hand);
			List<IPlacementHelper> filteredForHeldItem = helpers.stream().filter(helper -> helper.matchesItem(heldItem)).collect(Collectors.toList());
			if (filteredForHeldItem.isEmpty())
				continue;

			BlockPos pos = ray.getBlockPos();
			BlockState state = world.getBlockState(pos);

			List<IPlacementHelper> filteredForState = filteredForHeldItem.stream().filter(helper -> helper.matchesState(state)).collect(Collectors.toList());
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

			//at least one helper activated, no need to check the offhand if we are still in the mainhand
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
	public static void onRender(RenderGameOverlayEvent.Pre event) {
		if (event.getType() != RenderGameOverlayEvent.ElementType.CROSSHAIRS)
			return;

		Minecraft mc = Minecraft.getInstance();
		PlayerEntity player = mc.player;

		if (player != null && animationTick > 0) {
			MainWindow res = event.getWindow();

			float screenY = res.getGuiScaledHeight() / 2f;
			float screenX = res.getGuiScaledWidth() / 2f;
			float progress = getCurrentAlpha();

			drawDirectionIndicator(event.getMatrixStack(), event.getPartialTicks(), screenX, screenY, progress);
		}
	}

	public static float getCurrentAlpha() {
		return Math.min(animationTick / 10f/* + event.getPartialTicks()*/, 1f);
	}

	@OnlyIn(Dist.CLIENT)
	private static void drawDirectionIndicator(MatrixStack ms, float partialTicks, float centerX, float centerY, float progress) {
		float r = .8f;
		float g = .8f;
		float b = .8f;
		float a = progress * progress;

		Vector3d projTarget = VecHelper.projectToPlayerView(VecHelper.getCenterOf(lastTarget), partialTicks);

		Vector3d target = new Vector3d(projTarget.x, projTarget.y, 0);
		if (projTarget.z > 0) {
			target = target.reverse();
		}

		Vector3d norm = target.normalize();
		Vector3d ref = new Vector3d(0, 1, 0);
		float targetAngle = AngleHelper.deg(Math.acos(norm.dot(ref)));

		angle.withSpeed(0.25f);

		if (norm.x < 0) {
			targetAngle = 360 - targetAngle;
		}

		if (animationTick < 10)
			angle.set(targetAngle);

		angle.target(targetAngle);
		angle.tick();

		float snapSize = 22.5f;
		float snappedAngle = (snapSize * Math.round(angle.get(0f) / snapSize)) % 360f;

		float length = 10;

		CClient.PlacementIndicatorSetting mode = AllConfigs.CLIENT.placementIndicator.get();
		if (mode == CClient.PlacementIndicatorSetting.TRIANGLE)
			fadedArrow(ms, centerX, centerY, r, g, b, a, length, snappedAngle);
		else if (mode == CClient.PlacementIndicatorSetting.TEXTURE)
			textured(ms, centerX, centerY, a, snappedAngle);
	}

	private static void fadedArrow(MatrixStack ms, float centerX, float centerY, float r, float g, float b, float a, float length, float snappedAngle) {
		ms.pushPose();
		RenderSystem.disableTexture();
		RenderSystem.enableBlend();
		RenderSystem.disableAlphaTest();
		RenderSystem.defaultBlendFunc();
		RenderSystem.shadeModel(GL11.GL_SMOOTH);

		ms.translate(centerX, centerY, 0);
		ms.mulPose(Vector3f.ZP.rotationDegrees(angle.get(0)));
		//RenderSystem.rotatef(snappedAngle, 0, 0, 1);
		double scale = AllConfigs.CLIENT.indicatorScale.get();
		RenderSystem.scaled(scale, scale, 1);

		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder bufferbuilder = tessellator.getBuilder();
		bufferbuilder.begin(GL11.GL_POLYGON, DefaultVertexFormats.POSITION_COLOR);

		Matrix4f mat = ms.last().pose();

		bufferbuilder.vertex(mat, 0, - (10 + length), 0).color(r, g, b, a).endVertex();

		bufferbuilder.vertex(mat, -9, -3, 0).color(r, g, b, 0f).endVertex();
		bufferbuilder.vertex(mat, -6, -6, 0).color(r, g, b, 0f).endVertex();
		bufferbuilder.vertex(mat, -3, -8, 0).color(r, g, b, 0f).endVertex();
		bufferbuilder.vertex(mat, 0, -8.5f, 0).color(r, g, b, 0f).endVertex();
		bufferbuilder.vertex(mat, 3, -8, 0).color(r, g, b, 0f).endVertex();
		bufferbuilder.vertex(mat, 6, -6, 0).color(r, g, b, 0f).endVertex();
		bufferbuilder.vertex(mat, 9, -3, 0).color(r, g, b, 0f).endVertex();

		tessellator.end();
		RenderSystem.shadeModel(GL11.GL_FLAT);
		RenderSystem.disableBlend();
		RenderSystem.enableAlphaTest();
		RenderSystem.enableTexture();
		ms.popPose();
	}

	private static void textured(MatrixStack ms, float centerX, float centerY, float alpha, float snappedAngle) {
		ms.pushPose();
		RenderSystem.enableTexture();
		AllGuiTextures.PLACEMENT_INDICATOR_SHEET.bind();
		RenderSystem.enableBlend();
		RenderSystem.enableAlphaTest();
		RenderSystem.defaultBlendFunc();
		RenderSystem.shadeModel(GL11.GL_SMOOTH);

		ms.translate(centerX, centerY, 0);
		float scale = AllConfigs.CLIENT.indicatorScale.get().floatValue() * .75f;
		ms.scale(scale, scale, 1);
		ms.scale(12, 12, 1);

		float index = snappedAngle / 22.5f;
		float tex_size = 16f/256f;

		float tx = 0;
		float ty = index * tex_size;
		float tw = 1f;
		float th = tex_size;

		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder buffer = tessellator.getBuilder();
		buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR_TEX);

		Matrix4f mat = ms.last().pose();
		buffer.vertex(mat, -1, -1, 0).color(1f, 1f, 1f, alpha).uv(tx, ty).endVertex();
		buffer.vertex(mat, -1, 1, 0).color(1f, 1f, 1f, alpha).uv(tx, ty + th).endVertex();
		buffer.vertex(mat, 1, 1, 0).color(1f, 1f, 1f, alpha).uv(tx + tw, ty + th).endVertex();
		buffer.vertex(mat, 1, -1, 0).color(1f, 1f, 1f, alpha).uv(tx + tw, ty).endVertex();

		tessellator.end();

		RenderSystem.shadeModel(GL11.GL_FLAT);
		RenderSystem.disableBlend();
		ms.popPose();
	}

}
