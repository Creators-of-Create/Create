package com.simibubi.create.foundation.behaviour;

import com.mojang.blaze3d.platform.GlStateManager;
import com.simibubi.create.AllItems;
import com.simibubi.create.foundation.behaviour.ValueBox.ItemValueBox;
import com.simibubi.create.foundation.utility.ColorHelper;
import com.simibubi.create.foundation.utility.TessellatorHelper;
import com.simibubi.create.modules.contraptions.relays.elementary.CogWheelBlock;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.FenceBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;

@SuppressWarnings("deprecation")
public class ValueBoxRenderer {

	public static void renderBox(ValueBox box, boolean highlighted) {
		GlStateManager.enableBlend();
		GlStateManager.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA,
				GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE,
				GlStateManager.DestFactor.ZERO);
		GlStateManager.disableTexture();

		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder bufferbuilder = tessellator.getBuffer();
		bufferbuilder.begin(3, DefaultVertexFormats.POSITION_COLOR);

		GlStateManager.lineWidth(highlighted ? 3 : 2);
		Vec3d color = highlighted ? ColorHelper.getRGB(box.highlightColor) : ColorHelper.getRGB(box.passiveColor);
		AxisAlignedBB bb = box.bb;
		WorldRenderer.drawBoundingBox(bufferbuilder, bb.minX, bb.minY, bb.minZ, bb.maxX, bb.maxY, bb.maxZ,
				(float) color.x, (float) color.y, (float) color.z, 1f);
		GlStateManager.lineWidth(2);

		TessellatorHelper.draw();
		GlStateManager.enableTexture();

		float fontScale = -1 / 64f;
		Vec3d shift = box.labelOffset;
		GlStateManager.scaled(fontScale, fontScale, fontScale);
		GlStateManager.translated(17.5f, -5f, 7f);
		GlStateManager.translated(shift.x, shift.y, shift.z);
		GlStateManager.rotated(0, 1, 0, 0);
		FontRenderer font = Minecraft.getInstance().fontRenderer;

		if (highlighted) {
			String text = box.label;
			font.drawString(text, 0, 0, box.highlightColor);
			GlStateManager.translated(0, 0, -1 / 4f);
			font.drawString(text, 1, 1, ColorHelper.mixColors(box.passiveColor, 0, 0.75f));
			GlStateManager.translated(0, 0, 1 / 4f);
		}

		if (box instanceof ItemValueBox) {
			String count = ((ItemValueBox) box).count + "";
			GlStateManager.translated(-7 - font.getStringWidth(count), 10, 10 + 1 / 4f);
			GlStateManager.scaled(1.5, 1.5, 1.5);
			font.drawString(count, 0, 0, 0xEDEDED);
			GlStateManager.translated(0, 0, -1 / 4f);
			font.drawString(count, 1, 1, 0x4F4F4F);
		}
	}

	public static void renderItemIntoValueBox(ItemStack filter) {
		ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
		IBakedModel modelWithOverrides = itemRenderer.getModelWithOverrides(filter);
		boolean blockItem = modelWithOverrides.isGui3d();
		float scale = (!blockItem ? .5f : 1f) - 1 / 64f;
		float zOffset = (!blockItem ? -.225f : 0) + customZOffset(filter.getItem());
		GlStateManager.scaled(scale, scale, scale);
		GlStateManager.translated(0, 0, zOffset);
		itemRenderer.renderItem(filter, TransformType.FIXED);
	}

	private static float customZOffset(Item item) {
		float NUDGE = -.1f;
		if (AllItems.FILTER.get() == item)
			return NUDGE;
		if (item instanceof BlockItem) {
			Block block = ((BlockItem) item).getBlock();
			if (block instanceof CogWheelBlock)
				return NUDGE;
			if (block instanceof FenceBlock)
				return NUDGE;
			if (block == Blocks.END_ROD)
				return NUDGE;
		}
		return 0;
	}

}
