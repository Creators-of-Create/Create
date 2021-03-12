package com.simibubi.create.foundation.utility.placement.util;

import com.simibubi.create.content.curiosities.tools.ExtendoGripItem;
import com.simibubi.create.foundation.config.AllConfigs;
import com.simibubi.create.foundation.utility.placement.IPlacementHelper;
import com.simibubi.create.foundation.utility.placement.PlacementOffset;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.block.BlockState;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.state.IProperty;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.World;

import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

@MethodsReturnNonnullByDefault
public abstract class PoleHelper<T extends Comparable<T>> implements IPlacementHelper {

	protected final Predicate<BlockState> statePredicate;
	protected final IProperty<T> property;
	protected final Function<BlockState, Direction.Axis> axisFunction;

	public PoleHelper(Predicate<BlockState> statePredicate, Function<BlockState, Direction.Axis> axisFunction, IProperty<T> property) {
		this.statePredicate = statePredicate;
		this.axisFunction = axisFunction;
		this.property = property;
	}

	public boolean matchesAxis(BlockState state, Direction.Axis axis) {
		if (!statePredicate.test(state))
			return false;

		return axisFunction.apply(state) == axis;
	}

	public int attachedPoles(World world, BlockPos pos, Direction direction) {
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

	@Override
	public Predicate<BlockState> getStatePredicate() {
		return this.statePredicate;
	}

	@Override
	public PlacementOffset getOffset(PlayerEntity player, World world, BlockState state, BlockPos pos, BlockRayTraceResult ray) {
		List<Direction> directions = IPlacementHelper.orderedByDistance(pos, ray.getHitVec(), dir -> dir.getAxis() == axisFunction.apply(state));
		for (Direction dir : directions) {
			int range = AllConfigs.SERVER.curiosities.placementAssistRange.get();
			if (player != null) {
				IAttributeInstance reach = player.getAttribute(PlayerEntity.REACH_DISTANCE);
				if (reach.hasModifier(ExtendoGripItem.singleRangeAttributeModifier))
					range += 4;
			}
			int poles = attachedPoles(world, pos, dir);
			if (poles >= range)
				continue;

			BlockPos newPos = pos.offset(dir, poles + 1);
			BlockState newState = world.getBlockState(newPos);

			if (newState.getMaterial().isReplaceable())
				return PlacementOffset.success(newPos, bState -> bState.with(property, state.get(property)));

		}

		return PlacementOffset.fail();
	}
}
