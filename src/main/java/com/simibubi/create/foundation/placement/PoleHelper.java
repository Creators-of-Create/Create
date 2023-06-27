package com.simibubi.create.foundation.placement;

import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

import com.simibubi.create.content.equipment.extendoGrip.ExtendoGripItem;
import com.simibubi.create.infrastructure.config.AllConfigs;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.common.ForgeMod;

@MethodsReturnNonnullByDefault
public abstract class PoleHelper<T extends Comparable<T>> implements IPlacementHelper {

	protected final Predicate<BlockState> statePredicate;
	protected final Property<T> property;
	protected final Function<BlockState, Direction.Axis> axisFunction;

	public PoleHelper(Predicate<BlockState> statePredicate, Function<BlockState, Direction.Axis> axisFunction, Property<T> property) {
		this.statePredicate = statePredicate;
		this.axisFunction = axisFunction;
		this.property = property;
	}

	public boolean matchesAxis(BlockState state, Direction.Axis axis) {
		if (!statePredicate.test(state))
			return false;

		return axisFunction.apply(state) == axis;
	}

	public int attachedPoles(Level world, BlockPos pos, Direction direction) {
		BlockPos checkPos = pos.relative(direction);
		BlockState state = world.getBlockState(checkPos);
		int count = 0;
		while (matchesAxis(state, direction.getAxis())) {
			count++;
			checkPos = checkPos.relative(direction);
			state = world.getBlockState(checkPos);
		}
		return count;
	}

	@Override
	public Predicate<BlockState> getStatePredicate() {
		return this.statePredicate;
	}

	@Override
	public PlacementOffset getOffset(Player player, Level world, BlockState state, BlockPos pos, BlockHitResult ray) {
		List<Direction> directions = IPlacementHelper.orderedByDistance(pos, ray.getLocation(), dir -> dir.getAxis() == axisFunction.apply(state));
		for (Direction dir : directions) {
			int range = AllConfigs.server().equipment.placementAssistRange.get();
			if (player != null) {
				AttributeInstance reach = player.getAttribute(ForgeMod.REACH_DISTANCE.get());
				if (reach != null && reach.hasModifier(ExtendoGripItem.singleRangeAttributeModifier))
					range += 4;
			}
			int poles = attachedPoles(world, pos, dir);
			if (poles >= range)
				continue;

			BlockPos newPos = pos.relative(dir, poles + 1);
			BlockState newState = world.getBlockState(newPos);

			if (newState.getMaterial().isReplaceable())
				return PlacementOffset.success(newPos, bState -> bState.setValue(property, state.getValue(property)));

		}

		return PlacementOffset.fail();
	}
}
