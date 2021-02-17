package com.simibubi.create.foundation.utility.placement;

import com.mojang.blaze3d.systems.RenderSystem;
import com.simibubi.create.foundation.config.AllConfigs;
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
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
		ClientWorld world = mc.world;

		if (world == null)
			return;

		if (!(mc.objectMouseOver instanceof BlockRayTraceResult))
			return;

		BlockRayTraceResult ray = (BlockRayTraceResult) mc.objectMouseOver;

		if (mc.player == null)
			return;

		if (mc.player.isSneaking())//for now, disable all helpers when sneaking TODO add helpers that respect sneaking but still show position
			return;

		for (Hand hand : Hand.values()) {

			ItemStack heldItem = mc.player.getHeldItem(hand);
			List<IPlacementHelper> filteredForHeldItem = helpers.stream().filter(helper -> helper.matchesItem(heldItem)).collect(Collectors.toList());
			if (filteredForHeldItem.isEmpty())
				continue;

			BlockPos pos = ray.getPos();
			BlockState state = world.getBlockState(pos);

			List<IPlacementHelper> filteredForState = filteredForHeldItem.stream().filter(helper -> helper.matchesState(state)).collect(Collectors.toList());
			if (filteredForState.isEmpty())
				continue;

			boolean atLeastOneMatch = false;
			for (IPlacementHelper h : filteredForState) {
				PlacementOffset offset = h.getOffset(world, state, pos, ray, heldItem);

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
			//MatrixStack matrix = event.getMatrix();
			//String text = "(  )";

			//matrix.push();
			//matrix.translate(res.getScaledWidth() / 2F, res.getScaledHeight() / 2f - 4, 0);
			float screenY = res.getScaledHeight() / 2f;
			float screenX = res.getScaledWidth() / 2f;
			//float y = screenY - 3.5f;
			//float x = screenX;
			//x -= mc.fontRenderer.getStringWidth(text)/2f - 0.25f;

			float progress = Math.min(animationTick / 10f/* + event.getPartialTicks()*/, 1f);
			//int opacity = ((int) (255 * (progress * progress))) << 24;

			//mc.fontRenderer.drawString(text, x, y, 0xFFFFFF | opacity);

			drawDirectionIndicator(event.getPartialTicks(), screenX, screenY, progress);
			//matrix.pop();
		}
	}

	@OnlyIn(Dist.CLIENT)
	private static void drawDirectionIndicator(float partialTicks, float centerX, float centerY, float progress) {
		float r = .8f;
		float g = .8f;
		float b = .8f;
		float a = progress * progress;

		Vec3d projTarget = VecHelper.projectToPlayerView(VecHelper.getCenterOf(lastTarget), partialTicks);

		Vec3d target = new Vec3d(projTarget.x, projTarget.y, 0);
		if (projTarget.z > 0) {
			target = target.inverse();
		}

		Vec3d norm = target.normalize();
		Vec3d ref = new Vec3d(0, 1, 0);
		float targetAngle = AngleHelper.deg(Math.acos(norm.dotProduct(ref)));

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
		//TOD O if the target is off screen, use length to show a meaningful distance

		boolean flag = AllConfigs.CLIENT.smoothPlacementIndicator.get();
		if (flag)
			fadedArrow(centerX, centerY, r, g, b, a, length, snappedAngle);

		else
			textured(centerX, centerY, a, snappedAngle);
	}

	private static void fadedArrow(float centerX, float centerY, float r, float g, float b, float a, float length, float snappedAngle) {
		RenderSystem.pushMatrix();
		RenderSystem.disableTexture();
		RenderSystem.enableBlend();
		RenderSystem.disableAlphaTest();
		RenderSystem.defaultBlendFunc();
		RenderSystem.shadeModel(GL11.GL_SMOOTH);

		RenderSystem.translated(centerX, centerY, 0);
		RenderSystem.rotatef(angle.get(0), 0, 0, 1);
		//RenderSystem.rotatef(snappedAngle, 0, 0, 1);

		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder bufferbuilder = tessellator.getBuffer();
		bufferbuilder.begin(GL11.GL_POLYGON, DefaultVertexFormats.POSITION_COLOR);

		bufferbuilder.vertex(0, - (10 + length), 0).color(r, g, b, a).endVertex();

		bufferbuilder.vertex(-9, -3, 0).color(r, g, b, 0f).endVertex();
		bufferbuilder.vertex(-6, -6, 0).color(r, g, b, 0f).endVertex();
		bufferbuilder.vertex(-3, -8, 0).color(r, g, b, 0f).endVertex();
		bufferbuilder.vertex(0, -8.5, 0).color(r, g, b, 0f).endVertex();
		bufferbuilder.vertex(3, -8, 0).color(r, g, b, 0f).endVertex();
		bufferbuilder.vertex(6, -6, 0).color(r, g, b, 0f).endVertex();
		bufferbuilder.vertex(9, -3, 0).color(r, g, b, 0f).endVertex();

		tessellator.draw();
		RenderSystem.shadeModel(GL11.GL_FLAT);
		RenderSystem.disableBlend();
		RenderSystem.enableAlphaTest();
		RenderSystem.enableTexture();
		RenderSystem.popMatrix();
	}

	private static void textured(float centerX, float centerY, float alpha, float snappedAngle) {
		RenderSystem.pushMatrix();
		RenderSystem.enableTexture();
		//RenderSystem.disableTexture();
		AllGuiTextures.PLACEMENT_INDICATOR_SHEET.bind();
		RenderSystem.enableBlend();
		//RenderSystem.disableAlphaTest();
		RenderSystem.enableAlphaTest();
		RenderSystem.defaultBlendFunc();
		RenderSystem.color4f(1f, 1f, 1f, 1f);
		RenderSystem.shadeModel(GL11.GL_SMOOTH);

		RenderSystem.translated(centerX, centerY, 0);
		//RenderSystem.rotatef(angle.get(0.1f), 0, 0, -1);
		//RenderSystem.translated(0, 10, 0);
		//RenderSystem.rotatef(angle.get(0.1f), 0, 0, 1);
		RenderSystem.scaled(12, 12, 0);

		float index = snappedAngle / 22.5f;
		float tex_size = 16f/256f;

		float tx = 0;
		float ty = index * tex_size;
		float tw = 1f;
		float th = tex_size;

		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder buffer = tessellator.getBuffer();
		buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR_TEXTURE);


		buffer.vertex(-1, -1, 0).color(1f, 1f, 1f, alpha).texture(tx, ty).endVertex();
		buffer.vertex(-1, 1, 0).color(1f, 1f, 1f, alpha).texture(tx, ty + th).endVertex();
		buffer.vertex(1, 1, 0).color(1f, 1f, 1f, alpha).texture(tx + tw, ty + th).endVertex();
		buffer.vertex(1, -1, 0).color(1f, 1f, 1f, alpha).texture(tx + tw, ty).endVertex();

		tessellator.draw();
		RenderSystem.shadeModel(GL11.GL_FLAT);

		//RenderSystem.enableTexture();

		RenderSystem.disableBlend();
		//RenderSystem.enableAlphaTest();
		RenderSystem.popMatrix();
	}

}
