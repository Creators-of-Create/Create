package com.simibubi.create.content.logistics.trains.management;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import com.simibubi.create.Create;
import com.simibubi.create.content.contraptions.components.structureMovement.AssemblyException;
import com.simibubi.create.content.logistics.trains.IBogeyBlock;
import com.simibubi.create.content.logistics.trains.ITrackBlock;
import com.simibubi.create.content.logistics.trains.TrackEdge;
import com.simibubi.create.content.logistics.trains.TrackGraph;
import com.simibubi.create.content.logistics.trains.TrackNode;
import com.simibubi.create.content.logistics.trains.TrackNodeLocation.DiscoveredLocation;
import com.simibubi.create.content.logistics.trains.TrackPropagator;
import com.simibubi.create.content.logistics.trains.entity.Carriage;
import com.simibubi.create.content.logistics.trains.entity.Carriage.CarriageBogey;
import com.simibubi.create.content.logistics.trains.entity.CarriageContraption;
import com.simibubi.create.content.logistics.trains.entity.Train;
import com.simibubi.create.content.logistics.trains.entity.TravellingPoint;
import com.simibubi.create.foundation.tileEntity.SmartTileEntity;
import com.simibubi.create.foundation.tileEntity.TileEntityBehaviour;
import com.simibubi.create.foundation.utility.Iterate;
import com.simibubi.create.foundation.utility.Lang;
import com.simibubi.create.foundation.utility.Pair;
import com.simibubi.create.foundation.utility.WorldAttached;

import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.Direction.AxisDirection;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class StationTileEntity extends SmartTileEntity {

	UUID id;

	protected int failedCarriageIndex;
	protected AssemblyException lastException;
	protected CompoundTag toMigrate;

	public StationTileEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
		setLazyTickRate(20);
		id = UUID.randomUUID();
		lastException = null;
		toMigrate = null;
		failedCarriageIndex = -1;
	}

	public void migrate(GlobalStation globalStation) {
		if (toMigrate != null)
			return;
		toMigrate = globalStation.write();
		setChanged();
	}

	@Override
	public void addBehaviours(List<TileEntityBehaviour> behaviours) {
		behaviours.add(new TrackTargetingBehaviour(this));
	}

	public TrackTargetingBehaviour getTarget() {
		return getBehaviour(TrackTargetingBehaviour.TYPE);
	}

	@Override
	public void initialize() {
		if (!level.isClientSide)
			getOrCreateGlobalStation();
		super.initialize();
	}

	public GlobalStation getOrCreateGlobalStation() {
		for (TrackGraph trackGraph : Create.RAILWAYS.trackNetworks.values()) { // TODO thread breach
			GlobalStation station = trackGraph.getStation(id);
			if (station == null)
				continue;
			return station;
		}

		if (level.isClientSide)
			return null;

		TrackTargetingBehaviour target = getTarget();
		if (!target.hasValidTrack())
			return null;
		GraphLocation loc = target.determineGraphLocation();
		if (loc == null)
			return null;

		GlobalStation globalStation =
			toMigrate != null ? new GlobalStation(toMigrate) : new GlobalStation(id, worldPosition);
		globalStation.setLocation(loc.edge, loc.position);
		loc.graph.addStation(globalStation);
		toMigrate = null;
		setChanged();

		return globalStation;
	}

	@Override
	protected void read(CompoundTag tag, boolean clientPacket) {
		id = tag.getUUID("Id");
		lastException = AssemblyException.read(tag);
		failedCarriageIndex = tag.getInt("FailedCarriageIndex");
		super.read(tag, clientPacket);
		if (tag.contains("ToMigrate"))
			toMigrate = tag.getCompound("ToMigrate");
		renderBounds = null;
	}

	@Override
	protected void write(CompoundTag tag, boolean clientPacket) {
		tag.putUUID("Id", id);
		AssemblyException.write(tag, lastException);
		tag.putInt("FailedCarriageIndex", failedCarriageIndex);
		super.write(tag, clientPacket);
		if (!clientPacket && toMigrate != null)
			tag.put("ToMigrate", toMigrate);
	}

	// Train Assembly

	public static WorldAttached<Map<BlockPos, BoundingBox>> assemblyAreas = new WorldAttached<>(w -> new HashMap<>());

	Direction assemblyDirection;
	int assemblyLength;
	int[] bogeyLocations;
	IBogeyBlock[] bogeyTypes;
	int bogeyCount;

	@Override
	public void lazyTick() {
		if (isAssembling() && !level.isClientSide)
			refreshAssemblyInfo();
		if (!level.isClientSide)
			getOrCreateGlobalStation();
		super.lazyTick();
	}

	@Override
	public void tick() {
		if (isAssembling() && level.isClientSide)
			refreshAssemblyInfo();
		super.tick();

		if (level.isClientSide)
			return;
		if (toMigrate == null)
			return;
		getOrCreateGlobalStation();
	}

	public void trackClicked(Player player, ITrackBlock track, BlockState state, BlockPos pos) {
		refreshAssemblyInfo();
		BoundingBox bb = assemblyAreas.get(level)
			.get(worldPosition);
		if (bb == null || !bb.isInside(pos))
			return;

		int bogeyOffset = pos.distManhattan(getTarget().getGlobalPosition()) - 1;
		if (!isValidBogeyOffset(bogeyOffset))
			return;

		Vec3 upNormal = track.getUpNormal(level, pos, state);
		BlockState bogeyAnchor = track.getBogeyAnchor(level, pos, state);
		level.setBlock(pos.offset(new BlockPos(upNormal)), bogeyAnchor, 3);
	}

	public boolean isAssembling() {
		BlockState state = getBlockState();
		return state.hasProperty(StationBlock.ASSEMBLING) && state.getValue(StationBlock.ASSEMBLING);
	}

	public boolean tryEnterAssemblyMode() {
		TrackTargetingBehaviour target = getTarget();
		if (!target.hasValidTrack())
			return false;

		BlockPos targetPosition = target.getGlobalPosition();
		BlockState trackState = target.getTrackBlockState();
		ITrackBlock track = target.getTrack();
		Vec3 trackAxis = track.getTrackAxis(level, targetPosition, trackState);

		boolean axisFound = false;
		for (Axis axis : Iterate.axes) {
			if (trackAxis.get(axis) == 0)
				continue;
			if (axisFound)
				return false;
			axisFound = true;
		}

		return true;
	}

	public void refreshAssemblyInfo() {
		TrackTargetingBehaviour target = getTarget();
		if (!target.hasValidTrack())
			return;

		GlobalStation station = getOrCreateGlobalStation();
		if (station == null || station.getPresentTrain() != null)
			return;

		int prevLength = assemblyLength;
		BlockPos targetPosition = target.getGlobalPosition();
		BlockState trackState = target.getTrackBlockState();
		ITrackBlock track = target.getTrack();
		getAssemblyDirection();

		MutableBlockPos currentPos = targetPosition.mutable();
		currentPos.move(assemblyDirection);

		BlockPos bogeyOffset = new BlockPos(track.getUpNormal(level, targetPosition, trackState));

		int MAX_LENGTH = 48;
		int MAX_BOGEY_COUNT = 20;

		int bogeyIndex = 0;
		int maxBogeyCount = MAX_BOGEY_COUNT;
		if (bogeyLocations == null)
			bogeyLocations = new int[maxBogeyCount];
		if (bogeyTypes == null)
			bogeyTypes = new IBogeyBlock[maxBogeyCount];
		Arrays.fill(bogeyLocations, -1);
		Arrays.fill(bogeyTypes, null);

		for (int i = 0; i < MAX_LENGTH; i++) {
			if (i == MAX_LENGTH - 1 || !track.trackEquals(trackState, level.getBlockState(currentPos))) {
				assemblyLength = i;
				break;
			}

			BlockState potentialBogeyState = level.getBlockState(bogeyOffset.offset(currentPos));
			if (potentialBogeyState.getBlock()instanceof IBogeyBlock bogey && bogeyIndex < bogeyLocations.length) {
				bogeyTypes[bogeyIndex] = bogey;
				bogeyLocations[bogeyIndex] = i;
				bogeyIndex++;
			}

			currentPos.move(assemblyDirection);
		}

		bogeyCount = bogeyIndex;

		if (level.isClientSide)
			return;
		if (prevLength == assemblyLength)
			return;

		Map<BlockPos, BoundingBox> map = assemblyAreas.get(level);
		BlockPos startPosition = targetPosition.relative(assemblyDirection);
		BlockPos trackEnd = startPosition.relative(assemblyDirection, assemblyLength - 1);
		map.put(worldPosition, BoundingBox.fromCorners(startPosition, trackEnd));
	}

	public boolean isValidBogeyOffset(int i) {
		if ((i < 4 || bogeyCount == 0) && i != 0)
			return false;
		for (int j : bogeyLocations) {
			if (j == -1)
				break;
			if (i >= j - 3 && i <= j + 3)
				return false;
		}
		return true;
	}

	public Direction getAssemblyDirection() {
		if (assemblyDirection != null)
			return assemblyDirection;
		TrackTargetingBehaviour target = getTarget();
		if (!target.hasValidTrack())
			return null;
		BlockPos targetPosition = target.getGlobalPosition();
		BlockState trackState = target.getTrackBlockState();
		ITrackBlock track = target.getTrack();
		AxisDirection axisDirection = target.getTargetDirection();
		Vec3 axis = track.getTrackAxis(level, targetPosition, trackState)
			.normalize()
			.scale(axisDirection.getStep());
		return assemblyDirection = Direction.getNearest(axis.x, axis.y, axis.z);
	}

	@Override
	protected void setRemovedNotDueToChunkUnload() {
		assemblyAreas.get(level)
			.remove(worldPosition);
		for (TrackGraph trackGraph : Create.RAILWAYS.trackNetworks.values())
			trackGraph.removeStation(id);
		super.setRemovedNotDueToChunkUnload();
	}

	public void assemble(UUID playerUUID) {
		refreshAssemblyInfo();

		if (bogeyLocations[0] != 0) {
			exception(new AssemblyException(Lang.translate("train_assembly.frontmost_bogey_at_station")), -1);
			return;
		}

		TrackTargetingBehaviour target = getTarget();
		if (!target.hasValidTrack())
			return;

		BlockPos trackPosition = target.getGlobalPosition();
		BlockState trackState = target.getTrackBlockState();
		ITrackBlock track = target.getTrack();
		BlockPos bogeyOffset = new BlockPos(track.getUpNormal(level, trackPosition, trackState));

		DiscoveredLocation location = null;
		List<Pair<BlockPos, DiscoveredLocation>> ends =
			TrackPropagator.getEnds(level, trackPosition, trackState, null, true);
		for (Pair<BlockPos, DiscoveredLocation> pair : ends)
			if (trackPosition.relative(assemblyDirection)
				.equals(pair.getFirst()))
				location = pair.getSecond();
		if (location == null)
			return;

		List<Double> pointOffsets = new ArrayList<>();
		for (int i = 0; i < bogeyLocations.length; i++) {
			int loc = bogeyLocations[i];
			if (loc == -1)
				break;
			double bogeySize = bogeyTypes[i].getWheelPointSpacing();
			pointOffsets.add(Double.valueOf(loc + .5 - bogeySize / 2));
			pointOffsets.add(Double.valueOf(loc + .5 + bogeySize / 2));
		}

		List<TravellingPoint> points = new ArrayList<>();
		Vec3 directionVec = Vec3.atLowerCornerOf(assemblyDirection.getNormal());
		TrackGraph graph = null;
		TrackNode secondNode = null;

		for (int i = 0; i < assemblyLength + 20; i++) {
			if (points.size() == pointOffsets.size())
				break;

			DiscoveredLocation currentLocation = location;
			location = new DiscoveredLocation(location.getLocation()
				.add(directionVec));

			if (graph == null)
				graph = Create.RAILWAYS.getGraph(level, currentLocation);
			if (graph == null)
				continue;
			TrackNode node = graph.locateNode(currentLocation);
			if (node == null)
				continue;

			for (int pointIndex = points.size(); pointIndex < pointOffsets.size(); pointIndex++) {
				double offset = pointOffsets.get(pointIndex);
				if (offset > i)
					break;
				double positionOnEdge = i - offset;

				Map<TrackNode, TrackEdge> connectionsFromNode = graph.getConnectionsFrom(node);

				if (secondNode == null)
					for (Entry<TrackNode, TrackEdge> entry : connectionsFromNode.entrySet()) {
						TrackEdge edge = entry.getValue();
						TrackNode otherNode = entry.getKey();
						if (edge.isTurn())
							continue;
						Vec3 edgeDirection = edge.getDirection(node, otherNode, true);
						if (Mth.equal(edgeDirection.normalize()
							.dot(directionVec), -1d))
							secondNode = otherNode;
					}

				if (secondNode == null) {
					Create.LOGGER.warn("Cannot assemble: No valid starting node found");
					return;
				}

				TrackEdge edge = connectionsFromNode.get(secondNode);

				if (edge == null) {
					Create.LOGGER.warn("Cannot assemble: Missing graph edge");
					return;
				}

				points.add(new TravellingPoint(node, secondNode, edge, positionOnEdge));
			}

			secondNode = node;
		}

		if (points.size() != pointOffsets.size()) {
			Create.LOGGER.warn("Cannot assemble: Not all Points created");
			return;
		}

		if (points.size() == 0) {
			exception(new AssemblyException(Lang.translate("train_assembly.no_bogeys")), -1);
			return;
		}

		List<CarriageContraption> contraptions = new ArrayList<>();
		List<Carriage> carriages = new ArrayList<>();
		List<Integer> spacing = new ArrayList<>();

		for (int bogeyIndex = 0; bogeyIndex < bogeyCount; bogeyIndex++) {
			int pointIndex = bogeyIndex * 2;
			if (bogeyIndex > 0)
				spacing.add(bogeyLocations[bogeyIndex] - bogeyLocations[bogeyIndex - 1]);
			CarriageContraption contraption = new CarriageContraption(assemblyDirection);
			BlockPos bogeyPosOffset = trackPosition.offset(bogeyOffset);

			try {
				boolean success = contraption.assemble(level,
					bogeyPosOffset.relative(assemblyDirection, bogeyLocations[bogeyIndex] + 1));
				if (!success) {
					exception(new AssemblyException(Lang.translate("train_assembly.nothing_attached", bogeyIndex + 1)),
						-1);
					return;
				}
			} catch (AssemblyException e) {
				exception(e, contraptions.size() + 1);
				return;
			}

			IBogeyBlock typeOfFirstBogey = bogeyTypes[bogeyIndex];
			CarriageBogey firstBogey =
				new CarriageBogey(typeOfFirstBogey, points.get(pointIndex), points.get(pointIndex + 1));
			CarriageBogey secondBogey = null;
			BlockPos secondBogeyPos = contraption.getSecondBogeyPos();
			int bogeySpacing = 0;

			if (secondBogeyPos != null) {
				if (bogeyIndex == bogeyCount - 1 || !secondBogeyPos
					.equals(bogeyPosOffset.relative(assemblyDirection, bogeyLocations[bogeyIndex + 1] + 1))) {
					exception(new AssemblyException(Lang.translate("train_assembly.not_connected_in_order")),
						contraptions.size() + 1);
					return;
				}

				bogeySpacing = bogeyLocations[bogeyIndex + 1] - bogeyLocations[bogeyIndex];
				secondBogey = new CarriageBogey(bogeyTypes[bogeyIndex + 1], points.get(pointIndex + 2),
					points.get(pointIndex + 3));
				bogeyIndex++;

			} else if (!typeOfFirstBogey.allowsSingleBogeyCarriage()) {
				exception(new AssemblyException(Lang.translate("train_assembly.single_bogey_carriage")),
					contraptions.size() + 1);
				return;
			}

			contraptions.add(contraption);
			Carriage carriage = new Carriage(firstBogey, secondBogey, bogeySpacing);
			carriage.setContraption(contraption);
			carriages.add(carriage);
		}

		for (CarriageContraption contraption : contraptions) {
			contraption.removeBlocksFromWorld(level, BlockPos.ZERO);
			contraption.expandBoundsAroundAxis(Axis.Y);
		}

		Train train = new Train(UUID.randomUUID(), playerUUID, graph, carriages, spacing);
		GlobalStation station = getOrCreateGlobalStation();
		train.setCurrentStation(station);
		station.reserveFor(train);

		Create.RAILWAYS.trains.put(train.id, train);
		clearException();
	}

	public void cancelAssembly() {
		assemblyLength = 0;
		assemblyAreas.get(level)
			.remove(worldPosition);
		clearException();
	}

	private void clearException() {
		exception(null, -1);
	}

	private void exception(AssemblyException exception, int carriage) {
		failedCarriageIndex = carriage;
		lastException = exception;
		sendData();
	}

	// Render

	private AABB renderBounds = null;

	@Override
	public AABB getRenderBoundingBox() {
		if (isAssembling())
			return INFINITE_EXTENT_AABB;
		if (renderBounds == null)
			renderBounds = new AABB(worldPosition, getTarget().getGlobalPosition()).inflate(2);
		return renderBounds;
	}

}
