package com.simibubi.create.content.contraptions.components.structureMovement.bearing;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.CreateClient;
import com.simibubi.create.foundation.utility.Iterate;
import com.simibubi.create.foundation.utility.VecHelper;

import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class SailBlockPlacementHelper {

	@OnlyIn(Dist.CLIENT)
	public static void tick() {
		Minecraft mc = Minecraft.getInstance();
		RayTraceResult objectMouseOver = mc.objectMouseOver;
		ClientWorld world = mc.world;
		ClientPlayerEntity player = mc.player;
		if (!(objectMouseOver instanceof BlockRayTraceResult))
			return;
		BlockRayTraceResult ray = (BlockRayTraceResult) objectMouseOver;
		if (!isHoldingSail(player))
			return;
		BlockPos pos = ray.getPos();
		BlockState blockState = world.getBlockState(pos);
		if (!(blockState.getBlock() instanceof SailBlock))
			return;

		Direction sailFacing = blockState.get(SailBlock.FACING);
		Direction offset = getPlacementOffset(world, sailFacing, pos, ray.getHitVec());
		if (offset == null)
			return;
		
		Vec3d centerOf = VecHelper.getCenterOf(pos);
		Vec3d offsetVec = new Vec3d(offset.getDirectionVec());

		if (!world.getBlockState(pos.offset(offset))
			.getMaterial()
			.isReplaceable())
			return;

		for (Direction caretDirection : Iterate.directions) {
			if (caretDirection.getAxis() == offset.getAxis())
				continue;
			if (caretDirection.getAxis() == sailFacing.getAxis())
				continue;

			Vec3d otherOffset = new Vec3d(caretDirection.getDirectionVec()).scale(.25f);
			Vec3d start = offsetVec.scale(.75f)
				.add(otherOffset);
			Vec3d target = centerOf.add(offsetVec);
			CreateClient.outliner.showLine("sailHelp" + caretDirection, centerOf.add(start), target)
				.lineWidth(1 / 16f);
		}

		return;
	}

	public static boolean isHoldingSail(PlayerEntity player) {
		for (Hand hand : Hand.values()) {
			ItemStack heldItem = player.getHeldItem(hand);
			if (AllBlocks.SAIL.isIn(heldItem) || AllBlocks.SAIL_FRAME.isIn(heldItem))
				return true;
		}
		return false;
	}

	public static Direction getPlacementOffset(World world, Direction sailDirection, BlockPos pos, Vec3d hit) {
		Direction argMin = null;
		float min = Float.MAX_VALUE;
		Vec3d diffFromCentre = hit.subtract(VecHelper.getCenterOf(pos));
		for (Direction side : Iterate.directions) {
			if (side.getAxis() == sailDirection.getAxis())
				continue;
			if (!world.getBlockState(pos.offset(side))
				.getMaterial()
				.isReplaceable())
				continue;
			float distance = (float) new Vec3d(side.getDirectionVec()).distanceTo(diffFromCentre);
			if (distance > min)
				continue;
			min = distance;
			argMin = side;
		}
		return argMin;
	}

}
