package com.simibubi.create.content.contraptions.components.structureMovement.piston;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.CreateClient;
import com.simibubi.create.foundation.utility.Iterate;
import com.simibubi.create.foundation.utility.Pair;
import com.simibubi.create.foundation.utility.VecHelper;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.Arrays;

public class PistonPolePlacementHelper {

	@OnlyIn(Dist.CLIENT)
	public static void tick() {
		Minecraft mc = Minecraft.getInstance();
		ClientWorld world = mc.world;

		if (!(mc.objectMouseOver instanceof BlockRayTraceResult))
			return;

		BlockRayTraceResult ray = (BlockRayTraceResult) mc.objectMouseOver;

		if (!isHoldingPole(mc.player))
			return;

		BlockPos pos = ray.getPos();
		BlockState state = world.getBlockState(pos);
		if (!(state.getBlock() instanceof PistonExtensionPoleBlock))
			return;

		Pair<Direction, Integer> offset = getPlacementOffset(world, state.get(PistonExtensionPoleBlock.FACING).getAxis(), pos, ray.getHitVec());
		if (offset == null || offset.getSecond() == 0)
			return;

		Direction hitFace = ray.getFace();

		if (hitFace.getAxis() == offset.getFirst().getAxis())
			return;

		Vec3d hitCenter = VecHelper.getCenterOf(pos).add(new Vec3d(hitFace.getDirectionVec()).scale(0.3));

		//get the two perpendicular directions to form the arrow
		Direction[] directions = Arrays.stream(Direction.Axis.values()).filter(axis -> axis != hitFace.getAxis() && axis != offset.getFirst().getAxis()).map(Iterate::directionsInAxis).findFirst().orElse(new Direction[]{});
		Vec3d startOffset = new Vec3d(offset.getFirst().getDirectionVec());
		Vec3d start = hitCenter.add(startOffset);
		for (Direction dir : directions) {
			Vec3d arrowOffset = new Vec3d(dir.getDirectionVec()).scale(.25);
			Vec3d target = hitCenter.add(startOffset.scale(0.75)).add(arrowOffset);
			CreateClient.outliner.showLine("poleHelp" + offset.getFirst() + dir, start, target).lineWidth(1/16f);
		}
	}

	// first indicates the direction that the position needs to be offset into
	// second indicates by how many blocks the position needs to be offset by; is 0 if there was no valid position on either end of the pole
	public static Pair<Direction, Integer> getPlacementOffset(World world, Direction.Axis poleAxis, BlockPos pos, Vec3d hit) {
		Pair<Direction, Integer> offset = null;
		double min = Double.MAX_VALUE;
		Vec3d localPos = hit.subtract(VecHelper.getCenterOf(pos));

		//find target direction
		for (Direction dir : Iterate.directionsInAxis(poleAxis)) {
			double distance = new Vec3d(dir.getDirectionVec()).distanceTo(localPos);
			if (distance > min)
				continue;
			min = distance;
			offset = Pair.of(dir, 0);
		}

		if (offset == null)//??
			return null;

		//check for space at the end of the pole
		int poles = attachedPoles(world, pos, offset.getFirst());
		BlockState state = world.getBlockState(pos.offset(offset.getFirst(), poles + 1));

		if (state.getMaterial().isReplaceable()) {
			offset.setSecond(poles + 1);
			return offset;
		}

		//check the other end of the pole
		offset.setFirst(offset.getFirst().getOpposite());
		poles = attachedPoles(world, pos, offset.getFirst());
		state = world.getBlockState(pos.offset(offset.getFirst(), poles + 1));

		if (state.getMaterial().isReplaceable()) {
			offset.setSecond(poles + 1);
		}

		return offset;
	}

	public static int attachedPoles(World world, BlockPos pos, Direction direction) {
		BlockPos checkPos = pos.offset(direction);
		BlockState state = world.getBlockState(checkPos);
		int count = 0;
		while (matchesAxis(state, direction.getAxis())) {
			count++;
			checkPos = checkPos.offset(direction);
			state = world.getBlockState(checkPos);
		}
		return count;
	}

	//checks if the given state is a piston pole on the given axis
	public static boolean matchesAxis(BlockState state, Direction.Axis axis) {
		return AllBlocks.PISTON_EXTENSION_POLE.has(state) && state.get(PistonExtensionPoleBlock.FACING).getAxis() == axis;
	}

	public static boolean isHoldingPole(PlayerEntity player) {
		return Arrays.stream(Hand.values()).anyMatch(hand -> AllBlocks.PISTON_EXTENSION_POLE.isIn(player.getHeldItem(hand)));
	}

}
