package com.simibubi.create.modules.contraptions.receivers;

import static net.minecraft.state.properties.BlockStateProperties.AXIS;
import static net.minecraft.state.properties.BlockStateProperties.FACING;

import com.mojang.blaze3d.platform.GlStateManager;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.foundation.utility.TessellatorHelper;
import com.simibubi.create.modules.contraptions.base.IRotate;
import com.simibubi.create.modules.contraptions.base.KineticTileEntity;
import com.simibubi.create.modules.contraptions.base.KineticTileEntityRenderer;
import com.simibubi.create.modules.contraptions.base.KineticTileEntityRenderer.BlockModelSpinner;
import com.simibubi.create.modules.logistics.block.FilteredTileEntityRenderer;

import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;

@SuppressWarnings("deprecation")
public class SawTileEntityRenderer extends TileEntityRenderer<SawTileEntity> {

	FilteredTileEntityRenderer filterRenderer;

	public SawTileEntityRenderer() {
		filterRenderer = new FilteredTileEntityRenderer();
	}

	@Override
	public void render(SawTileEntity te, double x, double y, double z, float partialTicks, int destroyStage) {
		super.render(te, x, y, z, partialTicks, destroyStage);

		boolean processingMode = te.getBlockState().get(SawBlock.FACING) == Direction.UP;
		if (processingMode && !te.inventory.isEmpty()) {
			boolean alongZ = !te.getBlockState().get(SawBlock.AXIS_ALONG_FIRST_COORDINATE);
			GlStateManager.pushMatrix();

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

			GlStateManager.translated(x + (alongZ ? offset : .5), y + (blockItem ? .925f : 13f / 16f),
					z + (alongZ ? .5 : offset));

			GlStateManager.scaled(.5, .5, .5);
			if (alongZ)
				GlStateManager.rotated(90, 0, 1, 0);
			GlStateManager.rotated(90, 1, 0, 0);
			itemRenderer.renderItem(stack, ItemCameraTransforms.TransformType.FIXED);
			GlStateManager.popMatrix();
		}

		// Filter
		filterRenderer.render(te, x, y, z, partialTicks, destroyStage);

		// Kinetic renders
		final BlockState state = getRenderedBlockState(te);
		KineticTileEntityRenderer.cacheIfMissing(state, getWorld(), BlockModelSpinner::new);
		final BlockPos pos = te.getPos();
		Axis axis = ((IRotate) te.getBlockState().getBlock()).getRotationAxis(te.getBlockState());
		float angle = KineticTileEntityRenderer.getAngleForTe(te, pos, axis);

		TessellatorHelper.prepareFastRender();
		TessellatorHelper.begin(DefaultVertexFormats.BLOCK);
		KineticTileEntityRenderer.renderFromCache(Tessellator.getInstance().getBuffer(), state, getWorld(), (float) x,
				(float) y, (float) z, pos, axis, angle);
		TessellatorHelper.draw();
	}

	protected BlockState getRenderedBlockState(KineticTileEntity te) {
		BlockState state = te.getBlockState();
		if (state.get(FACING).getAxis().isHorizontal()) {
			return AllBlocks.SHAFT_HALF.block.getDefaultState().with(FACING, state.get(FACING).getOpposite());
		}
		return AllBlocks.SHAFT.block.getDefaultState().with(AXIS, ((IRotate) state.getBlock()).getRotationAxis(state));
	}

}
