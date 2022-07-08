package com.simibubi.create.content.contraptions.components.structureMovement.chassis;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.contraptions.components.structureMovement.BlockMovementChecks;
import com.simibubi.create.foundation.config.AllConfigs;
import com.simibubi.create.foundation.tileEntity.SmartTileEntity;
import com.simibubi.create.foundation.tileEntity.TileEntityBehaviour;
import com.simibubi.create.foundation.tileEntity.behaviour.CenteredSideValueBoxTransform;
import com.simibubi.create.foundation.tileEntity.behaviour.scrollvalue.BulkScrollValueBehaviour;
import com.simibubi.create.foundation.tileEntity.behaviour.scrollvalue.ScrollValueBehaviour;
import com.simibubi.create.foundation.utility.Iterate;
import com.simibubi.create.foundation.utility.Lang;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.Direction.AxisDirection;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;

public class ChassisTileEntity extends SmartTileEntity {

	ScrollValueBehaviour range;

	public ChassisTileEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
	}

	@Override
	public void addBehaviours(List<TileEntityBehaviour> behaviours) {
		int max = AllConfigs.SERVER.kinetics.maxChassisRange.get();
		range = new BulkScrollValueBehaviour(Lang.translateDirect("generic.range"), this, new CenteredSideValueBoxTransform(),
				te -> ((ChassisTileEntity) te).collectChassisGroup());
		range.requiresWrench();
		range.between(1, max);
		range
				.withClientCallback(
						i -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ChassisRangeDisplay.display(this)));
		range.value = max / 2;
		behaviours.add(range);
	}

	@Override
	public void initialize() {
		super.initialize();
		if (getBlockState().getBlock() instanceof RadialChassisBlock)
			range.setLabel(Lang.translateDirect("generic.radius"));
	}

	public int getRange() {
		return range.getValue();
	}

	public List<BlockPos> getIncludedBlockPositions(Direction forcedMovement, boolean visualize) {
		if (!(getBlockState().getBlock() instanceof AbstractChassisBlock))
			return Collections.emptyList();
		return isRadial() ? getIncludedBlockPositionsRadial(forcedMovement, visualize)
				: getIncludedBlockPositionsLinear(forcedMovement, visualize);
	}

	protected boolean isRadial() {
		return level.getBlockState(worldPosition).getBlock() instanceof RadialChassisBlock;
	}

	public List<ChassisTileEntity> collectChassisGroup() {
		Queue<BlockPos> frontier = new LinkedList<>();
		List<ChassisTileEntity> collected = new ArrayList<>();
		Set<BlockPos> visited = new HashSet<>();
		frontier.add(worldPosition);
		while (!frontier.isEmpty()) {
			BlockPos current = frontier.poll();
			if (visited.contains(current))
				continue;
			visited.add(current);
			BlockEntity tileEntity = level.getBlockEntity(current);
			if (tileEntity instanceof ChassisTileEntity) {
				ChassisTileEntity chassis = (ChassisTileEntity) tileEntity;
				collected.add(chassis);
				visited.add(current);
				chassis.addAttachedChasses(frontier, visited);
			}
		}
		return collected;
	}

	public boolean addAttachedChasses(Queue<BlockPos> frontier, Set<BlockPos> visited) {
		BlockState state = getBlockState();
		if (!(state.getBlock() instanceof AbstractChassisBlock))
			return false;
		Axis axis = state.getValue(AbstractChassisBlock.AXIS);
		if (isRadial()) {

			// Collect chain of radial chassis
			for (int offset : new int[] { -1, 1 }) {
				Direction direction = Direction.get(AxisDirection.POSITIVE, axis);
				BlockPos currentPos = worldPosition.relative(direction, offset);
				if (!level.isLoaded(currentPos))
					return false;

				BlockState neighbourState = level.getBlockState(currentPos);
				if (!AllBlocks.RADIAL_CHASSIS.has(neighbourState))
					continue;
				if (axis != neighbourState.getValue(BlockStateProperties.AXIS))
					continue;
				if (!visited.contains(currentPos))
					frontier.add(currentPos);
			}

			return true;
		}

		// Collect group of connected linear chassis
		for (Direction offset : Iterate.directions) {
			BlockPos current = worldPosition.relative(offset);
			if (visited.contains(current))
				continue;
			if (!level.isLoaded(current))
				return false;

			BlockState neighbourState = level.getBlockState(current);
			if (!LinearChassisBlock.isChassis(neighbourState))
				continue;
			if (!LinearChassisBlock.sameKind(state, neighbourState))
				continue;
			if (neighbourState.getValue(LinearChassisBlock.AXIS) != axis)
				continue;

			frontier.add(current);
		}

		return true;
	}

	private List<BlockPos> getIncludedBlockPositionsLinear(Direction forcedMovement, boolean visualize) {
		List<BlockPos> positions = new ArrayList<>();
		BlockState state = getBlockState();
		AbstractChassisBlock block = (AbstractChassisBlock) state.getBlock();
		Axis axis = state.getValue(AbstractChassisBlock.AXIS);
		Direction facing = Direction.get(AxisDirection.POSITIVE, axis);
		int chassisRange = visualize ? range.scrollableValue : getRange();

		for (int offset : new int[] { 1, -1 }) {
			if (offset == -1)
				facing = facing.getOpposite();
			boolean sticky = state.getValue(block.getGlueableSide(state, facing));
			for (int i = 1; i <= chassisRange; i++) {
				BlockPos current = worldPosition.relative(facing, i);
				BlockState currentState = level.getBlockState(current);

				if (forcedMovement != facing && !sticky)
					break;

				// Ignore replaceable Blocks and Air-like
				if (!BlockMovementChecks.isMovementNecessary(currentState, level, current))
					break;
				if (BlockMovementChecks.isBrittle(currentState))
					break;

				positions.add(current);

				if (BlockMovementChecks.isNotSupportive(currentState, facing))
					break;
			}
		}

		return positions;
	}

	private List<BlockPos> getIncludedBlockPositionsRadial(Direction forcedMovement, boolean visualize) {
		List<BlockPos> positions = new ArrayList<>();
		BlockState state = level.getBlockState(worldPosition);
		Axis axis = state.getValue(AbstractChassisBlock.AXIS);
		AbstractChassisBlock block = (AbstractChassisBlock) state.getBlock();
		int chassisRange = visualize ? range.scrollableValue : getRange();

		for (Direction facing : Iterate.directions) {
			if (facing.getAxis() == axis)
				continue;
			if (!state.getValue(block.getGlueableSide(state, facing)))
				continue;

			BlockPos startPos = worldPosition.relative(facing);
			List<BlockPos> localFrontier = new LinkedList<>();
			Set<BlockPos> localVisited = new HashSet<>();
			localFrontier.add(startPos);

			while (!localFrontier.isEmpty()) {
				BlockPos searchPos = localFrontier.remove(0);
				BlockState searchedState = level.getBlockState(searchPos);

				if (localVisited.contains(searchPos))
					continue;
				if (!searchPos.closerThan(worldPosition, chassisRange + .5f))
					continue;
				if (!BlockMovementChecks.isMovementNecessary(searchedState, level, searchPos))
					continue;
				if (BlockMovementChecks.isBrittle(searchedState))
					continue;

				localVisited.add(searchPos);
				if (!searchPos.equals(worldPosition))
					positions.add(searchPos);

				for (Direction offset : Iterate.directions) {
					if (offset.getAxis() == axis)
						continue;
					if (searchPos.equals(worldPosition) && offset != facing)
						continue;
					if (BlockMovementChecks.isNotSupportive(searchedState, offset))
						continue;

					localFrontier.add(searchPos.relative(offset));
				}
			}
		}

		return positions;
	}

}
