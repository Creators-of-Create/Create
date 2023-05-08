package com.simibubi.create.content.curiosities.girder;

import java.util.List;
import java.util.function.Predicate;

import com.google.common.base.Predicates;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.curiosities.tools.ExtendoGripItem;
import com.simibubi.create.foundation.config.AllConfigs;
import com.simibubi.create.foundation.utility.placement.IPlacementHelper;
import com.simibubi.create.foundation.utility.placement.PlacementOffset;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.common.ForgeMod;

public class GirderPlacementHelper implements IPlacementHelper {

	@Override
	public Predicate<ItemStack> getItemPredicate() {
		return AllBlocks.METAL_GIRDER::isIn;
	}

	@Override
	public Predicate<BlockState> getStatePredicate() {
		return Predicates.or(AllBlocks.METAL_GIRDER::has, AllBlocks.METAL_GIRDER_ENCASED_SHAFT::has);
	}

	private boolean canExtendToward(BlockState state, Direction side) {
		Axis axis = side.getAxis();
		if (state.getBlock() instanceof GirderBlock) {
			boolean x = state.getValue(GirderBlock.X);
			boolean z = state.getValue(GirderBlock.Z);
			if (!x && !z)
				return axis == Axis.Y;
			if (x && z)
				return true;
			return axis == (x ? Axis.X : Axis.Z);
		}

		if (state.getBlock() instanceof GirderEncasedShaftBlock)
			return axis != Axis.Y && axis != state.getValue(GirderEncasedShaftBlock.HORIZONTAL_AXIS);

		return false;
	}

	private int attachedPoles(Level world, BlockPos pos, Direction direction) {
		BlockPos checkPos = pos.relative(direction);
		BlockState state = world.getBlockState(checkPos);
		int count = 0;
		while (canExtendToward(state, direction)) {
			count++;
			checkPos = checkPos.relative(direction);
			state = world.getBlockState(checkPos);
		}
		return count;
	}

	private BlockState withAxis(BlockState state, Axis axis) {
		if (state.getBlock() instanceof GirderBlock)
			return state.setValue(GirderBlock.X, axis == Axis.X)
				.setValue(GirderBlock.Z, axis == Axis.Z)
				.setValue(GirderBlock.AXIS, axis);
		if (state.getBlock() instanceof GirderEncasedShaftBlock && axis.isHorizontal())
			return state.setValue(GirderEncasedShaftBlock.HORIZONTAL_AXIS, axis == Axis.X ? Axis.Z : Axis.X);
		return state;
	}

	@Override
	public PlacementOffset getOffset(Player player, Level world, BlockState state, BlockPos pos, BlockHitResult ray) {
		List<Direction> directions =
			IPlacementHelper.orderedByDistance(pos, ray.getLocation(), dir -> canExtendToward(state, dir));
		for (Direction dir : directions) {
			int range = AllConfigs.server().curiosities.placementAssistRange.get();
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

			if (!newState.getMaterial()
				.isReplaceable())
				continue;

			return PlacementOffset.success(newPos,
				bState -> Block.updateFromNeighbourShapes(withAxis(bState, dir.getAxis()), world, newPos));
		}

		return PlacementOffset.fail();
	}

}
