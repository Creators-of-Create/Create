package com.simibubi.create.foundation.behaviour;

import com.mojang.blaze3d.platform.GlStateManager;
import com.simibubi.create.foundation.utility.ColorHelper;
import com.simibubi.create.foundation.utility.TessellatorHelper;
import com.simibubi.create.modules.contraptions.relays.elementary.ShaftBlock;
import com.simibubi.create.modules.logistics.item.filter.FilterItem;

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
import net.minecraft.tags.BlockTags;
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
		GlStateManager.lineWidth(1);

		TessellatorHelper.draw();
		GlStateManager.enableTexture();

		float fontScale = -1 / 64f;
		Vec3d shift = box.labelOffset;
		FontRenderer font = Minecraft.getInstance().fontRenderer;
		GlStateManager.scaled(fontScale, fontScale, fontScale);

		if (highlighted) {
			GlStateManager.pushMatrix();
			GlStateManager.translated(17.5f, -5f, 7f);
			GlStateManager.translated(shift.x, shift.y, shift.z);
			renderText(box, font, box.label);
			if (!box.sublabel.isEmpty()) {
				GlStateManager.translated(0, 10, 0);
				renderText(box, font, box.sublabel);
			}
			if (!box.scrollTooltip.isEmpty()) {
				GlStateManager.translated(0, 10, 0);
				renderText(font, box.scrollTooltip, 0x998899, 0x111111);
			}
			GlStateManager.popMatrix();
		}

		box.render(highlighted);
	}

	public static void renderText(ValueBox box, FontRenderer font, String text) {
		renderText(font, text, box.highlightColor, ColorHelper.mixColors(box.passiveColor, 0, 0.75f));
	}

	public static void renderText(FontRenderer font, String text, int color, int shadowColor) {
		font.drawString(text, 0, 0, color);
		GlStateManager.translated(0, 0, -1 / 4f);
		font.drawString(text, 1, 1, shadowColor);
		GlStateManager.translated(0, 0, 1 / 4f);
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
		if (item instanceof FilterItem)
			return NUDGE;
		if (item instanceof BlockItem) {
			Block block = ((BlockItem) item).getBlock();
			if (block instanceof ShaftBlock)
				return NUDGE;
			if (block instanceof FenceBlock)
				return NUDGE;
			if (block.isIn(BlockTags.BUTTONS))
				return NUDGE;
			if (block == Blocks.END_ROD)
				return NUDGE;
		}
		return 0;
	}

}
