package com.simibubi.create.content.trains.signal;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import com.google.common.base.Objects;
import com.simibubi.create.Create;
import com.simibubi.create.content.trains.graph.DimensionPalette;
import com.simibubi.create.content.trains.graph.EdgePointType;
import com.simibubi.create.content.trains.graph.TrackGraph;
import com.simibubi.create.content.trains.graph.TrackNode;
import com.simibubi.create.content.trains.signal.SignalBlock.SignalType;
import com.simibubi.create.content.trains.signal.SignalBlockEntity.OverlayState;
import com.simibubi.create.content.trains.signal.SignalBlockEntity.SignalState;
import com.simibubi.create.foundation.utility.Couple;
import com.simibubi.create.foundation.utility.Iterate;
import com.simibubi.create.foundation.utility.NBTHelper;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;

public class SignalBoundary extends TrackEdgePoint {

	public Couple<Map<BlockPos, Boolean>> blockEntities;
	public Couple<SignalType> types;
	public Couple<UUID> groups;
	public Couple<Boolean> sidesToUpdate;
	public Couple<SignalState> cachedStates;

	private Couple<Map<UUID, Boolean>> chainedSignals;

	public SignalBoundary() {
		blockEntities = Couple.create(HashMap::new);
		chainedSignals = Couple.create(null, null);
		groups = Couple.create(null, null);
		sidesToUpdate = Couple.create(true, true);
		types = Couple.create(() -> SignalType.ENTRY_SIGNAL);
		cachedStates = Couple.create(() -> SignalState.INVALID);
	}

	public void setGroup(boolean primary, UUID groupId) {
		UUID previous = groups.get(primary);

		groups.set(primary, groupId);

		UUID opposite = groups.get(!primary);
		Map<UUID, SignalEdgeGroup> signalEdgeGroups = Create.RAILWAYS.signalEdgeGroups;

		if (opposite != null && signalEdgeGroups.containsKey(opposite)) {
			SignalEdgeGroup oppositeGroup = signalEdgeGroups.get(opposite);
			if (previous != null)
				oppositeGroup.removeAdjacent(previous);
			if (groupId != null)
				oppositeGroup.putAdjacent(groupId);
		}

		if (groupId != null && signalEdgeGroups.containsKey(groupId)) {
			SignalEdgeGroup group = signalEdgeGroups.get(groupId);
			if (opposite != null)
				group.putAdjacent(opposite);
		}
	}

	public void setGroupAndUpdate(TrackNode side, UUID groupId) {
		boolean primary = isPrimary(side);
		setGroup(primary, groupId);
		sidesToUpdate.set(primary, false);
		chainedSignals.set(primary, null);
	}

	@Override
	public boolean canMerge() {
		return true;
	}

	@Override
	public void invalidate(LevelAccessor level) {
		blockEntities.forEach(s -> s.keySet()
			.forEach(p -> invalidateAt(level, p)));
		groups.forEach(uuid -> {
			if (Create.RAILWAYS.signalEdgeGroups.remove(uuid) != null)
				Create.RAILWAYS.sync.edgeGroupRemoved(uuid);
		});
	}

	@Override
	public boolean canCoexistWith(EdgePointType<?> otherType, boolean front) {
		return otherType == getType();
	}

	@Override
	public void blockEntityAdded(BlockEntity blockEntity, boolean front) {
		Map<BlockPos, Boolean> blockEntitiesOnSide = blockEntities.get(front);
		if (blockEntitiesOnSide.isEmpty())
			blockEntity.getBlockState()
				.getOptionalValue(SignalBlock.TYPE)
				.ifPresent(type -> types.set(front, type));
		blockEntitiesOnSide.put(blockEntity.getBlockPos(), blockEntity instanceof SignalBlockEntity ste && ste.getReportedPower());
	}

	public void updateBlockEntityPower(SignalBlockEntity blockEntity) {
		for (boolean front : Iterate.trueAndFalse)
			blockEntities.get(front)
				.computeIfPresent(blockEntity.getBlockPos(), (p, c) -> blockEntity.getReportedPower());
	}

	@Override
	public void blockEntityRemoved(BlockPos blockEntityPos, boolean front) {
		blockEntities.forEach(s -> s.remove(blockEntityPos));
		if (blockEntities.both(Map::isEmpty))
			removeFromAllGraphs();
	}

	@Override
	public void onRemoved(TrackGraph graph) {
		super.onRemoved(graph);
		SignalPropagator.onSignalRemoved(graph, this);
	}

	public void queueUpdate(TrackNode side) {
		sidesToUpdate.set(isPrimary(side), true);
	}

	public UUID getGroup(TrackNode side) {
		return groups.get(isPrimary(side));
	}

	@Override
	public boolean canNavigateVia(TrackNode side) {
		return !blockEntities.get(isPrimary(side))
			.isEmpty();
	}

	public OverlayState getOverlayFor(BlockPos blockEntity) {
		for (boolean first : Iterate.trueAndFalse) {
			Map<BlockPos, Boolean> set = blockEntities.get(first);
			for (BlockPos blockPos : set.keySet()) {
				if (blockPos.equals(blockEntity))
					return blockEntities.get(!first)
						.isEmpty() ? OverlayState.RENDER : OverlayState.DUAL;
				return OverlayState.SKIP;
			}
		}
		return OverlayState.SKIP;
	}

	public SignalType getTypeFor(BlockPos blockEntity) {
		return types.get(blockEntities.getFirst()
			.containsKey(blockEntity));
	}

	public SignalState getStateFor(BlockPos blockEntity) {
		for (boolean first : Iterate.trueAndFalse) {
			Map<BlockPos, Boolean> set = blockEntities.get(first);
			if (set.containsKey(blockEntity))
				return cachedStates.get(first);
		}
		return SignalState.INVALID;
	}

	@Override
	public void tick(TrackGraph graph, boolean preTrains) {
		super.tick(graph, preTrains);
		if (!preTrains) {
			tickState(graph);
			return;
		}
		for (boolean front : Iterate.trueAndFalse) {
			if (!sidesToUpdate.get(front))
				continue;
			sidesToUpdate.set(front, false);
			SignalPropagator.propagateSignalGroup(graph, this, front);
			chainedSignals.set(front, null);
		}
	}

	private void tickState(TrackGraph graph) {
		for (boolean current : Iterate.trueAndFalse) {
			Map<BlockPos, Boolean> set = blockEntities.get(current);
			if (set.isEmpty())
				continue;

			boolean forcedRed = isForcedRed(current);
			UUID group = groups.get(current);
			if (Objects.equal(group, groups.get(!current))) {
				cachedStates.set(current, SignalState.INVALID);
				continue;
			}

			Map<UUID, SignalEdgeGroup> signalEdgeGroups = Create.RAILWAYS.signalEdgeGroups;
			SignalEdgeGroup signalEdgeGroup = signalEdgeGroups.get(group);
			if (signalEdgeGroup == null) {
				cachedStates.set(current, SignalState.INVALID);
				continue;
			}

			boolean occupiedUnlessBySelf = forcedRed || signalEdgeGroup.isOccupiedUnless(this);
			cachedStates.set(current, occupiedUnlessBySelf ? SignalState.RED : resolveSignalChain(graph, current));
		}
	}

	public boolean isForcedRed(TrackNode side) {
		return isForcedRed(isPrimary(side));
	}

	public boolean isForcedRed(boolean primary) {
		Collection<Boolean> values = blockEntities.get(primary)
			.values();
		for (Boolean b : values)
			if (b)
				return true;
		return false;
	}

	private SignalState resolveSignalChain(TrackGraph graph, boolean side) {
		if (types.get(side) != SignalType.CROSS_SIGNAL)
			return SignalState.GREEN;

		if (chainedSignals.get(side) == null)
			chainedSignals.set(side, SignalPropagator.collectChainedSignals(graph, this, side));

		boolean allPathsFree = true;
		boolean noPathsFree = true;
		boolean invalid = false;

		for (Entry<UUID, Boolean> entry : chainedSignals.get(side)
			.entrySet()) {
			UUID uuid = entry.getKey();
			boolean sideOfOther = entry.getValue();
			SignalBoundary otherSignal = graph.getPoint(EdgePointType.SIGNAL, uuid);
			if (otherSignal == null) {
				invalid = true;
				break;
			}
			if (otherSignal.blockEntities.get(sideOfOther)
				.isEmpty())
				continue;
			SignalState otherState = otherSignal.cachedStates.get(sideOfOther);
			allPathsFree &= otherState == SignalState.GREEN || otherState == SignalState.INVALID;
			noPathsFree &= otherState == SignalState.RED;
		}
		if (invalid) {
			chainedSignals.set(side, null);
			return SignalState.INVALID;
		}
		if (allPathsFree)
			return SignalState.GREEN;
		if (noPathsFree)
			return SignalState.RED;
		return SignalState.YELLOW;
	}

	@Override
	public void read(CompoundTag nbt, boolean migration, DimensionPalette dimensions) {
		super.read(nbt, migration, dimensions);

		if (migration)
			return;

		blockEntities = Couple.create(HashMap::new);
		groups = Couple.create(null, null);

		for (int i = 1; i <= 2; i++)
			if (nbt.contains("Tiles" + i)) {
				boolean first = i == 1;
				NBTHelper.iterateCompoundList(nbt.getList("Tiles" + i, Tag.TAG_COMPOUND), c -> blockEntities.get(first)
					.put(NbtUtils.readBlockPos(c), c.getBoolean("Power")));
			}

		for (int i = 1; i <= 2; i++)
			if (nbt.contains("Group" + i))
				groups.set(i == 1, nbt.getUUID("Group" + i));
		for (int i = 1; i <= 2; i++)
			sidesToUpdate.set(i == 1, nbt.contains("Update" + i));
		for (int i = 1; i <= 2; i++)
			types.set(i == 1, NBTHelper.readEnum(nbt, "Type" + i, SignalType.class));
		for (int i = 1; i <= 2; i++)
			cachedStates.set(i == 1, NBTHelper.readEnum(nbt, "State" + i, SignalState.class));
	}

	@Override
	public void read(FriendlyByteBuf buffer, DimensionPalette dimensions) {
		super.read(buffer, dimensions);
		for (int i = 1; i <= 2; i++) {
			if (buffer.readBoolean())
				groups.set(i == 1, buffer.readUUID());
		}
	}

	@Override
	public void write(CompoundTag nbt, DimensionPalette dimensions) {
		super.write(nbt, dimensions);
		for (int i = 1; i <= 2; i++)
			if (!blockEntities.get(i == 1)
				.isEmpty())
				nbt.put("Tiles" + i, NBTHelper.writeCompoundList(blockEntities.get(i == 1)
					.entrySet(), e -> {
						CompoundTag c = NbtUtils.writeBlockPos(e.getKey());
						c.putBoolean("Power", e.getValue());
						return c;
					}));
		for (int i = 1; i <= 2; i++)
			if (groups.get(i == 1) != null)
				nbt.putUUID("Group" + i, groups.get(i == 1));
		for (int i = 1; i <= 2; i++)
			if (sidesToUpdate.get(i == 1))
				nbt.putBoolean("Update" + i, true);
		for (int i = 1; i <= 2; i++)
			NBTHelper.writeEnum(nbt, "Type" + i, types.get(i == 1));
		for (int i = 1; i <= 2; i++)
			NBTHelper.writeEnum(nbt, "State" + i, cachedStates.get(i == 1));
	}

	@Override
	public void write(FriendlyByteBuf buffer, DimensionPalette dimensions) {
		super.write(buffer, dimensions);
		for (int i = 1; i <= 2; i++) {
			boolean hasGroup = groups.get(i == 1) != null;
			buffer.writeBoolean(hasGroup);
			if (hasGroup)
				buffer.writeUUID(groups.get(i == 1));
		}
	}

	public void cycleSignalType(BlockPos pos) {
		types.set(blockEntities.getFirst()
			.containsKey(pos), SignalType.values()[(getTypeFor(pos).ordinal() + 1) % SignalType.values().length]);
	}

}
