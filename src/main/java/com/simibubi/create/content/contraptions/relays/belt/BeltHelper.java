package com.simibubi.create.content.contraptions.relays.belt;

import com.simibubi.create.AllTags.AllItemTags;
import com.simibubi.create.foundation.utility.VecHelper;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;

public class BeltHelper {

	public static boolean isItemUpright(ItemStack stack) {
		return stack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY)
			.isPresent() || AllItemTags.UPRIGHT_ON_BELT.matches(stack);
	}

	public static BeltTileEntity getSegmentTE(LevelAccessor world, BlockPos pos) {
		if (world instanceof Level l && !l.isLoaded(pos))
			return null;
		BlockEntity tileEntity = world.getBlockEntity(pos);
		if (!(tileEntity instanceof BeltTileEntity))
			return null;
		return (BeltTileEntity) tileEntity;
	}

	public static BeltTileEntity getControllerTE(LevelAccessor world, BlockPos pos) {
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
		BlockEntity te = controller.getLevel()
			.getBlockEntity(pos);
		if (te == null || !(te instanceof BeltTileEntity))
			return null;
		return (BeltTileEntity) te;
	}

	public static BlockPos getPositionForOffset(BeltTileEntity controller, int offset) {
		BlockPos pos = controller.getBlockPos();
		Vec3i vec = controller.getBeltFacing()
			.getNormal();
		BeltSlope slope = controller.getBlockState()
			.getValue(BeltBlock.SLOPE);
		int verticality = slope == BeltSlope.DOWNWARD ? -1 : slope == BeltSlope.UPWARD ? 1 : 0;

		return pos.offset(offset * vec.getX(), Mth.clamp(offset, 0, controller.beltLength - 1) * verticality,
			offset * vec.getZ());
	}

	public static Vec3 getVectorForOffset(BeltTileEntity controller, float offset) {
		BeltSlope slope = controller.getBlockState()
			.getValue(BeltBlock.SLOPE);
		int verticality = slope == BeltSlope.DOWNWARD ? -1 : slope == BeltSlope.UPWARD ? 1 : 0;
		float verticalMovement = verticality;
		if (offset < .5)
			verticalMovement = 0;
		verticalMovement = verticalMovement * (Math.min(offset, controller.beltLength - .5f) - .5f);
		Vec3 vec = VecHelper.getCenterOf(controller.getBlockPos());
		Vec3 horizontalMovement = Vec3.atLowerCornerOf(controller.getBeltFacing()
			.getNormal())
			.scale(offset - .5f);

		if (slope == BeltSlope.VERTICAL)
			horizontalMovement = Vec3.ZERO;

		vec = vec.add(horizontalMovement)
			.add(0, verticalMovement, 0);
		return vec;
	}

	public static Vec3 getBeltVector(BlockState state) {
		BeltSlope slope = state.getValue(BeltBlock.SLOPE);
		int verticality = slope == BeltSlope.DOWNWARD ? -1 : slope == BeltSlope.UPWARD ? 1 : 0;
		Vec3 horizontalMovement = Vec3.atLowerCornerOf(state.getValue(BeltBlock.HORIZONTAL_FACING)
			.getNormal());
		if (slope == BeltSlope.VERTICAL)
			return new Vec3(0, state.getValue(BeltBlock.HORIZONTAL_FACING)
				.getAxisDirection()
				.getStep(), 0);
		return new Vec3(0, verticality, 0).add(horizontalMovement);
	}

}
