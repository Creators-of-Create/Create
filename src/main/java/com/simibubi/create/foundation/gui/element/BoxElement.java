package com.simibubi.create.foundation.gui.element;

import org.joml.Matrix4f;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.simibubi.create.foundation.utility.Color;
import com.simibubi.create.foundation.utility.Couple;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;

public class BoxElement extends RenderElement {

	protected Color background = new Color(0xff000000, true);
	protected Color borderTop = new Color(0x40ffeedd, true);
	protected Color borderBot = new Color(0x20ffeedd, true);
	protected int borderOffset = 2;

	public <T extends BoxElement> T withBackground(Color color) {
		this.background = color;
		//noinspection unchecked
		return (T) this;
	}

	public <T extends BoxElement> T withBackground(int color) {
		return withBackground(new Color(color, true));
	}

	public <T extends BoxElement> T flatBorder(Color color) {
		this.borderTop = color;
		this.borderBot = color;
		//noinspection unchecked
		return (T) this;
	}

	public <T extends BoxElement> T flatBorder(int color) {
		return flatBorder(new Color(color, true));
	}

	public <T extends BoxElement> T gradientBorder(Couple<Color> colors) {
		this.borderTop = colors.getFirst();
		this.borderBot = colors.getSecond();
		//noinspection unchecked
		return (T) this;
	}

	public <T extends BoxElement> T gradientBorder(Color top, Color bot) {
		this.borderTop = top;
		this.borderBot = bot;
		//noinspection unchecked
		return (T) this;
	}

	public <T extends BoxElement> T gradientBorder(int top, int bot) {
		return gradientBorder(new Color(top, true), new Color(bot, true));
	}

	public <T extends BoxElement> T withBorderOffset(int offset) {
		this.borderOffset = offset;
		//noinspection unchecked
		return (T) this;
	}

	@Override
	public void render(GuiGraphics graphics) {
		renderBox(graphics.pose());
	}

	//total box width = 1 * 2 (outer border) + 1 * 2 (inner color border) + 2 * borderOffset + width
	//defaults to 2 + 2 + 4 + 16 = 24px
	//batch everything together to save a bunch of gl calls over ScreenUtils
	protected void renderBox(PoseStack ms) {
		/*
		*          _____________
		*        _|_____________|_
		*       | | ___________ | |
		*       | | |  |      | | |
		*       | | |  |      | | |
		*       | | |--*   |  | | |
		*       | | |      h  | | |
		*       | | |  --w-+  | | |
		*       | | |         | | |
		*       | | |_________| | |
		*       |_|_____________|_|
		*         |_____________|
		*
		* */
//		RenderSystem.disableTexture();
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		RenderSystem.setShader(GameRenderer::getPositionColorShader);

		Matrix4f model = ms.last().pose();
		int f = borderOffset;
		Color c1 = background.copy().scaleAlpha(alpha);
		Color c2 = borderTop.copy().scaleAlpha(alpha);
		Color c3 = borderBot.copy().scaleAlpha(alpha);
		Tesselator tessellator = Tesselator.getInstance();
		BufferBuilder b = tessellator.getBuilder();

		b.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
		//outer top
		b.vertex(model, x - f - 1        , y - f - 2         , z).color(c1.getRed(), c1.getGreen(), c1.getBlue(), c1.getAlpha()).endVertex();
		b.vertex(model, x - f - 1        , y - f - 1         , z).color(c1.getRed(), c1.getGreen(), c1.getBlue(), c1.getAlpha()).endVertex();
		b.vertex(model, x + f + 1 + width, y - f - 1         , z).color(c1.getRed(), c1.getGreen(), c1.getBlue(), c1.getAlpha()).endVertex();
		b.vertex(model, x + f + 1 + width, y - f - 2         , z).color(c1.getRed(), c1.getGreen(), c1.getBlue(), c1.getAlpha()).endVertex();
		//outer left
		b.vertex(model, x - f - 2        , y - f - 1         , z).color(c1.getRed(), c1.getGreen(), c1.getBlue(), c1.getAlpha()).endVertex();
		b.vertex(model, x - f - 2        , y + f + 1 + height, z).color(c1.getRed(), c1.getGreen(), c1.getBlue(), c1.getAlpha()).endVertex();
		b.vertex(model, x - f - 1        , y + f + 1 + height, z).color(c1.getRed(), c1.getGreen(), c1.getBlue(), c1.getAlpha()).endVertex();
		b.vertex(model, x - f - 1        , y - f - 1         , z).color(c1.getRed(), c1.getGreen(), c1.getBlue(), c1.getAlpha()).endVertex();
		//outer bottom
		b.vertex(model, x - f - 1        , y + f + 1 + height, z).color(c1.getRed(), c1.getGreen(), c1.getBlue(), c1.getAlpha()).endVertex();
		b.vertex(model, x - f - 1        , y + f + 2 + height, z).color(c1.getRed(), c1.getGreen(), c1.getBlue(), c1.getAlpha()).endVertex();
		b.vertex(model, x + f + 1 + width, y + f + 2 + height, z).color(c1.getRed(), c1.getGreen(), c1.getBlue(), c1.getAlpha()).endVertex();
		b.vertex(model, x + f + 1 + width, y + f + 1 + height, z).color(c1.getRed(), c1.getGreen(), c1.getBlue(), c1.getAlpha()).endVertex();
		//outer right
		b.vertex(model, x + f + 1 + width, y - f - 1         , z).color(c1.getRed(), c1.getGreen(), c1.getBlue(), c1.getAlpha()).endVertex();
		b.vertex(model, x + f + 1 + width, y + f + 1 + height, z).color(c1.getRed(), c1.getGreen(), c1.getBlue(), c1.getAlpha()).endVertex();
		b.vertex(model, x + f + 2 + width, y + f + 1 + height, z).color(c1.getRed(), c1.getGreen(), c1.getBlue(), c1.getAlpha()).endVertex();
		b.vertex(model, x + f + 2 + width, y - f - 1         , z).color(c1.getRed(), c1.getGreen(), c1.getBlue(), c1.getAlpha()).endVertex();
		//inner background - also render behind the inner edges
		b.vertex(model, x - f - 1        , y - f - 1         , z).color(c1.getRed(), c1.getGreen(), c1.getBlue(), c1.getAlpha()).endVertex();
		b.vertex(model, x - f - 1        , y + f + 1 + height, z).color(c1.getRed(), c1.getGreen(), c1.getBlue(), c1.getAlpha()).endVertex();
		b.vertex(model, x + f + 1 + width, y + f + 1 + height, z).color(c1.getRed(), c1.getGreen(), c1.getBlue(), c1.getAlpha()).endVertex();
		b.vertex(model, x + f + 1 + width, y - f - 1         , z).color(c1.getRed(), c1.getGreen(), c1.getBlue(), c1.getAlpha()).endVertex();
		tessellator.end();
		b.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
		//inner top - includes corners
		b.vertex(model, x - f - 1        , y - f - 1         , z).color(c2.getRed(), c2.getGreen(), c2.getBlue(), c2.getAlpha()).endVertex();
		b.vertex(model, x - f - 1        , y - f             , z).color(c2.getRed(), c2.getGreen(), c2.getBlue(), c2.getAlpha()).endVertex();
		b.vertex(model, x + f + 1 + width, y - f             , z).color(c2.getRed(), c2.getGreen(), c2.getBlue(), c2.getAlpha()).endVertex();
		b.vertex(model, x + f + 1 + width, y - f - 1         , z).color(c2.getRed(), c2.getGreen(), c2.getBlue(), c2.getAlpha()).endVertex();
		//inner left - excludes corners
		b.vertex(model, x - f - 1        , y - f             , z).color(c2.getRed(), c2.getGreen(), c2.getBlue(), c2.getAlpha()).endVertex();
		b.vertex(model, x - f - 1        , y + f     + height, z).color(c3.getRed(), c3.getGreen(), c3.getBlue(), c3.getAlpha()).endVertex();
		b.vertex(model, x - f            , y + f     + height, z).color(c3.getRed(), c3.getGreen(), c3.getBlue(), c3.getAlpha()).endVertex();
		b.vertex(model, x - f            , y - f             , z).color(c2.getRed(), c2.getGreen(), c2.getBlue(), c2.getAlpha()).endVertex();
		//inner bottom - includes corners
		b.vertex(model, x - f - 1        , y + f     + height, z).color(c3.getRed(), c3.getGreen(), c3.getBlue(), c3.getAlpha()).endVertex();
		b.vertex(model, x - f - 1        , y + f + 1 + height, z).color(c3.getRed(), c3.getGreen(), c3.getBlue(), c3.getAlpha()).endVertex();
		b.vertex(model, x + f + 1 + width, y + f + 1 + height, z).color(c3.getRed(), c3.getGreen(), c3.getBlue(), c3.getAlpha()).endVertex();
		b.vertex(model, x + f + 1 + width, y + f     + height, z).color(c3.getRed(), c3.getGreen(), c3.getBlue(), c3.getAlpha()).endVertex();
		//inner right - excludes corners
		b.vertex(model, x + f     + width, y - f             , z).color(c2.getRed(), c2.getGreen(), c2.getBlue(), c2.getAlpha()).endVertex();
		b.vertex(model, x + f     + width, y + f     + height, z).color(c3.getRed(), c3.getGreen(), c3.getBlue(), c3.getAlpha()).endVertex();
		b.vertex(model, x + f + 1 + width, y + f     + height, z).color(c3.getRed(), c3.getGreen(), c3.getBlue(), c3.getAlpha()).endVertex();
		b.vertex(model, x + f + 1 + width, y - f             , z).color(c2.getRed(), c2.getGreen(), c2.getBlue(), c2.getAlpha()).endVertex();

		tessellator.end();

		RenderSystem.disableBlend();
//		RenderSystem.enableTexture();
	}
}
