package com.simibubi.create.content.logistics.trains;

import java.util.List;

import com.jozufozu.flywheel.core.PartialModel;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.foundation.utility.Iterate;
import com.simibubi.create.foundation.utility.Pair;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.AxisDirection;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public interface ITrackBlock {

	public Vec3 getUpNormal(BlockGetter world, BlockPos pos, BlockState state);

	public List<Vec3> getTrackAxes(BlockGetter world, BlockPos pos, BlockState state);

	public Vec3 getCurveStart(BlockGetter world, BlockPos pos, BlockState state, Vec3 axis);

	public BlockState getBogeyAnchor(BlockGetter world, BlockPos pos, BlockState state); // should be on bogey side

	public boolean trackEquals(BlockState state1, BlockState state2);
	
	public default BlockState overlay(BlockGetter world, BlockPos pos, BlockState existing, BlockState placed) {
		return existing;
	}

	@OnlyIn(Dist.CLIENT)
	public PartialModel prepareStationOverlay(BlockGetter world, BlockPos pos, BlockState state,
		AxisDirection direction, PoseStack transform);

	@OnlyIn(Dist.CLIENT)
	public PartialModel prepareAssemblyOverlay(BlockGetter world, BlockPos pos, BlockState state, Direction direction,
		PoseStack ms);

	public default boolean isSlope(BlockGetter world, BlockPos pos, BlockState state) {
		return getTrackAxes(world, pos, state).get(0).y != 0;
	}

	public default Pair<Vec3, AxisDirection> getNearestTrackAxis(BlockGetter world, BlockPos pos, BlockState state,
		Vec3 lookVec) {
		Vec3 best = null;
		double bestDiff = Double.MAX_VALUE;
		for (Vec3 vec3 : getTrackAxes(world, pos, state)) {
			for (int opposite : Iterate.positiveAndNegative) {
				double distanceTo = vec3.normalize()
					.distanceTo(lookVec.scale(opposite));
				if (distanceTo > bestDiff)
					continue;
				bestDiff = distanceTo;
				best = vec3;
			}
		}
		return Pair.of(best, lookVec.dot(best.multiply(1, 0, 1)
			.normalize()) < 0 ? AxisDirection.POSITIVE : AxisDirection.NEGATIVE);
	}

}
