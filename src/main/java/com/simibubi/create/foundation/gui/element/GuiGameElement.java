package com.simibubi.create.foundation.gui.element;

import javax.annotation.Nullable;

import com.jozufozu.flywheel.core.PartialModel;
import com.jozufozu.flywheel.core.virtual.VirtualEmptyModelData;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.GlStateManager.DestFactor;
import com.mojang.blaze3d.platform.GlStateManager.SourceFactor;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;
import com.simibubi.create.foundation.fluid.FluidRenderer;
import com.simibubi.create.foundation.gui.ILightingSettings;
import com.simibubi.create.foundation.gui.UIRenderHelper;
import com.simibubi.create.foundation.utility.Color;
import com.simibubi.create.foundation.utility.VecHelper;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.fluids.FluidStack;

public class GuiGameElement {

	public static GuiRenderBuilder of(ItemStack stack) {
		return new GuiItemRenderBuilder(stack);
	}

	public static GuiRenderBuilder of(ItemLike itemProvider) {
		return new GuiItemRenderBuilder(itemProvider);
	}

	public static GuiRenderBuilder of(BlockState state) {
		return new GuiBlockStateRenderBuilder(state);
	}

	public static GuiRenderBuilder of(PartialModel partial) {
		return new GuiBlockPartialRenderBuilder(partial);
	}

	public static GuiRenderBuilder of(Fluid fluid) {
		return new GuiBlockStateRenderBuilder(fluid.defaultFluidState()
			.createLegacyBlock()
			.setValue(LiquidBlock.LEVEL, 0));
	}

	public static abstract class GuiRenderBuilder extends RenderElement {
		protected double xLocal, yLocal, zLocal;
		protected double xRot, yRot, zRot;
		protected double scale = 1;
		protected int color = 0xFFFFFF;
		protected Vec3 rotationOffset = Vec3.ZERO;
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

		public GuiRenderBuilder withRotationOffset(Vec3 offset) {
			this.rotationOffset = offset;
			return this;
		}

		public GuiRenderBuilder lighting(ILightingSettings lighting) {
			customLighting = lighting;
			return this;
		}

		protected void prepareMatrix(PoseStack matrixStack) {
			matrixStack.pushPose();
			RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
			RenderSystem.enableDepthTest();
			RenderSystem.enableBlend();
			RenderSystem.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
			prepareLighting(matrixStack);
		}

		protected void transformMatrix(PoseStack matrixStack) {
			matrixStack.translate(x, y, z);
			matrixStack.scale((float) scale, (float) scale, (float) scale);
			matrixStack.translate(xLocal, yLocal, zLocal);
			UIRenderHelper.flipForGuiRender(matrixStack);
			matrixStack.translate(rotationOffset.x, rotationOffset.y, rotationOffset.z);
			matrixStack.mulPose(Vector3f.ZP.rotationDegrees((float) zRot));
			matrixStack.mulPose(Vector3f.XP.rotationDegrees((float) xRot));
			matrixStack.mulPose(Vector3f.YP.rotationDegrees((float) yRot));
			matrixStack.translate(-rotationOffset.x, -rotationOffset.y, -rotationOffset.z);
		}

		protected void cleanUpMatrix(PoseStack matrixStack) {
			matrixStack.popPose();
			cleanUpLighting(matrixStack);
		}

		protected void prepareLighting(PoseStack matrixStack) {
			if (customLighting != null) {
				customLighting.applyLighting();
			} else {
				Lighting.setupFor3DItems();
			}
		}

		protected void cleanUpLighting(PoseStack matrixStack) {
			if (customLighting != null) {
				Lighting.setupFor3DItems();
			}
		}
	}

	private static class GuiBlockModelRenderBuilder extends GuiRenderBuilder {

		protected BakedModel blockModel;
		protected BlockState blockState;

		public GuiBlockModelRenderBuilder(BakedModel blockmodel, @Nullable BlockState blockState) {
			this.blockState = blockState == null ? Blocks.AIR.defaultBlockState() : blockState;
			this.blockModel = blockmodel;
		}

		@Override
		public void render(PoseStack matrixStack) {
			prepareMatrix(matrixStack);

			Minecraft mc = Minecraft.getInstance();
			BlockRenderDispatcher blockRenderer = mc.getBlockRenderer();
			MultiBufferSource.BufferSource buffer = mc.renderBuffers()
				.bufferSource();
			RenderType renderType = blockState.getBlock() == Blocks.AIR ? Sheets.translucentCullBlockSheet()
				: ItemBlockRenderTypes.getRenderType(blockState, true);
			VertexConsumer vb = buffer.getBuffer(renderType);

			transformMatrix(matrixStack);

			RenderSystem.setShaderTexture(0, InventoryMenu.BLOCK_ATLAS);
			renderModel(blockRenderer, buffer, renderType, vb, matrixStack);

			cleanUpMatrix(matrixStack);
		}

		protected void renderModel(BlockRenderDispatcher blockRenderer, MultiBufferSource.BufferSource buffer,
			RenderType renderType, VertexConsumer vb, PoseStack ms) {
			int color = Minecraft.getInstance()
				.getBlockColors()
				.getColor(blockState, null, null, 0);
			Color rgb = new Color(color == -1 ? this.color : color);
			blockRenderer.getModelRenderer()
				.renderModel(ms.last(), vb, blockState, blockModel, rgb.getRedAsFloat(), rgb.getGreenAsFloat(), rgb.getBlueAsFloat(),
					LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, VirtualEmptyModelData.INSTANCE);
			buffer.endBatch();
		}

	}

	public static class GuiBlockStateRenderBuilder extends GuiBlockModelRenderBuilder {

		public GuiBlockStateRenderBuilder(BlockState blockstate) {
			super(Minecraft.getInstance()
				.getBlockRenderer()
				.getBlockModel(blockstate), blockstate);
		}

		@Override
		protected void renderModel(BlockRenderDispatcher blockRenderer, MultiBufferSource.BufferSource buffer,
			RenderType renderType, VertexConsumer vb, PoseStack ms) {
			if (blockState.getBlock() instanceof BaseFireBlock) {
				Lighting.setupForFlatItems();
				blockRenderer.renderSingleBlock(blockState, ms, buffer, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY,
					VirtualEmptyModelData.INSTANCE);
				buffer.endBatch();
				Lighting.setupFor3DItems();
				return;
			}

			super.renderModel(blockRenderer, buffer, renderType, vb, ms);

			if (blockState.getFluidState()
				.isEmpty())
				return;

			FluidRenderer.renderFluidBox(new FluidStack(blockState.getFluidState()
				.getType(), 1000), 0, 0, 0, 1, 1, 1, buffer, ms, LightTexture.FULL_BRIGHT, false);
			buffer.endBatch();
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

		public GuiItemRenderBuilder(ItemLike provider) {
			this(new ItemStack(provider));
		}

		@Override
		public void render(PoseStack matrixStack) {
			prepareMatrix(matrixStack);
			transformMatrix(matrixStack);
			renderItemIntoGUI(matrixStack, stack, customLighting == null);
			cleanUpMatrix(matrixStack);
		}

		public static void renderItemIntoGUI(PoseStack matrixStack, ItemStack stack, boolean useDefaultLighting) {
			ItemRenderer renderer = Minecraft.getInstance().getItemRenderer();
			BakedModel bakedModel = renderer.getModel(stack, null, null, 0);

			renderer.textureManager.getTexture(InventoryMenu.BLOCK_ATLAS).setFilter(false, false);
			RenderSystem.setShaderTexture(0, InventoryMenu.BLOCK_ATLAS);
			RenderSystem.enableBlend();
			RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
			RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
			matrixStack.pushPose();
			matrixStack.translate(0, 0, 100.0F + renderer.blitOffset);
			matrixStack.translate(8.0F, -8.0F, 0.0F);
			matrixStack.scale(16.0F, 16.0F, 16.0F);
			MultiBufferSource.BufferSource buffer = Minecraft.getInstance().renderBuffers().bufferSource();
			boolean flatLighting = !bakedModel.usesBlockLight();
			if (useDefaultLighting && flatLighting) {
				Lighting.setupForFlatItems();
			}

			renderer.render(stack, ItemTransforms.TransformType.GUI, false, matrixStack, buffer, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, bakedModel);
			buffer.endBatch();
			RenderSystem.enableDepthTest();
			if (useDefaultLighting && flatLighting) {
				Lighting.setupFor3DItems();
			}

			matrixStack.popPose();
		}

	}

}
