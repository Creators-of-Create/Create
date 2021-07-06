package com.simibubi.create.foundation.gui;

import javax.annotation.Nullable;

import com.jozufozu.flywheel.core.PartialModel;
import com.jozufozu.flywheel.util.VirtualEmptyModelData;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.GlStateManager.DestFactor;
import com.mojang.blaze3d.platform.GlStateManager.SourceFactor;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import com.simibubi.create.foundation.fluid.FluidRenderer;
import com.simibubi.create.foundation.utility.ColorHelper;
import com.simibubi.create.foundation.utility.VecHelper;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FireBlock;
import net.minecraft.block.FlowingFluidBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Atlases;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.fluid.Fluid;
import net.minecraft.inventory.container.PlayerContainer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IItemProvider;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraftforge.fluids.FluidStack;

public class GuiGameElement {

	public static GuiRenderBuilder of(ItemStack stack) {
		return new GuiItemRenderBuilder(stack);
	}

	public static GuiRenderBuilder of(IItemProvider itemProvider) {
		return new GuiItemRenderBuilder(itemProvider);
	}

	public static GuiRenderBuilder of(BlockState state) {
		return new GuiBlockStateRenderBuilder(state);
	}

	public static GuiRenderBuilder of(PartialModel partial) {
		return new GuiBlockPartialRenderBuilder(partial);
	}

	public static GuiRenderBuilder of(Fluid fluid) {
		return new GuiBlockStateRenderBuilder(fluid.getDefaultState()
			.getBlockState()
			.with(FlowingFluidBlock.LEVEL, 0));
	}

	public static abstract class GuiRenderBuilder extends RenderElement {
		protected double xLocal, yLocal, zLocal;
		protected double xRot, yRot, zRot;
		protected double scale = 1;
		protected int color = 0xFFFFFF;
		protected Vector3d rotationOffset = Vector3d.ZERO;
		protected ILightingSettings customLighting = null;

		public GuiRenderBuilder atLocal(double x, double y, double z) {
			this.xLocal = x;
			this.yLocal = y;
			this.zLocal = z;
			return this;
		}

		public GuiRenderBuilder rotate(double xRot, double yRot, double zRot) {
			this.xRot = xRot;
			this.yRot = yRot;
			this.zRot = zRot;
			return this;
		}

		public GuiRenderBuilder rotateBlock(double xRot, double yRot, double zRot) {
			return this.rotate(xRot, yRot, zRot)
				.withRotationOffset(VecHelper.getCenterOf(BlockPos.ZERO));
		}

		public GuiRenderBuilder scale(double scale) {
			this.scale = scale;
			return this;
		}

		public GuiRenderBuilder color(int color) {
			this.color = color;
			return this;
		}

		public GuiRenderBuilder withRotationOffset(Vector3d offset) {
			this.rotationOffset = offset;
			return this;
		}

		public GuiRenderBuilder lighting(ILightingSettings lighting) {
			customLighting = lighting;
			return this;
		}

		protected void prepareMatrix(MatrixStack matrixStack) {
			matrixStack.push();
			RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
			RenderSystem.alphaFunc(516, 0.1F);
			RenderSystem.enableAlphaTest();
			RenderSystem.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
			RenderSystem.enableBlend();
			RenderSystem.enableDepthTest();
			RenderSystem.enableRescaleNormal();
			prepareLighting(matrixStack);
		}

		protected void transformMatrix(MatrixStack matrixStack) {
			matrixStack.translate(x, y, z);
			matrixStack.scale((float) scale, (float) scale, (float) scale);
			matrixStack.translate(xLocal, yLocal, zLocal);
			matrixStack.scale(1, -1, 1);
			matrixStack.translate(rotationOffset.x, rotationOffset.y, rotationOffset.z);
			matrixStack.multiply(Vector3f.POSITIVE_Z.getDegreesQuaternion((float) zRot));
			matrixStack.multiply(Vector3f.POSITIVE_X.getDegreesQuaternion((float) xRot));
			matrixStack.multiply(Vector3f.POSITIVE_Y.getDegreesQuaternion((float) yRot));
			matrixStack.translate(-rotationOffset.x, -rotationOffset.y, -rotationOffset.z);
		}

		protected void cleanUpMatrix(MatrixStack matrixStack) {
			matrixStack.pop();
			RenderSystem.disableRescaleNormal();
			RenderSystem.disableAlphaTest();
			cleanUpLighting(matrixStack);
		}

		protected void prepareLighting(MatrixStack matrixStack) {
			if (customLighting != null) {
				customLighting.applyLighting();
			} else {
				RenderHelper.enableGuiDepthLighting();
			}
		}

		protected void cleanUpLighting(MatrixStack matrixStack) {
			if (customLighting != null) {
				RenderHelper.enableGuiDepthLighting();
			}
		}
	}

	private static class GuiBlockModelRenderBuilder extends GuiRenderBuilder {

		protected IBakedModel blockModel;
		protected BlockState blockState;

		public GuiBlockModelRenderBuilder(IBakedModel blockmodel, @Nullable BlockState blockState) {
			this.blockState = blockState == null ? Blocks.AIR.getDefaultState() : blockState;
			this.blockModel = blockmodel;
		}

		@Override
		public void render(MatrixStack matrixStack) {
			prepareMatrix(matrixStack);

			Minecraft mc = Minecraft.getInstance();
			BlockRendererDispatcher blockRenderer = mc.getBlockRendererDispatcher();
			IRenderTypeBuffer.Impl buffer = mc.getBufferBuilders()
				.getEntityVertexConsumers();
			RenderType renderType = blockState.getBlock() == Blocks.AIR ? Atlases.getEntityTranslucentCull()
				: RenderTypeLookup.getEntityBlockLayer(blockState, true);
			IVertexBuilder vb = buffer.getBuffer(renderType);

			transformMatrix(matrixStack);

			mc.getTextureManager()
				.bindTexture(PlayerContainer.BLOCK_ATLAS_TEXTURE);
			renderModel(blockRenderer, buffer, renderType, vb, matrixStack);

			cleanUpMatrix(matrixStack);
		}

		protected void renderModel(BlockRendererDispatcher blockRenderer, IRenderTypeBuffer.Impl buffer,
			RenderType renderType, IVertexBuilder vb, MatrixStack ms) {
			int color = Minecraft.getInstance()
				.getBlockColors()
				.getColor(blockState, null, null, 0);
			Vector3d rgb = ColorHelper.getRGB(color == -1 ? this.color : color);
			blockRenderer.getBlockModelRenderer()
				.renderModel(ms.peek(), vb, blockState, blockModel, (float) rgb.x, (float) rgb.y, (float) rgb.z,
					0xF000F0, OverlayTexture.DEFAULT_UV, VirtualEmptyModelData.INSTANCE);
			buffer.draw();
		}

	}

	public static class GuiBlockStateRenderBuilder extends GuiBlockModelRenderBuilder {

		public GuiBlockStateRenderBuilder(BlockState blockstate) {
			super(Minecraft.getInstance()
				.getBlockRendererDispatcher()
				.getModelForState(blockstate), blockstate);
		}

		@Override
		protected void renderModel(BlockRendererDispatcher blockRenderer, IRenderTypeBuffer.Impl buffer,
			RenderType renderType, IVertexBuilder vb, MatrixStack ms) {
			if (blockState.getBlock() instanceof FireBlock) {
				RenderHelper.disableGuiDepthLighting();
				blockRenderer.renderBlock(blockState, ms, buffer, 0xF000F0, OverlayTexture.DEFAULT_UV,
					VirtualEmptyModelData.INSTANCE);
				buffer.draw();
				RenderHelper.enableGuiDepthLighting();
				return;
			}

			super.renderModel(blockRenderer, buffer, renderType, vb, ms);

			if (blockState.getFluidState()
				.isEmpty())
				return;

			FluidRenderer.renderTiledFluidBB(new FluidStack(blockState.getFluidState()
				.getFluid(), 1000), 0, 0, 0, 1.0001f, 1.0001f, 1.0001f, buffer, ms, 0xF000F0, false);
			buffer.draw();
		}
	}

	public static class GuiBlockPartialRenderBuilder extends GuiBlockModelRenderBuilder {

		public GuiBlockPartialRenderBuilder(PartialModel partial) {
			super(partial.get(), null);
		}

	}

	public static class GuiItemRenderBuilder extends GuiRenderBuilder {

		private final ItemStack stack;

		public GuiItemRenderBuilder(ItemStack stack) {
			this.stack = stack;
		}

		public GuiItemRenderBuilder(IItemProvider provider) {
			this(new ItemStack(provider));
		}

		@Override
		public void render(MatrixStack matrixStack) {
			prepareMatrix(matrixStack);
			transformMatrix(matrixStack);
			renderItemIntoGUI(matrixStack, stack, customLighting == null);
			cleanUpMatrix(matrixStack);
		}

		public static void renderItemIntoGUI(MatrixStack matrixStack, ItemStack stack, boolean useDefaultLighting) {
			ItemRenderer renderer = Minecraft.getInstance().getItemRenderer();
			IBakedModel bakedModel = renderer.getItemModelWithOverrides(stack, null, null);

			matrixStack.push();
			renderer.textureManager.bindTexture(AtlasTexture.LOCATION_BLOCKS_TEXTURE);
			renderer.textureManager.getTexture(AtlasTexture.LOCATION_BLOCKS_TEXTURE).setBlurMipmapDirect(false, false);
			RenderSystem.enableRescaleNormal();
			RenderSystem.enableAlphaTest();
			RenderSystem.enableCull();
			RenderSystem.defaultAlphaFunc();
			RenderSystem.enableBlend();
			RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
			RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
			matrixStack.translate(0, 0, 100.0F + renderer.zLevel);
			matrixStack.translate(8.0F, -8.0F, 0.0F);
			matrixStack.scale(16.0F, 16.0F, 16.0F);
			IRenderTypeBuffer.Impl buffer = Minecraft.getInstance().getBufferBuilders().getEntityVertexConsumers();
			boolean flatLighting = !bakedModel.isSideLit();
			if (useDefaultLighting) {
				if (flatLighting) {
					RenderHelper.disableGuiDepthLighting();
				}
			}

			renderer.renderItem(stack, ItemCameraTransforms.TransformType.GUI, false, matrixStack, buffer, 0xF000F0, OverlayTexture.DEFAULT_UV, bakedModel);
			buffer.draw();
			RenderSystem.enableDepthTest();
			if (useDefaultLighting) {
				if (flatLighting) {
					RenderHelper.enableGuiDepthLighting();
				}
			}

			RenderSystem.disableAlphaTest();
			RenderSystem.disableRescaleNormal();
			RenderSystem.enableCull();
			matrixStack.pop();
		}

	}

}
