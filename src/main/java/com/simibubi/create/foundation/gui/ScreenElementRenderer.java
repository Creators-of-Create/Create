package com.simibubi.create.foundation.gui;

import java.util.function.Supplier;

import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager.DestFactor;
import com.mojang.blaze3d.platform.GlStateManager.SourceFactor;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import com.simibubi.create.foundation.utility.ColorHelper;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FireBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.model.data.EmptyModelData;

public class ScreenElementRenderer {

	public static void render3DItem(Supplier<ItemStack> transformsAndStack) {
		RenderSystem.pushMatrix();

		RenderSystem.enableBlend();
		RenderSystem.enableRescaleNormal();
		RenderSystem.enableAlphaTest();
		RenderHelper.enableGuiDepthLighting(); // TODO 1.15 buffered render
		RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);

		ItemStack stack = transformsAndStack.get();

		Minecraft.getInstance().getItemRenderer().renderItemIntoGUI(stack, 0, 0);
		RenderSystem.popMatrix();
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
		RenderSystem.pushMatrix();

		RenderSystem.enableBlend();
		RenderSystem.enableRescaleNormal();
		RenderSystem.enableAlphaTest();
		RenderHelper.enableGuiDepthLighting();
		RenderSystem.alphaFunc(516, 0.1F);
		RenderSystem.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
		RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		RenderSystem.translated(0, 0, 200);

		Minecraft mc = Minecraft.getInstance();
		BlockRendererDispatcher blockRenderer = mc.getBlockRendererDispatcher();
		IBakedModel modelToRender = null;
		BlockState blockToRender = Blocks.AIR.getDefaultState();
		boolean stateMode = transformsAndModel == null;
		boolean fire = false;

		if (stateMode) {
			blockToRender = transformsAndState.get();
			fire = (blockToRender.getBlock() instanceof FireBlock);
			modelToRender = blockRenderer.getModelForState(blockToRender);
		} else {
			modelToRender = transformsAndModel.get();
		}

		RenderSystem.scaled(50, -50, 50);
		IRenderTypeBuffer.Impl buffer = mc.getBufferBuilders().getEntityVertexConsumers(); 
		RenderType renderType = RenderTypeLookup.getEntityBlockLayer(blockToRender);
		IVertexBuilder vb = buffer.getBuffer(renderType);
		MatrixStack ms = new MatrixStack();
		
		RenderSystem.pushMatrix();
		if (fire) {
			blockRenderer.renderBlock(blockToRender, ms, buffer, 0xF000F0, OverlayTexture.DEFAULT_UV,
					EmptyModelData.INSTANCE);
		} else {
			RenderSystem.rotatef(90, 0, 1, 0);
			if (color == -1) {
				blockRenderer.getBlockModelRenderer().renderModel(ms.peek(), vb, blockToRender, modelToRender, 1, 1, 1,
						0xF000F0, OverlayTexture.DEFAULT_UV, EmptyModelData.INSTANCE);
			} else {
				Vec3d rgb = ColorHelper.getRGB(color);
				blockRenderer.getBlockModelRenderer().renderModel(ms.peek(), vb, blockToRender, modelToRender,
						(float) rgb.x, (float) rgb.y, (float) rgb.z, 0xF000F0, OverlayTexture.DEFAULT_UV,
						EmptyModelData.INSTANCE);
			}
		}
		RenderSystem.popMatrix();

		if (stateMode && !blockToRender.getFluidState().isEmpty()) {
			RenderHelper.disableStandardItemLighting();
			RenderSystem.translatef(0, -300, 0);
			blockRenderer.renderFluid(new BlockPos(0, 300, 0), mc.world, vb, blockToRender.getFluidState());
		}
		
		buffer.draw(renderType);

		RenderSystem.disableAlphaTest();
		RenderSystem.disableRescaleNormal();

		RenderSystem.popMatrix();
	}

}
