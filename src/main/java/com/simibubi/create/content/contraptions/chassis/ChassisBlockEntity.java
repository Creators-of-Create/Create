package com.simibubi.create.content.contraptions.chassis;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.function.Function;

import com.google.common.collect.ImmutableList;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllKeys;
import com.simibubi.create.content.contraptions.BlockMovementChecks;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.CenteredSideValueBoxTransform;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueBoxTransform;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueSettingsBoard;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueSettingsFormatter;
import com.simibubi.create.foundation.blockEntity.behaviour.scrollValue.BulkScrollValueBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.scrollValue.ScrollValueBehaviour;
import com.simibubi.create.foundation.utility.Iterate;
import com.simibubi.create.foundation.utility.Lang;
import com.simibubi.create.infrastructure.config.AllConfigs;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.Direction.AxisDirection;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.DistExecutor;

public class ChassisBlockEntity extends SmartBlockEntity {

	ScrollValueBehaviour range;

	public int currentlySelectedRange;

	public ChassisBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
	}

	@Override
	public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
		int max = AllConfigs.server().kinetics.maxChassisRange.get();
		range = new ChassisScrollValueBehaviour(Lang.translateDirect("contraptions.chassis.range"), this,
			new CenteredSideValueBoxTransform(), be -> ((ChassisBlockEntity) be).collectChassisGroup());
		range.requiresWrench();
		range.between(1, max);
		range.withClientCallback(
			i -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ChassisRangeDisplay.display(this)));
		range.setValue(max / 2);
		range.withFormatter(s -> String.valueOf(currentlySelectedRange));
		behaviours.add(range);
		currentlySelectedRange = range.getValue();
	}

	@Override
	public void initialize() {
		super.initialize();
		if (getBlockState().getBlock() instanceof RadialChassisBlock)
			range.setLabel(Lang.translateDirect("contraptions.chassis.radius"));
	}

	@Override
	protected void read(CompoundTag tag, boolean clientPacket) {
		super.read(tag, clientPacket);
		if (clientPacket)
			currentlySelectedRange = getRange();
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
		return level.getBlockState(worldPosition)
			.getBlock() instanceof RadialChassisBlock;
	}

	public List<ChassisBlockEntity> collectChassisGroup() {
		Queue<BlockPos> frontier = new LinkedList<>();
		List<ChassisBlockEntity> collected = new ArrayList<>();
		Set<BlockPos> visited = new HashSet<>();
		frontier.add(worldPosition);
		while (!frontier.isEmpty()) {
			BlockPos current = frontier.poll();
			if (visited.contains(current))
				continue;
			visited.add(current);
			BlockEntity blockEntity = level.getBlockEntity(current);
			if (blockEntity instanceof ChassisBlockEntity) {
				ChassisBlockEntity chassis = (ChassisBlockEntity) blockEntity;
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
		int chassisRange = visualize ? currentlySelectedRange : getRange();

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
		int chassisRange = visualize ? currentlySelectedRange : getRange();

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

	class ChassisScrollValueBehaviour extends BulkScrollValueBehaviour {

		public ChassisScrollValueBehaviour(Component label, SmartBlockEntity be, ValueBoxTransform slot,
			Function<SmartBlockEntity, List<? extends SmartBlockEntity>> groupGetter) {
			super(label, be, slot, groupGetter);
		}

		@Override
		public ValueSettingsBoard createBoard(Player player, BlockHitResult hitResult) {
			ImmutableList<Component> rows = ImmutableList.of(Lang.translateDirect("contraptions.chassis.distance"));
			ValueSettingsFormatter formatter =
				new ValueSettingsFormatter(vs -> new ValueSettings(vs.row(), vs.value() + 1).format());
			return new ValueSettingsBoard(label, max - 1, 1, rows, formatter);
		}

		@Override
		@OnlyIn(Dist.CLIENT)
		public void newSettingHovered(ValueSettings valueSetting) {
			if (!level.isClientSide)
				return;
			if (!AllKeys.ctrlDown())
				currentlySelectedRange = valueSetting.value() + 1;
			else
				for (SmartBlockEntity be : getBulk())
					if (be instanceof ChassisBlockEntity cbe)
						cbe.currentlySelectedRange = valueSetting.value() + 1;
			ChassisRangeDisplay.display(ChassisBlockEntity.this);
		}

		@Override
		public void setValueSettings(Player player, ValueSettings vs, boolean ctrlHeld) {
			super.setValueSettings(player, new ValueSettings(vs.row(), vs.value() + 1), ctrlHeld);
		}

		@Override
		public ValueSettings getValueSettings() {
			ValueSettings vs = super.getValueSettings();
			return new ValueSettings(vs.row(), vs.value() - 1);
		}
		
		@Override
		public String getClipboardKey() {
			return "Chassis";
		}

	}

}
