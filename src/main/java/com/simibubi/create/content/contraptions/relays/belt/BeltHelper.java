package com.simibubi.create.content.contraptions.relays.belt;

import com.simibubi.create.AllTags.AllItemTags;
import com.simibubi.create.foundation.utility.VecHelper;

import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3i;
import net.minecraft.world.IWorld;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;

public class BeltHelper {

	public static boolean isItemUpright(ItemStack stack) {
		return stack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY)
			.isPresent()
			|| stack.getItem()
				.is(AllItemTags.UPRIGHT_ON_BELT.tag);
	}

	public static BeltTileEntity getSegmentTE(IWorld world, BlockPos pos) {
		if (!world.isAreaLoaded(pos, 0))
			return null;
		TileEntity tileEntity = world.getBlockEntity(pos);
		if (!(tileEntity instanceof BeltTileEntity))
			return null;
		return (BeltTileEntity) tileEntity;
	}

	public static BeltTileEntity getControllerTE(IWorld world, BlockPos pos) {
		BeltTileEntity segment = getSegmentTE(world, pos);
		if (segment == null)
			return null;
		BlockPos controllerPos = segment.controller;
		if (controllerPos == null)
			return null;
		return getSegmentTE(world, controllerPos);
	}

	public static BeltTileEntity getBeltForOffset(BeltTileEntity controller, float offset) {
		return getBeltAtSegment(controller, (int) Math.floor(offset));
	}

	public static BeltTileEntity getBeltAtSegment(BeltTileEntity controller, int segment) {
		BlockPos pos = getPositionForOffset(controller, segment);
		TileEntity te = controller.getLevel()
			.getBlockEntity(pos);
		if (te == null || !(te instanceof BeltTileEntity))
			return null;
		return (BeltTileEntity) te;
	}

	public static BlockPos getPositionForOffset(BeltTileEntity controller, int offset) {
		BlockPos pos = controller.getBlockPos();
		Vector3i vec = controller.getBeltFacing()
			.getNormal();
		BeltSlope slope = controller.getBlockState()
			.getValue(BeltBlock.SLOPE);
		int verticality = slope == BeltSlope.DOWNWARD ? -1 : slope == BeltSlope.UPWARD ? 1 : 0;

		return pos.offset(offset * vec.getX(), MathHelper.clamp(offset, 0, controller.beltLength - 1) * verticality,
			offset * vec.getZ());
	}

	public static Vector3d getVectorForOffset(BeltTileEntity controller, float offset) {
		BeltSlope slope = controller.getBlockState()
			.getValue(BeltBlock.SLOPE);
		int verticality = slope == BeltSlope.DOWNWARD ? -1 : slope == BeltSlope.UPWARD ? 1 : 0;
		float verticalMovement = verticality;
		if (offset < .5)
			verticalMovement = 0;
		verticalMovement = verticalMovement * (Math.min(offset, controller.beltLength - .5f) - .5f);
		Vector3d vec = VecHelper.getCenterOf(controller.getBlockPos());
		Vector3d horizontalMovement = Vector3d.atLowerCornerOf(controller.getBeltFacing()
			.getNormal())
			.scale(offset - .5f);

		if (slope == BeltSlope.VERTICAL)
			horizontalMovement = Vector3d.ZERO;

		vec = vec.add(horizontalMovement)
			.add(0, verticalMovement, 0);
		return vec;
	}

	public static Vector3d getBeltVector(BlockState state) {
		BeltSlope slope = state.getValue(BeltBlock.SLOPE);
		int verticality = slope == BeltSlope.DOWNWARD ? -1 : slope == BeltSlope.UPWARD ? 1 : 0;
		Vector3d horizontalMovement = Vector3d.atLowerCornerOf(state.getValue(BeltBlock.HORIZONTAL_FACING)
			.getNormal());
		if (slope == BeltSlope.VERTICAL)
			return new Vector3d(0, state.getValue(BeltBlock.HORIZONTAL_FACING)
				.getAxisDirection()
				.getStep(), 0);
		return new Vector3d(0, verticality, 0).add(horizontalMovement);
	}

}
