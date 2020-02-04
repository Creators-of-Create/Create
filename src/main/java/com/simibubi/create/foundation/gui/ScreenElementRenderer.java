package com.simibubi.create.foundation.gui;

import java.util.function.Supplier;

import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.platform.GlStateManager;
import com.simibubi.create.foundation.utility.ColorHelper;

import net.minecraft.block.BlockState;
import net.minecraft.block.FireBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class ScreenElementRenderer {

	public static void render3DItem(Supplier<ItemStack> transformsAndStack) {
		GlStateManager.pushMatrix();

		GlStateManager.enableBlend();
		GlStateManager.enableRescaleNormal();
		GlStateManager.enableAlphaTest();
		RenderHelper.enableGUIStandardItemLighting();
		GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);

		ItemStack stack = transformsAndStack.get();

		Minecraft.getInstance().getItemRenderer().renderItemIntoGUI(stack, 0, 0);
		GlStateManager.popMatrix();
	}

	public static void renderBlock(Supplier<BlockState> transformsAndState) {
		renderBlock(transformsAndState, -1);
	}

	public static void renderBlock(Supplier<BlockState> transformsAndState, int color) {
		render(transformsAndState, null, -1);
	}

	public static void renderModel(Supplier<IBakedModel> transformsAndModel) {
		render(null, transformsAndModel, -1);
	}

	private static void render(Supplier<BlockState> transformsAndState, Supplier<IBakedModel> transformsAndModel,
			int color) {
		GlStateManager.pushMatrix();

		GlStateManager.enableBlend();
		GlStateManager.enableRescaleNormal();
		GlStateManager.enableAlphaTest();
		RenderHelper.enableGUIStandardItemLighting();
		GlStateManager.alphaFunc(516, 0.1F);
		GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
		GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		GlStateManager.translated(0, 0, 200);

		Minecraft mc = Minecraft.getInstance();
		BlockRendererDispatcher blockRenderer = mc.getBlockRendererDispatcher();
		IBakedModel modelToRender = null;
		BlockState blockToRender = null;
		boolean stateMode = transformsAndModel == null;
		boolean fire = false;

		if (stateMode) {
			blockToRender = transformsAndState.get();
			fire = (blockToRender.getBlock() instanceof FireBlock);
			modelToRender = blockRenderer.getModelForState(blockToRender);
		} else {
			modelToRender = transformsAndModel.get();
		}

		GlStateManager.scaled(50, -50, 50);
		mc.getTextureManager().bindTexture(AtlasTexture.LOCATION_BLOCKS_TEXTURE);

		
		GlStateManager.pushMatrix();
		if (fire) {
			blockRenderer.renderBlockBrightness(blockToRender, 1);
		} else {
			GlStateManager.rotated(90, 0, 1, 0);
			if (color == -1) {
				blockRenderer.getBlockModelRenderer().renderModelBrightnessColor(modelToRender, 1, 1, 1, 1);
			} else {
				Vec3d rgb = ColorHelper.getRGB(color);
				blockRenderer.getBlockModelRenderer().renderModelBrightnessColor(modelToRender, 1, (float) rgb.x,
						(float) rgb.y, (float) rgb.z);
			}
		}
		GlStateManager.popMatrix();

		if (stateMode && !blockToRender.getFluidState().isEmpty()) {
			RenderHelper.disableStandardItemLighting();
			Tessellator tessellator = Tessellator.getInstance();
			BufferBuilder bufferbuilder = tessellator.getBuffer();
			bufferbuilder.setTranslation(0, -300, 0);
			bufferbuilder.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
			blockRenderer.renderFluid(new BlockPos(0, 300, 0), mc.world, bufferbuilder, blockToRender.getFluidState());
			Tessellator.getInstance().draw();
			bufferbuilder.setTranslation(0, 0, 0);
		}

		GlStateManager.disableAlphaTest();
		GlStateManager.disableRescaleNormal();

		GlStateManager.popMatrix();
	}

}
