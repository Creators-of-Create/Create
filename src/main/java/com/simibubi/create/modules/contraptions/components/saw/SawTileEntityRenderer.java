package com.simibubi.create.modules.contraptions.components.saw;

import static net.minecraft.state.properties.BlockStateProperties.AXIS;
import static net.minecraft.state.properties.BlockStateProperties.FACING;

import com.mojang.blaze3d.systems.RenderSystem;
import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.CreateClient;
import com.simibubi.create.foundation.behaviour.filtering.FilteringRenderer;
import com.simibubi.create.foundation.block.SafeTileEntityRenderer;
import com.simibubi.create.foundation.utility.SuperByteBuffer;
import com.simibubi.create.foundation.utility.TessellatorHelper;
import com.simibubi.create.modules.contraptions.base.IRotate;
import com.simibubi.create.modules.contraptions.base.KineticTileEntity;
import com.simibubi.create.modules.contraptions.base.KineticTileEntityRenderer;

import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.MathHelper;

@SuppressWarnings("deprecation")
public class SawTileEntityRenderer extends SafeTileEntityRenderer<SawTileEntity> {

	public SawTileEntityRenderer(TileEntityRendererDispatcher dispatcher) {
		super(dispatcher);
	}

	@Override
	public void renderWithGL(SawTileEntity te, double x, double y, double z, float partialTicks, int destroyStage) {
		renderItems(te, x, y, z, partialTicks);
		FilteringRenderer.renderOnTileEntity(te, x, y, z, partialTicks, destroyStage);
		renderShaft(te, x, y, z);
	}

	protected void renderShaft(SawTileEntity te, double x, double y, double z) {
		TessellatorHelper.prepareFastRender();
		TessellatorHelper.begin(DefaultVertexFormats.BLOCK);
		KineticTileEntityRenderer.renderRotatingBuffer(te, getWorld(), getRotatedModel(te), x, y, z,
				Tessellator.getInstance().getBuffer());
		TessellatorHelper.draw();
	}

	protected void renderItems(SawTileEntity te, double x, double y, double z, float partialTicks) {
		boolean processingMode = te.getBlockState().get(SawBlock.FACING) == Direction.UP;
		if (processingMode && !te.inventory.isEmpty()) {
			boolean alongZ = !te.getBlockState().get(SawBlock.AXIS_ALONG_FIRST_COORDINATE);
			RenderSystem.pushMatrix();

			boolean moving = te.inventory.recipeDuration != 0;
			float offset = moving ? (float) (te.inventory.remainingTime) / te.inventory.recipeDuration : 0;
			if (moving)
				offset = MathHelper.clamp(offset + (-partialTicks + .5f) / te.inventory.recipeDuration, 0, 1);

			if (te.getSpeed() == 0)
				offset = .5f;
			if (te.getSpeed() < 0 ^ alongZ)
				offset = 1 - offset;

			ItemStack stack = te.inventory.getStackInSlot(0);
			ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
			IBakedModel modelWithOverrides = itemRenderer.getModelWithOverrides(stack);
			boolean blockItem = modelWithOverrides.isGui3d();

			RenderSystem.translated(x + (alongZ ? offset : .5), y + (blockItem ? .925f : 13f / 16f),
					z + (alongZ ? .5 : offset));

			RenderSystem.scaled(.5, .5, .5);
			if (alongZ)
				RenderSystem.rotatef(90, 0, 1, 0);
			RenderSystem.rotatef(90, 1, 0, 0);
			itemRenderer.renderItem(stack, ItemCameraTransforms.TransformType.FIXED);
			RenderSystem.popMatrix();
		}
	}

	protected SuperByteBuffer getRotatedModel(KineticTileEntity te) {
		BlockState state = te.getBlockState();
		if (state.get(FACING).getAxis().isHorizontal())
			return AllBlockPartials.SHAFT_HALF.renderOnDirectional(state.rotate(Rotation.CLOCKWISE_180));
		return CreateClient.bufferCache.renderBlockIn(KineticTileEntityRenderer.KINETIC_TILE,
				getRenderedBlockState(te));
	}

	protected BlockState getRenderedBlockState(KineticTileEntity te) {
		BlockState state = te.getBlockState();
		return AllBlocks.SHAFT.get().getDefaultState().with(AXIS, ((IRotate) state.getBlock()).getRotationAxis(state));
	}

}
