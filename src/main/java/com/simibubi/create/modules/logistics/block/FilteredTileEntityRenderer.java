package com.simibubi.create.modules.logistics.block;

import com.mojang.blaze3d.platform.GlStateManager;
import com.simibubi.create.foundation.utility.TessellatorHelper;

import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms.TransformType;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

@SuppressWarnings("deprecation")
public class FilteredTileEntityRenderer {

	public static <T extends TileEntity & IHaveFilter> void render(T tileEntityIn, double x, double y, double z,
			float partialTicks, int destroyStage) {
		BlockState state = tileEntityIn.getBlockState();
		IBlockWithFilter block = (IBlockWithFilter) state.getBlock();

		if (!block.isFilterVisible(state))
			return;

		Direction facing = block.getFilterFacing(state);
		float scale = block.getItemHitboxScale();

		TessellatorHelper.prepareForDrawing();

		Vec3d position = block.getFilterPosition(state);
		BlockPos pos = tileEntityIn.getPos();
		GlStateManager.translated(pos.getX(), pos.getY(), pos.getZ());

		renderFilterItem(tileEntityIn.getFilter(), position, facing, scale - 2 / 16f, block.getFilterAngle(state));

		TessellatorHelper.cleanUpAfterDrawing();

	}

	private static void renderFilterItem(ItemStack stack, Vec3d position, Direction facing, float scaleDiff,
			float angle) {
		ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
		boolean vertical = facing.getAxis().isVertical();

		IBakedModel modelWithOverrides = itemRenderer.getModelWithOverrides(stack);
		boolean blockItem = modelWithOverrides.isGui3d();

		float offX = 0;
		float offY = 0;
		float offZ = !blockItem ? 1 / 4f + 2 * scaleDiff - 1 / 16f : 1 / 16f;
		if (vertical)
			offZ = -offZ;

		float rotX = vertical ? 90 : 0 - (blockItem ? -90f + angle : 90 - angle);
		float rotY = vertical ? 0 : facing.getHorizontalAngle() + (blockItem ? 180 : 0);
		float rotZ = vertical && facing == Direction.DOWN ? 180 : 0;
		if (facing.getAxis() == Axis.X) {
			rotY = -rotY;
		}

		float scale = !blockItem ? .25f : .5f;
		scale *= 1 + 8 * scaleDiff;

		GlStateManager.pushMatrix();
		GlStateManager.translated(position.x, position.y, position.z);
		GlStateManager.rotatef(rotZ, 0, 0, 1);
		GlStateManager.rotatef(rotY, 0, 1, 0);
		GlStateManager.rotatef(rotX, 1, 0, 0);
		GlStateManager.scaled(scale, scale, scale);
		GlStateManager.translatef(offX, offY, offZ);
		itemRenderer.renderItem(stack, TransformType.FIXED);
		GlStateManager.popMatrix();
	}

}
