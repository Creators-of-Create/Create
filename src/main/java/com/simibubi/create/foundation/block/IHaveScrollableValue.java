package com.simibubi.create.foundation.block;

import com.mojang.blaze3d.platform.GlStateManager;
import com.simibubi.create.AllItems;
import com.simibubi.create.foundation.utility.Lang;
import com.simibubi.create.foundation.utility.TessellatorHelper;
import com.simibubi.create.foundation.utility.VecHelper;

import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.IWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.DrawBlockHighlightEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber(value = Dist.CLIENT)
public interface IHaveScrollableValue {

	public static final AxisAlignedBB VALUE_BB = new AxisAlignedBB(0, 0, 0, 2 / 16f, 6 / 16f, 6 / 16f);

	public int getCurrentValue(BlockState state, IWorld world, BlockPos pos);

	public void onScroll(BlockState state, IWorld world, BlockPos pos, double delta);

	public String getValueName(BlockState state, IWorld world, BlockPos pos);

	public Vec3d getValueBoxPosition(BlockState state, IWorld world, BlockPos pos);

	public Direction getValueBoxDirection(BlockState state, IWorld world, BlockPos pos);

	public default boolean isValueOnMultipleFaces() {
		return false;
	}

	public default boolean requiresWrench() {
		return false;
	}

	public default boolean isValueOnFace(Direction face) {
		return true;
	}

	public default String getValueSuffix(BlockState state, IWorld world, BlockPos pos) {
		return "";
	}

	@SubscribeEvent
	@OnlyIn(Dist.CLIENT)
	public static void onDrawBlockHighlight(DrawBlockHighlightEvent event) {
		if (event.getTarget() == null || !(event.getTarget() instanceof BlockRayTraceResult))
			return;

		BlockRayTraceResult result = (BlockRayTraceResult) event.getTarget();
		Minecraft mc = Minecraft.getInstance();
		ClientWorld world = mc.world;
		BlockPos blockPos = result.getPos();
		BlockState state = world.getBlockState(blockPos);

		if (!(state.getBlock() instanceof IHaveScrollableValue))
			return;
		if (!mc.player.isAllowEdit())
			return;

		IHaveScrollableValue block = (IHaveScrollableValue) state.getBlock();
		Vec3d pos = new Vec3d(blockPos);

		if (block.requiresWrench() && !AllItems.WRENCH.typeOf(mc.player.getHeldItemMainhand()))
			return;

		Vec3d valueBoxPosition = block.getValueBoxPosition(state, world, blockPos);
		AxisAlignedBB bb = VALUE_BB.offset(valueBoxPosition);
		bb = bb.grow(1 / 128f);
		Direction facing = block.isValueOnMultipleFaces() ? result.getFace()
				: block.getValueBoxDirection(state, world, blockPos);
		if (block.isValueOnMultipleFaces() && !block.isValueOnFace(result.getFace()))
			return;

		Vec3d cursor = result.getHitVec().subtract(VecHelper.getCenterOf(blockPos));
		cursor = VecHelper.rotate(cursor, facing.getHorizontalAngle() + 90, Axis.Y);
		cursor = VecHelper.rotate(cursor, facing == Direction.UP ? -90 : facing == Direction.DOWN ? 90 : 0, Axis.Z)
				.add(.5, .5, .5);
		boolean contains = bb.contains(cursor);

		TessellatorHelper.prepareForDrawing();
		GlStateManager.enableBlend();
		GlStateManager.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA,
				GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE,
				GlStateManager.DestFactor.ZERO);
		GlStateManager.disableTexture();
		GlStateManager.depthMask(false);

		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder bufferbuilder = tessellator.getBuffer();
		bufferbuilder.begin(3, DefaultVertexFormats.POSITION_COLOR);

		GlStateManager.translated(pos.x, pos.y, pos.z);
		GlStateManager.translated(.5f, .5f, .5f);
		GlStateManager.rotated(-facing.getHorizontalAngle() - 90, 0, 1, 0);
		GlStateManager.rotated(facing == Direction.UP ? 90 : facing == Direction.DOWN ? -90 : 0, 0, 0, 1);
		GlStateManager.translated(-.5f, -.5f, -.5f);

		GlStateManager.pushMatrix();

		if (contains) {
			GlStateManager.lineWidth(2);
			WorldRenderer.drawBoundingBox(bufferbuilder, bb.minX, bb.minY, bb.minZ, bb.maxX, bb.maxY, bb.maxZ, 1, .5f,
					.75f, 1f);
		} else {
			GlStateManager.lineWidth(2);
			WorldRenderer.drawBoundingBox(bufferbuilder, bb.minX, bb.minY, bb.minZ, bb.maxX, bb.maxY, bb.maxZ, .5f,
					.25f, .35f, 1f);
		}

		tessellator.draw();
		GlStateManager.popMatrix();

		GlStateManager.enableTexture();
		GlStateManager.depthMask(true);

		float textScale = 1 / 128f;

		GlStateManager.rotated(90, 0, 1, 0);
		GlStateManager.translated(1 - valueBoxPosition.z - bb.getZSize(), valueBoxPosition.y + bb.getYSize(),
				valueBoxPosition.x);
		GlStateManager.translated(-1, 0, 3 / 32f);

		if (contains) {
			GlStateManager.pushMatrix();
			GlStateManager.translated(bb.getZSize() + 1 / 32f, -1 / 16f, 0);
			GlStateManager.scaled(textScale, -textScale, textScale);

			String text = block.getValueName(state, world, blockPos);
			mc.fontRenderer.drawString(text, 0, 0, 0xFF88BB);
			GlStateManager.translated(0, 0, -1 / 4f);
			mc.fontRenderer.drawString(text, 1, 1, 0x224433);
			GlStateManager.translated(0, 0, 1 / 4f);

			text = TextFormatting.ITALIC + "<" + Lang.translate("action.scroll") + ">";
			mc.fontRenderer.drawString(text, 0, 10, 0xCCBBCC);
			GlStateManager.translated(0, 0, -1 / 4f);
			mc.fontRenderer.drawString(text, 1, 11, 0x111111);
			GlStateManager.translated(0, 0, 1 / 4f);

			GlStateManager.popMatrix();
		}

		String numberText = block.getCurrentValue(state, world, blockPos)
				+ block.getValueSuffix(state, world, blockPos);
		int stringWidth = mc.fontRenderer.getStringWidth(numberText);
		float numberScale = 4 / 128f * ((float) mc.fontRenderer.FONT_HEIGHT / stringWidth);
		boolean singleDigit = stringWidth < 10;
		if (singleDigit)
			numberScale = numberScale / 2;
		GlStateManager.translated(4 / 64f, -5 / 64f, 0);

		GlStateManager.scaled(numberScale, -numberScale, numberScale);
		float verticalMargin = (stringWidth - mc.fontRenderer.FONT_HEIGHT) / 2f;
		GlStateManager.translated(singleDigit ? stringWidth / 2 : 0, singleDigit ? -verticalMargin : verticalMargin, 0);

		mc.fontRenderer.drawString(numberText, 0, 0, 0xFFFFFF);
		GlStateManager.translated(0, 0, -1 / 4f);
		mc.fontRenderer.drawString(numberText, 1, 1, 0x224433);

		GlStateManager.disableBlend();

		GlStateManager.lineWidth(1);
		TessellatorHelper.cleanUpAfterDrawing();
	}

	public static boolean onScroll(double delta) {
		RayTraceResult objectMouseOver = Minecraft.getInstance().objectMouseOver;
		if (!(objectMouseOver instanceof BlockRayTraceResult))
			return false;

		BlockRayTraceResult result = (BlockRayTraceResult) objectMouseOver;
		Minecraft mc = Minecraft.getInstance();
		ClientWorld world = mc.world;
		BlockPos blockPos = result.getPos();
		BlockState state = world.getBlockState(blockPos);

		if (!(state.getBlock() instanceof IHaveScrollableValue))
			return false;
		if (!mc.player.isAllowEdit())
			return false;

		IHaveScrollableValue block = (IHaveScrollableValue) state.getBlock();

		if (block.requiresWrench() && !AllItems.WRENCH.typeOf(mc.player.getHeldItemMainhand()))
			return false;

		Vec3d valueBoxPosition = block.getValueBoxPosition(state, world, blockPos);
		AxisAlignedBB bb = VALUE_BB.offset(valueBoxPosition);
		bb = bb.grow(1 / 128f);
		Direction facing = block.isValueOnMultipleFaces() ? result.getFace()
				: block.getValueBoxDirection(state, world, blockPos);

		Vec3d cursor = result.getHitVec().subtract(VecHelper.getCenterOf(blockPos));
		cursor = VecHelper.rotate(cursor, facing.getHorizontalAngle() + 90, Axis.Y);
		cursor = VecHelper.rotate(cursor, facing == Direction.UP ? -90 : facing == Direction.DOWN ? 90 : 0, Axis.Z)
				.add(.5, .5, .5);
		if (!bb.contains(cursor))
			return false;

		block.onScroll(state, world, blockPos, delta);
		return true;
	}

}
