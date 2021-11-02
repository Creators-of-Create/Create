package com.simibubi.create.content.logistics.block.redstone;

import java.util.Map;
import java.util.Random;

import com.google.common.collect.ImmutableMap;
import com.jozufozu.flywheel.util.transform.MatrixTransformStack;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.foundation.tileEntity.renderer.SafeTileEntityRenderer;
import com.simibubi.create.foundation.utility.AngleHelper;
import com.simibubi.create.foundation.utility.Color;
import com.simibubi.create.foundation.utility.Couple;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.font.glyphs.BakedGlyph;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.MultiBufferSource.BufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.network.chat.Style;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.state.BlockState;

public class NixieTubeRenderer extends SafeTileEntityRenderer<NixieTubeTileEntity> {

	private Random r = new Random();

	public static final Map<DyeColor, Couple<Integer>> DYE_TABLE = new ImmutableMap.Builder<DyeColor, Couple<Integer>>()

		// DyeColor, ( Front RGB, Back RGB )
		.put(DyeColor.BLACK, Couple.create(0x45403B, 0x21201F))
		.put(DyeColor.RED, Couple.create(0xB13937, 0x632737))
		.put(DyeColor.GREEN, Couple.create(0x208A46, 0x1D6045))
		.put(DyeColor.BROWN, Couple.create(0xAC855C, 0x68533E))

		.put(DyeColor.BLUE, Couple.create(0x5391E1, 0x504B90))
		.put(DyeColor.GRAY, Couple.create(0x5D666F, 0x313538))
		.put(DyeColor.LIGHT_GRAY, Couple.create(0x95969B, 0x707070))
		.put(DyeColor.PURPLE, Couple.create(0x9F54AE, 0x63366C))

		.put(DyeColor.CYAN, Couple.create(0x3EABB4, 0x3C7872))
		.put(DyeColor.PINK, Couple.create(0xD5A8CB, 0xB86B95))
		.put(DyeColor.LIME, Couple.create(0xA3DF55, 0x4FB16F))
		.put(DyeColor.YELLOW, Couple.create(0xE6D756, 0xE9AC29))

		.put(DyeColor.LIGHT_BLUE, Couple.create(0x69CED2, 0x508AA5))
		.put(DyeColor.ORANGE, Couple.create(0xEE9246, 0xD94927))
		.put(DyeColor.MAGENTA, Couple.create(0xF062B0, 0xC04488))
		.put(DyeColor.WHITE, Couple.create(0xEDEAE5, 0xBBB6B0))

		.build();

	public NixieTubeRenderer(BlockEntityRendererProvider.Context context) {
	}

	@Override
	protected void renderSafe(NixieTubeTileEntity te, float partialTicks, PoseStack ms, MultiBufferSource buffer,
		int light, int overlay) {
		ms.pushPose();
		BlockState blockState = te.getBlockState();
		MatrixTransformStack.of(ms)
			.centre()
			.rotateY(AngleHelper.horizontalAngle(blockState.getValue(NixieTubeBlock.FACING)));

		float height = blockState.getValue(NixieTubeBlock.CEILING) ? 2 : 6;
		float scale = 1 / 20f;

		Couple<String> s = te.getDisplayedStrings();
		DyeColor color = NixieTubeBlock.colorOf(te.getBlockState());

		ms.pushPose();
		ms.translate(-4 / 16f, 0, 0);
		ms.scale(scale, -scale, scale);
		drawTube(ms, buffer, s.getFirst(), height, color);
		ms.popPose();

		ms.pushPose();
		ms.translate(4 / 16f, 0, 0);
		ms.scale(scale, -scale, scale);
		drawTube(ms, buffer, s.getSecond(), height, color);
		ms.popPose();

		ms.popPose();
	}

	private void drawTube(PoseStack ms, MultiBufferSource buffer, String c, float height, DyeColor color) {
		Font fontRenderer = Minecraft.getInstance().font;
		float charWidth = fontRenderer.width(c);
		float shadowOffset = .5f;
		float flicker = r.nextFloat();
		Couple<Integer> couple = DYE_TABLE.get(color);
		int brightColor = couple.getFirst();
		int darkColor = couple.getSecond();
		int flickeringBrightColor = Color.mixColors(brightColor, darkColor, flicker / 4);

		ms.pushPose();
		ms.translate((charWidth - shadowOffset) / -2f, -height, 0);
		drawChar(ms, buffer, c, flickeringBrightColor);
		ms.pushPose();
		ms.translate(shadowOffset, shadowOffset, -1 / 16f);
		drawChar(ms, buffer, c, darkColor);
		ms.popPose();
		ms.popPose();

		ms.pushPose();
		ms.scale(-1, 1, 1);
		ms.translate((charWidth - shadowOffset) / -2f, -height, 0);
		drawChar(ms, buffer, c, darkColor);
		ms.pushPose();
		ms.translate(-shadowOffset, shadowOffset, -1 / 16f);
		drawChar(ms, buffer, c, Color.mixColors(darkColor, 0, .35f));
		ms.popPose();
		ms.popPose();
	}

	private static void drawChar(PoseStack ms, MultiBufferSource buffer, String c, int color) {
		Font fontRenderer = Minecraft.getInstance().font;
		fontRenderer.drawInBatch(c, 0, 0, color, false, ms.last()
			.pose(), buffer, false, 0, 15728880);
		if (buffer instanceof BufferSource) {
			BakedGlyph texturedglyph = fontRenderer.getFontSet(Style.DEFAULT_FONT)
				.whiteGlyph();
			((BufferSource) buffer).endBatch(texturedglyph.renderType(false));
		}
	}

}
