package com.simibubi.create.foundation.gui;

import javax.annotation.Nonnull;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import com.simibubi.create.foundation.utility.Color;
import com.simibubi.create.foundation.utility.Couple;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraftforge.fmlclient.gui.GuiUtils;

public class UIRenderHelper {

	/**
	 * An FBO that has a stencil buffer for use wherever stencil are necessary. Forcing the main FBO to have a stencil
	 * buffer will cause GL error spam when using fabulous graphics.
	 */
	public static CustomRenderTarget framebuffer;

	public static void init() {
		RenderSystem.recordRenderCall(() -> {
			Window mainWindow = Minecraft.getInstance()
					.getWindow();
			framebuffer = CustomRenderTarget.create(mainWindow);
		});
	}

	public static void updateWindowSize(Window mainWindow) {
		if (framebuffer != null)
			framebuffer.resize(mainWindow.getWidth(), mainWindow.getHeight(), Minecraft.ON_OSX);
	}

	public static void drawFramebuffer(float alpha) {
		framebuffer.blitToScreen(alpha);
	}

	public static void streak(PoseStack ms, float angle, int x, int y, int breadth, int length) {
		streak(ms, angle, x, y, breadth, length, Theme.i(Theme.Key.STREAK));
	}
	// angle in degrees; 0° -> fading to the right
	// x and y specify the middle point of the starting edge
	// breadth is the total width of the streak

	public static void streak(PoseStack ms, float angle, int x, int y, int breadth, int length, int color) {
		int a1 = 0xa0 << 24;
		int a2 = 0x80 << 24;
		int a3 = 0x10 << 24;
		int a4 = 0x00 << 24;

		color &= 0x00FFFFFF;
		int c1 = a1 | color;
		int c2 = a2 | color;
		int c3 = a3 | color;
		int c4 = a4 | color;

		ms.pushPose();
		ms.translate(x, y, 0);
		ms.mulPose(Vector3f.ZP.rotationDegrees(angle - 90));

		streak(ms, breadth / 2, length, c1, c2, c3, c4);

		ms.popPose();
	}

	public static void streak(PoseStack ms, float angle, int x, int y, int breadth, int length, Color c) {
		Color color = c.copy().setImmutable();
		int c1 = color.scaleAlpha(0.625f).getRGB();
		int c2 = color.scaleAlpha(0.5f).getRGB();
		int c3 = color.scaleAlpha(0.0625f).getRGB();
		int c4 = color.scaleAlpha(0f).getRGB();

		ms.pushPose();
		ms.translate(x, y, 0);
		ms.mulPose(Vector3f.ZP.rotationDegrees(angle - 90));

		streak(ms, breadth / 2, length, c1, c2, c3, c4);

		ms.popPose();
	}

	private static void streak(PoseStack ms, int width, int height, int c1, int c2, int c3, int c4) {
		double split1 = .5;
		double split2 = .75;
		Matrix4f model = ms.last().pose();
		GuiUtils.drawGradientRect(model, 0, -width, 0, width, (int) (split1 * height), c1, c2);
		GuiUtils.drawGradientRect(model, 0, -width, (int) (split1 * height), width, (int) (split2 * height), c2, c3);
		GuiUtils.drawGradientRect(model, 0, -width, (int) (split2 * height), width, height, c3, c4);
	}

	/**
	 * @see #angledGradient(MatrixStack, float, int, int, int, int, int, Color, Color)
	 */
	public static void angledGradient(@Nonnull PoseStack ms, float angle, int x, int y, int breadth, int length, Couple<Color> c) {
		angledGradient(ms, angle, x, y, 0, breadth, length, c);
	}

	/**
	 * @see #angledGradient(MatrixStack, float, int, int, int, int, int, Color, Color)
	 */
	public static void angledGradient(@Nonnull PoseStack ms, float angle, int x, int y, int z, int breadth, int length, Couple<Color> c) {
		angledGradient(ms, angle, x, y, z, breadth, length, c.getFirst(), c.getSecond());
	}

	/**
	 * @see #angledGradient(MatrixStack, float, int, int, int, int, int, Color, Color)
	 */
	public static void angledGradient(@Nonnull PoseStack ms, float angle, int x, int y, int breadth, int length, Color color1, Color color2) {
		angledGradient(ms, angle, x, y, 0, breadth, length, color1, color2);
	}

	/**
	 * x and y specify the middle point of the starting edge
	 *
	 * @param angle	the angle of the gradient in degrees; 0° means from left to right
	 * @param color1  the color at the starting edge
	 * @param color2  the color at the ending edge
	 * @param breadth the total width of the gradient
	 */
	public static void angledGradient(@Nonnull PoseStack ms, float angle, int x, int y, int z, int breadth, int length, Color color1, Color color2) {
		ms.pushPose();
		ms.translate(x, y, z);
		ms.mulPose(Vector3f.ZP.rotationDegrees(angle - 90));

		Matrix4f model = ms.last().pose();
		int w = breadth / 2;
		GuiUtils.drawGradientRect(model, 0, -w, 0, w, length, color1.getRGB(), color2.getRGB());

		ms.popPose();
	}

	public static void breadcrumbArrow(PoseStack matrixStack, int x, int y, int z, int width, int height, int indent, Couple<Color> colors) {breadcrumbArrow(matrixStack, x, y, z, width, height, indent, colors.getFirst(), colors.getSecond());}

	// draws a wide chevron-style breadcrumb arrow pointing left
	public static void breadcrumbArrow(PoseStack matrixStack, int x, int y, int z, int width, int height, int indent, Color startColor, Color endColor) {
		matrixStack.pushPose();
		matrixStack.translate(x - indent, y, z);

		breadcrumbArrow(matrixStack, width, height, indent, startColor, endColor);

		matrixStack.popPose();
	}

	private static void breadcrumbArrow(PoseStack ms, int width, int height, int indent, Color c1, Color c2) {

		/*
		 * 0,0		 x1,y1 ********************* x4,y4 ***** x7,y7
		 *		 ****												 ****
		 *	****												 ****
		 * x0,y0	  x2,y2							  x5,y5
		 *	****												 ****
		 *		 ****												 ****
		 *			  x3,y3 ********************* x6,y6 ***** x8,y8
		 *
		 */

		float x0 = 0, y0 = height / 2f;
		float x1 = indent, y1 = 0;
		float x2 = indent, y2 = height / 2f;
		float x3 = indent, y3 = height;
		float x4 = width, y4 = 0;
		float x5 = width, y5 = height / 2f;
		float x6 = width, y6 = height;
		float x7 = indent + width, y7 = 0;
		float x8 = indent + width, y8 = height;

		indent = Math.abs(indent);
		width = Math.abs(width);
		Color fc1 = Color.mixColors(c1, c2, 0);
		Color fc2 = Color.mixColors(c1, c2, (indent) / (width + 2f * indent));
		Color fc3 = Color.mixColors(c1, c2, (indent + width) / (width + 2f * indent));
		Color fc4 = Color.mixColors(c1, c2, 1);

		RenderSystem.disableTexture();
		RenderSystem.enableBlend();
		RenderSystem.disableCull();
		RenderSystem.defaultBlendFunc();
		RenderSystem.setShader(GameRenderer::getPositionColorShader);

		Tesselator tessellator = Tesselator.getInstance();
		BufferBuilder bufferbuilder = tessellator.getBuilder();
		Matrix4f model = ms.last().pose();
		bufferbuilder.begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION_COLOR);

		bufferbuilder.vertex(model, x0, y0, 0).color(fc1.getRed(), fc1.getGreen(), fc1.getBlue(), fc1.getAlpha()).endVertex();
		bufferbuilder.vertex(model, x1, y1, 0).color(fc2.getRed(), fc2.getGreen(), fc2.getBlue(), fc2.getAlpha()).endVertex();
		bufferbuilder.vertex(model, x2, y2, 0).color(fc2.getRed(), fc2.getGreen(), fc2.getBlue(), fc2.getAlpha()).endVertex();

		bufferbuilder.vertex(model, x0, y0, 0).color(fc1.getRed(), fc1.getGreen(), fc1.getBlue(), fc1.getAlpha()).endVertex();
		bufferbuilder.vertex(model, x2, y2, 0).color(fc2.getRed(), fc2.getGreen(), fc2.getBlue(), fc2.getAlpha()).endVertex();
		bufferbuilder.vertex(model, x3, y3, 0).color(fc2.getRed(), fc2.getGreen(), fc2.getBlue(), fc2.getAlpha()).endVertex();

		bufferbuilder.vertex(model, x3, y3, 0).color(fc2.getRed(), fc2.getGreen(), fc2.getBlue(), fc2.getAlpha()).endVertex();
		bufferbuilder.vertex(model, x1, y1, 0).color(fc2.getRed(), fc2.getGreen(), fc2.getBlue(), fc2.getAlpha()).endVertex();
		bufferbuilder.vertex(model, x4, y4, 0).color(fc3.getRed(), fc3.getGreen(), fc3.getBlue(), fc3.getAlpha()).endVertex();

		bufferbuilder.vertex(model, x3, y3, 0).color(fc2.getRed(), fc2.getGreen(), fc2.getBlue(), fc2.getAlpha()).endVertex();
		bufferbuilder.vertex(model, x4, y4, 0).color(fc3.getRed(), fc3.getGreen(), fc3.getBlue(), fc3.getAlpha()).endVertex();
		bufferbuilder.vertex(model, x6, y6, 0).color(fc3.getRed(), fc3.getGreen(), fc3.getBlue(), fc3.getAlpha()).endVertex();

		bufferbuilder.vertex(model, x5, y5, 0).color(fc3.getRed(), fc3.getGreen(), fc3.getBlue(), fc3.getAlpha()).endVertex();
		bufferbuilder.vertex(model, x4, y4, 0).color(fc3.getRed(), fc3.getGreen(), fc3.getBlue(), fc3.getAlpha()).endVertex();
		bufferbuilder.vertex(model, x7, y7, 0).color(fc4.getRed(), fc4.getGreen(), fc4.getBlue(), fc4.getAlpha()).endVertex();

		bufferbuilder.vertex(model, x6, y6, 0).color(fc3.getRed(), fc3.getGreen(), fc3.getBlue(), fc3.getAlpha()).endVertex();
		bufferbuilder.vertex(model, x5, y5, 0).color(fc3.getRed(), fc3.getGreen(), fc3.getBlue(), fc3.getAlpha()).endVertex();
		bufferbuilder.vertex(model, x8, y8, 0).color(fc4.getRed(), fc4.getGreen(), fc4.getBlue(), fc4.getAlpha()).endVertex();

		tessellator.end();
		RenderSystem.enableCull();
		RenderSystem.disableBlend();
		RenderSystem.enableTexture();
	}

	//just like AbstractGui#drawTexture, but with a color at every vertex
	public static void drawColoredTexture(PoseStack ms, Color c, int x, int y, int tex_left, int tex_top, int width, int height) {
		drawColoredTexture(ms, c, x, y, 0, (float) tex_left, (float) tex_top, width, height, 256, 256);
	}

	public static void drawColoredTexture(PoseStack ms, Color c, int x, int y, int z, float tex_left, float tex_top, int width, int height, int sheet_width, int sheet_height) {
		drawColoredTexture(ms, c, x, x + width, y, y + height, z, width, height, tex_left, tex_top, sheet_width, sheet_height);
	}

	private static void drawColoredTexture(PoseStack ms, Color c, int left, int right, int top, int bot, int z, int tex_width, int tex_height, float tex_left, float tex_top, int sheet_width, int sheet_height) {
		drawTexturedQuad(ms.last().pose(), c, left, right, top, bot, z, (tex_left + 0.0F) / (float) sheet_width, (tex_left + (float) tex_width) / (float) sheet_width, (tex_top + 0.0F) / (float) sheet_height, (tex_top + (float) tex_height) / (float) sheet_height);
	}

	private static void drawTexturedQuad(Matrix4f m, Color c, int left, int right, int top, int bot, int z, float u1, float u2, float v1, float v2) {
		Tesselator tesselator = Tesselator.getInstance();
		BufferBuilder bufferbuilder = tesselator.getBuilder();
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		RenderSystem.setShader(GameRenderer::getPositionColorTexShader);
		bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR_TEX);
		bufferbuilder.vertex(m, (float) left , (float) bot, (float) z).color(c.getRed(), c.getGreen(), c.getBlue(), c.getAlpha()).uv(u1, v2).endVertex();
		bufferbuilder.vertex(m, (float) right, (float) bot, (float) z).color(c.getRed(), c.getGreen(), c.getBlue(), c.getAlpha()).uv(u2, v2).endVertex();
		bufferbuilder.vertex(m, (float) right, (float) top, (float) z).color(c.getRed(), c.getGreen(), c.getBlue(), c.getAlpha()).uv(u2, v1).endVertex();
		bufferbuilder.vertex(m, (float) left , (float) top, (float) z).color(c.getRed(), c.getGreen(), c.getBlue(), c.getAlpha()).uv(u1, v1).endVertex();
		tesselator.end();
		RenderSystem.disableBlend();
	}

	public static class CustomRenderTarget extends RenderTarget {

		public CustomRenderTarget(boolean pUseDepth) {
			super(pUseDepth);
		}

		public static CustomRenderTarget create(Window mainWindow) {
			CustomRenderTarget framebuffer = new CustomRenderTarget(false);
			framebuffer.resize(mainWindow.getWidth(), mainWindow.getHeight(), Minecraft.ON_OSX);
			framebuffer.setClearColor(0, 0, 0, 0);
			framebuffer.enableStencil();
			return framebuffer;
		}

		public void blitToScreen(float alpha) {
			Window window = Minecraft.getInstance().getWindow();
			this.blitToScreen(window.getWidth(), window.getHeight());
		}

		public void blitToScreen(int pWidth, int pHeight, float alpha) {
			RenderSystem.assertThread(RenderSystem::isOnGameThreadOrInit);
			if (!RenderSystem.isInInitPhase()) {
				RenderSystem.recordRenderCall(() -> {
					this._blitToScreen(pWidth, pHeight, alpha);
				});
			} else {
				this._blitToScreen(pWidth, pHeight, alpha);
			}
		}

		private void _blitToScreen(int pWidth, int pHeight, float alpha) {
			RenderSystem.assertThread(RenderSystem::isOnRenderThread);

			GlStateManager._enableDepthTest();
			GlStateManager._viewport(0, 0, pWidth, pHeight);

			Minecraft minecraft = Minecraft.getInstance();
			ShaderInstance shaderinstance = minecraft.gameRenderer.blitShader;
			shaderinstance.setSampler("DiffuseSampler", this.colorTextureId);
			Matrix4f matrix4f = Matrix4f.orthographic((float)pWidth, (float)(-pHeight), 1000.0F, 3000.0F);
			RenderSystem.setProjectionMatrix(matrix4f);
			if (shaderinstance.MODEL_VIEW_MATRIX != null) {
				shaderinstance.MODEL_VIEW_MATRIX.set(Matrix4f.createTranslateMatrix(0.0F, 0.0F, -2000.0F));
			}
			if (shaderinstance.PROJECTION_MATRIX != null) {
				shaderinstance.PROJECTION_MATRIX.set(matrix4f);
			}
			shaderinstance.apply();

			float f = (float)pWidth;
			float f1 = (float)pHeight;
			float f2 = (float)this.viewWidth / (float)this.width;
			float f3 = (float)this.viewHeight / (float)this.height;

			Tesselator tesselator = RenderSystem.renderThreadTesselator();
			BufferBuilder bufferbuilder = tesselator.getBuilder();
			bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
			bufferbuilder.vertex(0.0D, (double)f1, 0.0D).uv(0.0F, 0.0F).color(1, 1, 1, alpha).endVertex();
			bufferbuilder.vertex((double)f, (double)f1, 0.0D).uv(f2, 0.0F).color(1, 1, 1, alpha).endVertex();
			bufferbuilder.vertex((double)f, 0.0D, 0.0D).uv(f2, f3).color(1, 1, 1, alpha).endVertex();
			bufferbuilder.vertex(0.0D, 0.0D, 0.0D).uv(0.0F, f3).color(1, 1, 1, alpha).endVertex();
			bufferbuilder.end();
			BufferUploader._endInternal(bufferbuilder);

			shaderinstance.clear();
		}

	}

}
