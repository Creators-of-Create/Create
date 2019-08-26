package com.simibubi.create.modules.logistics;

import org.apache.commons.lang3.tuple.Pair;

import com.mojang.blaze3d.platform.GlStateManager;
import com.simibubi.create.foundation.utility.TessellatorHelper;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

@SuppressWarnings("deprecation")
public class RedstoneBridgeTileEntityRenderer extends TileEntityRenderer<RedstoneBridgeTileEntity> {

	@Override
	public void render(RedstoneBridgeTileEntity tileEntityIn, double x, double y, double z, float partialTicks,
			int destroyStage) {
		super.render(tileEntityIn, x, y, z, partialTicks, destroyStage);

		Direction facing = tileEntityIn.getBlockState().get(BlockStateProperties.FACING);

		TessellatorHelper.prepareForDrawing();

		Pair<Vec3d, Vec3d> itemPositions = RedstoneBridgeBlock.getFrequencyItemPositions(tileEntityIn.getBlockState());
		Vec3d first = itemPositions.getLeft();
		Vec3d second = itemPositions.getRight();
		BlockPos pos = tileEntityIn.getPos();
		GlStateManager.translated(pos.getX(), pos.getY(), pos.getZ());

		renderFrequencyItem(tileEntityIn.frequencyFirst.getStack(), first, facing);
		renderFrequencyItem(tileEntityIn.frequencyLast.getStack(), second, facing);

		TessellatorHelper.cleanUpAfterDrawing();

	}

	private void renderFrequencyItem(ItemStack stack, Vec3d position, Direction facing) {
		ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
		boolean vertical = facing.getAxis().isVertical();
		
		IBakedModel modelWithOverrides = itemRenderer.getModelWithOverrides(stack);
		boolean blockItem = modelWithOverrides.isGui3d();
		
		float offX = 0;
		float offY = vertical && !blockItem ? 0 : 0;
		float offZ = !blockItem ? 1/4f : 0;
		if (vertical)
			offZ = -offZ;
		
		float rotX = vertical ? 90 : 0;
		float rotY = vertical ? 0 : facing.getHorizontalAngle() + (blockItem ? 180 : 0);
		float rotZ = vertical && facing == Direction.DOWN ? 180 : 0;
		if (facing.getAxis() == Axis.X) {
//			offZ = -offZ;
			rotY = -rotY;
		}
		
		float scale = !blockItem ? .25f : .5f;

		GlStateManager.pushMatrix();
		GlStateManager.translated(position.x, position.y, position.z);
		GlStateManager.scaled(scale, scale, scale);
		GlStateManager.rotatef(rotZ, 0, 0, 1);
		GlStateManager.rotatef(rotY, 0, 1, 0);
		GlStateManager.rotatef(rotX, 1, 0, 0);
		GlStateManager.translatef(offX, offY, offZ);
		itemRenderer.renderItem(stack, TransformType.FIXED);
		GlStateManager.popMatrix();
	}

}
