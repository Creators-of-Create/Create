package com.simibubi.create.content.logistics.trains.management.edgePoint.station;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.UUID;

import javax.annotation.Nullable;

import com.simibubi.create.Create;
import com.simibubi.create.content.contraptions.components.structureMovement.AssemblyException;
import com.simibubi.create.content.logistics.trains.IBogeyBlock;
import com.simibubi.create.content.logistics.trains.ITrackBlock;
import com.simibubi.create.content.logistics.trains.TrackEdge;
import com.simibubi.create.content.logistics.trains.TrackGraph;
import com.simibubi.create.content.logistics.trains.TrackNode;
import com.simibubi.create.content.logistics.trains.TrackNodeLocation.DiscoveredLocation;
import com.simibubi.create.content.logistics.trains.entity.Carriage;
import com.simibubi.create.content.logistics.trains.entity.CarriageBogey;
import com.simibubi.create.content.logistics.trains.entity.CarriageContraption;
import com.simibubi.create.content.logistics.trains.entity.Train;
import com.simibubi.create.content.logistics.trains.entity.TrainPacket;
import com.simibubi.create.content.logistics.trains.entity.TravellingPoint;
import com.simibubi.create.content.logistics.trains.management.edgePoint.EdgePointType;
import com.simibubi.create.content.logistics.trains.management.edgePoint.TrackTargetingBehaviour;
import com.simibubi.create.foundation.networking.AllPackets;
import com.simibubi.create.foundation.tileEntity.SmartTileEntity;
import com.simibubi.create.foundation.tileEntity.TileEntityBehaviour;
import com.simibubi.create.foundation.utility.Iterate;
import com.simibubi.create.foundation.utility.Lang;
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
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.PacketDistributor;

public class StationTileEntity extends SmartTileEntity {

	public TrackTargetingBehaviour<GlobalStation> edgePoint;

	protected int failedCarriageIndex;
	protected AssemblyException lastException;

	// for display
	UUID imminentTrain;
	boolean trainPresent;
	boolean trainBackwards;
	boolean trainCanDisassemble;

	public StationTileEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
		setLazyTickRate(20);
		lastException = null;
		failedCarriageIndex = -1;
	}

	@Override
	public void addBehaviours(List<TileEntityBehaviour> behaviours) {
		edgePoint = new TrackTargetingBehaviour<>(this, EdgePointType.STATION);
		behaviours.add(edgePoint);
	}

	@Override
	protected void read(CompoundTag tag, boolean clientPacket) {
		lastException = AssemblyException.read(tag);
		failedCarriageIndex = tag.getInt("FailedCarriageIndex");
		super.read(tag, clientPacket);
		invalidateRenderBoundingBox();

		if (!clientPacket)
			return;
		if (!tag.contains("ImminentTrain")) {
			imminentTrain = null;
			trainPresent = false;
			trainCanDisassemble = false;
			trainBackwards = false;
			return;
		}

		imminentTrain = tag.getUUID("ImminentTrain");
		trainPresent = tag.getBoolean("TrainPresent");
		trainCanDisassemble = tag.getBoolean("TrainCanDisassemble");
		trainBackwards = tag.getBoolean("TrainBackwards");
	}

	@Override
	protected void write(CompoundTag tag, boolean clientPacket) {
		AssemblyException.write(tag, lastException);
		tag.putInt("FailedCarriageIndex", failedCarriageIndex);
		super.write(tag, clientPacket);

		if (!clientPacket)
			return;
		if (imminentTrain == null)
			return;

		tag.putUUID("ImminentTrain", imminentTrain);
		tag.putBoolean("TrainPresent", trainPresent);
		tag.putBoolean("TrainCanDisassemble", trainCanDisassemble);
		tag.putBoolean("TrainBackwards", trainBackwards);
	}

	@Nullable
	public GlobalStation getStation() {
		return edgePoint.getEdgePoint();
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
		super.lazyTick();
	}

	@Override
	public void tick() {
		if (isAssembling() && level.isClientSide)
			refreshAssemblyInfo();
		super.tick();

		if (level.isClientSide)
			return;

		GlobalStation station = getStation();
		if (station == null)
			return;

		Train imminentTrain = station.getImminentTrain();
		boolean trainPresent = imminentTrain != null && imminentTrain.getCurrentStation() == station;
		boolean canDisassemble = trainPresent && imminentTrain.canDisassemble();
		UUID imminentID = imminentTrain != null ? imminentTrain.id : null;

		if (this.trainPresent != trainPresent || this.trainCanDisassemble != canDisassemble
			|| !Objects.equals(imminentID, this.imminentTrain)) {
			this.imminentTrain = imminentID;
			this.trainPresent = trainPresent;
			this.trainCanDisassemble = canDisassemble;
			this.trainBackwards = imminentTrain != null && imminentTrain.currentlyBackwards;
			sendData();
		}
	}

	public void trackClicked(Player player, ITrackBlock track, BlockState state, BlockPos pos) {
		refreshAssemblyInfo();
		BoundingBox bb = assemblyAreas.get(level)
			.get(worldPosition);
		if (bb == null || !bb.isInside(pos))
			return;

		int bogeyOffset = pos.distManhattan(edgePoint.getGlobalPosition()) - 1;
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
		if (!edgePoint.hasValidTrack())
			return false;

		BlockPos targetPosition = edgePoint.getGlobalPosition();
		BlockState trackState = edgePoint.getTrackBlockState();
		ITrackBlock track = edgePoint.getTrack();
		Vec3 trackAxis = track.getTrackAxes(level, targetPosition, trackState)
			.get(0);

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
		if (!edgePoint.hasValidTrack())
			return;

		GlobalStation station = getStation();
		if (station == null || station.getPresentTrain() != null)
			return;

		int prevLength = assemblyLength;
		BlockPos targetPosition = edgePoint.getGlobalPosition();
		BlockState trackState = edgePoint.getTrackBlockState();
		ITrackBlock track = edgePoint.getTrack();
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
		if (!edgePoint.hasValidTrack())
			return null;
		BlockPos targetPosition = edgePoint.getGlobalPosition();
		BlockState trackState = edgePoint.getTrackBlockState();
		ITrackBlock track = edgePoint.getTrack();
		AxisDirection axisDirection = edgePoint.getTargetDirection();
		Vec3 axis = track.getTrackAxes(level, targetPosition, trackState)
			.get(0)
			.normalize()
			.scale(axisDirection.getStep());
		return assemblyDirection = Direction.getNearest(axis.x, axis.y, axis.z);
	}

	@Override
	protected void setRemovedNotDueToChunkUnload() {
		assemblyAreas.get(level)
			.remove(worldPosition);

		super.setRemovedNotDueToChunkUnload();
	}

	public void assemble(UUID playerUUID) {
		refreshAssemblyInfo();

		if (bogeyLocations[0] != 0) {
			exception(new AssemblyException(Lang.translate("train_assembly.frontmost_bogey_at_station")), -1);
			return;
		}

		if (!edgePoint.hasValidTrack())
			return;

		BlockPos trackPosition = edgePoint.getGlobalPosition();
		BlockState trackState = edgePoint.getTrackBlockState();
		ITrackBlock track = edgePoint.getTrack();
		BlockPos bogeyOffset = new BlockPos(track.getUpNormal(level, trackPosition, trackState));

		DiscoveredLocation location = null;
		Vec3 centre = Vec3.atBottomCenterOf(trackPosition)
			.add(0, track.getElevationAtCenter(level, trackPosition, trackState), 0);
		Collection<DiscoveredLocation> ends = track.getConnected(level, trackPosition, trackState, true, null);
		Vec3 targetOffset = Vec3.atLowerCornerOf(assemblyDirection.getNormal());
		for (DiscoveredLocation end : ends)
			if (Mth.equal(0, targetOffset.distanceToSqr(end.getLocation()
				.subtract(centre)
				.normalize())))
				location = end;
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

		for (int j = 0; j < assemblyLength * 2 + 40; j++) {
			double i = j / 2d;
			if (points.size() == pointOffsets.size())
				break;

			DiscoveredLocation currentLocation = location;
			location = new DiscoveredLocation(location.getLocation()
				.add(directionVec.scale(.5)));

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
		boolean atLeastOneForwardControls = false;

		for (int bogeyIndex = 0; bogeyIndex < bogeyCount; bogeyIndex++) {
			int pointIndex = bogeyIndex * 2;
			if (bogeyIndex > 0)
				spacing.add(bogeyLocations[bogeyIndex] - bogeyLocations[bogeyIndex - 1]);
			CarriageContraption contraption = new CarriageContraption(assemblyDirection);
			BlockPos bogeyPosOffset = trackPosition.offset(bogeyOffset);

			try {
				boolean success = contraption.assemble(level,
					bogeyPosOffset.relative(assemblyDirection, bogeyLocations[bogeyIndex] + 1));
				atLeastOneForwardControls |= contraption.hasForwardControls();
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
			carriages.add(new Carriage(firstBogey, secondBogey, bogeySpacing));
		}

		if (!atLeastOneForwardControls) {
			exception(new AssemblyException(Lang.translate("train_assembly.no_controls")), -1);
			return;
		}

		for (CarriageContraption contraption : contraptions) {
			contraption.removeBlocksFromWorld(level, BlockPos.ZERO);
			contraption.expandBoundsAroundAxis(Axis.Y);
		}

		Train train = new Train(UUID.randomUUID(), playerUUID, graph, carriages, spacing, contraptions.stream()
			.anyMatch(CarriageContraption::hasBackwardControls));

		for (int i = 0; i < contraptions.size(); i++)
			carriages.get(i)
				.setContraption(level, contraptions.get(i));

		GlobalStation station = getStation();
		if (station != null) {
			train.setCurrentStation(station);
			station.reserveFor(train);
		}

		train.collectInitiallyOccupiedSignalBlocks();
		Create.RAILWAYS.addTrain(train);
		AllPackets.channel.send(PacketDistributor.ALL.noArg(), new TrainPacket(train, true));
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

	@Override
	@OnlyIn(Dist.CLIENT)
	public AABB getRenderBoundingBox() {
		if (isAssembling())
			return INFINITE_EXTENT_AABB;
		return super.getRenderBoundingBox();
	}

	@Override
	protected AABB createRenderBoundingBox() {
		return new AABB(worldPosition, edgePoint.getGlobalPosition()).inflate(2);
	}

}
