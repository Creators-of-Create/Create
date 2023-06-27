package com.simibubi.create.content.trains.station;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Consumer;

import javax.annotation.Nullable;

import org.jetbrains.annotations.NotNull;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import com.simibubi.create.AllPackets;
import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.Create;
import com.simibubi.create.compat.computercraft.AbstractComputerBehaviour;
import com.simibubi.create.compat.computercraft.ComputerCraftProxy;
import com.simibubi.create.content.contraptions.AssemblyException;
import com.simibubi.create.content.contraptions.ITransformableBlockEntity;
import com.simibubi.create.content.contraptions.StructureTransform;
import com.simibubi.create.content.decoration.slidingDoor.DoorControlBehaviour;
import com.simibubi.create.content.logistics.depot.DepotBehaviour;
import com.simibubi.create.content.redstone.displayLink.DisplayLinkBlock;
import com.simibubi.create.content.trains.bogey.AbstractBogeyBlock;
import com.simibubi.create.content.trains.bogey.AbstractBogeyBlockEntity;
import com.simibubi.create.content.trains.entity.Carriage;
import com.simibubi.create.content.trains.entity.CarriageBogey;
import com.simibubi.create.content.trains.entity.CarriageContraption;
import com.simibubi.create.content.trains.entity.Train;
import com.simibubi.create.content.trains.entity.TrainPacket;
import com.simibubi.create.content.trains.entity.TravellingPoint;
import com.simibubi.create.content.trains.graph.EdgePointType;
import com.simibubi.create.content.trains.graph.TrackEdge;
import com.simibubi.create.content.trains.graph.TrackGraph;
import com.simibubi.create.content.trains.graph.TrackGraphLocation;
import com.simibubi.create.content.trains.graph.TrackNode;
import com.simibubi.create.content.trains.graph.TrackNodeLocation;
import com.simibubi.create.content.trains.graph.TrackNodeLocation.DiscoveredLocation;
import com.simibubi.create.content.trains.schedule.Schedule;
import com.simibubi.create.content.trains.schedule.ScheduleItem;
import com.simibubi.create.content.trains.track.ITrackBlock;
import com.simibubi.create.content.trains.track.TrackTargetingBehaviour;
import com.simibubi.create.foundation.advancement.AllAdvancements;
import com.simibubi.create.foundation.block.ProperWaterloggedBlock;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.utility.Iterate;
import com.simibubi.create.foundation.utility.Lang;
import com.simibubi.create.foundation.utility.NBTHelper;
import com.simibubi.create.foundation.utility.VecHelper;
import com.simibubi.create.foundation.utility.WorldAttached;
import com.simibubi.create.foundation.utility.animation.LerpedFloat;
import com.simibubi.create.foundation.utility.animation.LerpedFloat.Chaser;
import com.simibubi.create.infrastructure.config.AllConfigs;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.Direction.AxisDirection;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.network.PacketDistributor;

public class StationBlockEntity extends SmartBlockEntity implements ITransformableBlockEntity {

	public TrackTargetingBehaviour<GlobalStation> edgePoint;
	public DoorControlBehaviour doorControls;
	public LerpedFloat flag;

	protected int failedCarriageIndex;
	protected AssemblyException lastException;
	protected DepotBehaviour depotBehaviour;
	public AbstractComputerBehaviour computerBehaviour;

	// for display
	UUID imminentTrain;
	boolean trainPresent;
	boolean trainBackwards;
	boolean trainCanDisassemble;
	boolean trainHasSchedule;
	boolean trainHasAutoSchedule;

	int flagYRot = -1;
	boolean flagFlipped;

	public Component lastDisassembledTrainName;

	public StationBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
		setLazyTickRate(20);
		lastException = null;
		failedCarriageIndex = -1;
		flag = LerpedFloat.linear()
			.startWithValue(0);
	}

	@Override
	public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
		behaviours.add(edgePoint = new TrackTargetingBehaviour<>(this, EdgePointType.STATION));
		behaviours.add(doorControls = new DoorControlBehaviour(this));
		behaviours.add(depotBehaviour = new DepotBehaviour(this).onlyAccepts(AllItems.SCHEDULE::isIn)
			.withCallback(s -> applyAutoSchedule()));
		depotBehaviour.addSubBehaviours(behaviours);
		registerAwardables(behaviours, AllAdvancements.CONTRAPTION_ACTORS, AllAdvancements.TRAIN,
			AllAdvancements.LONG_TRAIN, AllAdvancements.CONDUCTOR);
		behaviours.add(computerBehaviour = ComputerCraftProxy.behaviour(this));
	}

	@Override
	protected void read(CompoundTag tag, boolean clientPacket) {
		lastException = AssemblyException.read(tag);
		failedCarriageIndex = tag.getInt("FailedCarriageIndex");
		super.read(tag, clientPacket);
		invalidateRenderBoundingBox();

		if (tag.contains("ForceFlag"))
			trainPresent = tag.getBoolean("ForceFlag");
		if (tag.contains("PrevTrainName"))
			lastDisassembledTrainName = Component.Serializer.fromJson(tag.getString("PrevTrainName"));

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
		trainPresent = tag.contains("TrainPresent");
		trainCanDisassemble = tag.contains("TrainCanDisassemble");
		trainBackwards = tag.contains("TrainBackwards");
		trainHasSchedule = tag.contains("TrainHasSchedule");
		trainHasAutoSchedule = tag.contains("TrainHasAutoSchedule");
	}

	@Override
	protected void write(CompoundTag tag, boolean clientPacket) {
		AssemblyException.write(tag, lastException);
		tag.putInt("FailedCarriageIndex", failedCarriageIndex);

		if (lastDisassembledTrainName != null)
			tag.putString("PrevTrainName", Component.Serializer.toJson(lastDisassembledTrainName));

		super.write(tag, clientPacket);

		if (!clientPacket)
			return;
		if (imminentTrain == null)
			return;

		tag.putUUID("ImminentTrain", imminentTrain);

		if (trainPresent)
			NBTHelper.putMarker(tag, "TrainPresent");
		if (trainCanDisassemble)
			NBTHelper.putMarker(tag, "TrainCanDisassemble");
		if (trainBackwards)
			NBTHelper.putMarker(tag, "TrainBackwards");
		if (trainHasSchedule)
			NBTHelper.putMarker(tag, "TrainHasSchedule");
		if (trainHasAutoSchedule)
			NBTHelper.putMarker(tag, "TrainHasAutoSchedule");
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
	AbstractBogeyBlock<?>[] bogeyTypes;
	boolean[] upsideDownBogeys;
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

		if (level.isClientSide) {
			float currentTarget = flag.getChaseTarget();
			if (currentTarget == 0 || flag.settled()) {
				int target = trainPresent || isAssembling() ? 1 : 0;
				if (target != currentTarget) {
					flag.chase(target, 0.1f, Chaser.LINEAR);
					if (target == 1)
						AllSoundEvents.CONTRAPTION_ASSEMBLE.playAt(level, worldPosition, 1, 2, true);
				}
			}
			boolean settled = flag.getValue() > .15f;
			flag.tickChaser();
			if (currentTarget == 0 && settled != flag.getValue() > .15f)
				AllSoundEvents.CONTRAPTION_DISASSEMBLE.playAt(level, worldPosition, 0.75f, 1.5f, true);
			return;
		}

		GlobalStation station = getStation();
		if (station == null)
			return;

		Train imminentTrain = station.getImminentTrain();
		boolean trainPresent = imminentTrain != null && imminentTrain.getCurrentStation() == station;
		boolean canDisassemble = trainPresent && imminentTrain.canDisassemble();
		UUID imminentID = imminentTrain != null ? imminentTrain.id : null;
		boolean trainHasSchedule = trainPresent && imminentTrain.runtime.getSchedule() != null;
		boolean trainHasAutoSchedule = trainHasSchedule && imminentTrain.runtime.isAutoSchedule;
		boolean newlyArrived = this.trainPresent != trainPresent;

		if (trainPresent && imminentTrain.runtime.displayLinkUpdateRequested) {
			DisplayLinkBlock.notifyGatherers(level, worldPosition);
			imminentTrain.runtime.displayLinkUpdateRequested = false;
		}

		if (newlyArrived)
			applyAutoSchedule();

		if (newlyArrived || this.trainCanDisassemble != canDisassemble
			|| !Objects.equals(imminentID, this.imminentTrain) || this.trainHasSchedule != trainHasSchedule
			|| this.trainHasAutoSchedule != trainHasAutoSchedule) {

			this.imminentTrain = imminentID;
			this.trainPresent = trainPresent;
			this.trainCanDisassemble = canDisassemble;
			this.trainBackwards = imminentTrain != null && imminentTrain.currentlyBackwards;
			this.trainHasSchedule = trainHasSchedule;
			this.trainHasAutoSchedule = trainHasAutoSchedule;

			notifyUpdate();
		}
	}

	public boolean trackClicked(Player player, InteractionHand hand, ITrackBlock track, BlockState state,
		BlockPos pos) {
		refreshAssemblyInfo();
		BoundingBox bb = assemblyAreas.get(level)
			.get(worldPosition);
		if (bb == null || !bb.isInside(pos))
			return false;

		BlockPos up = BlockPos.containing(track.getUpNormal(level, pos, state));
		BlockPos down = BlockPos.containing(track.getUpNormal(level, pos, state).scale(-1));
		int bogeyOffset = pos.distManhattan(edgePoint.getGlobalPosition()) - 1;
		
		if (!isValidBogeyOffset(bogeyOffset)) {
			for (boolean upsideDown : Iterate.falseAndTrue) {
				for (int i = -1; i <= 1; i++) {
					BlockPos bogeyPos = pos.relative(assemblyDirection, i)
						.offset(upsideDown ? down : up);
					BlockState blockState = level.getBlockState(bogeyPos);
					if (!(blockState.getBlock() instanceof AbstractBogeyBlock<?> bogey))
						continue;
					BlockEntity be = level.getBlockEntity(bogeyPos);
					if (!(be instanceof AbstractBogeyBlockEntity oldBE))
						continue;
					CompoundTag oldData = oldBE.getBogeyData();
					BlockState newBlock = bogey.getNextSize(oldBE);
					if (newBlock.getBlock() == bogey)
						player.displayClientMessage(Lang.translateDirect("bogey.style.no_other_sizes")
							.withStyle(ChatFormatting.RED), true);
					level.setBlock(bogeyPos, newBlock, 3);
					BlockEntity newEntity = level.getBlockEntity(bogeyPos);
					if (!(newEntity instanceof AbstractBogeyBlockEntity newBE))
						continue;
					newBE.setBogeyData(oldData);
					bogey.playRotateSound(level, bogeyPos);
					return true;
				}
			}

			return false;
		}

		ItemStack handItem = player.getItemInHand(hand);
		if (!player.isCreative() && !AllBlocks.RAILWAY_CASING.isIn(handItem)) {
			player.displayClientMessage(Lang.translateDirect("train_assembly.requires_casing"), true);
			return false;
		}

		boolean upsideDown = (player.getViewXRot(1.0F) < 0 && (track.getBogeyAnchor(level, pos, state)).getBlock() instanceof AbstractBogeyBlock<?> bogey && bogey.canBeUpsideDown());

		BlockPos targetPos = upsideDown ? pos.offset(down) : pos.offset(up);
		if (level.getBlockState(targetPos)
			.getDestroySpeed(level, targetPos) == -1) {
			return false;
		}

		level.destroyBlock(targetPos, true);

		BlockState bogeyAnchor = track.getBogeyAnchor(level, pos, state);
		if (bogeyAnchor.getBlock() instanceof AbstractBogeyBlock<?> bogey) {
			bogeyAnchor = bogey.getVersion(bogeyAnchor, upsideDown);
		}
		bogeyAnchor = ProperWaterloggedBlock.withWater(level, bogeyAnchor, pos);
		level.setBlock(targetPos, bogeyAnchor, 3);
		player.displayClientMessage(Lang.translateDirect("train_assembly.bogey_created"), true);
		SoundType soundtype = bogeyAnchor.getBlock()
			.getSoundType(state, level, pos, player);
		level.playSound(null, pos, soundtype.getPlaceSound(), SoundSource.BLOCKS, (soundtype.getVolume() + 1.0F) / 2.0F,
			soundtype.getPitch() * 0.8F);

		if (!player.isCreative()) {
			ItemStack itemInHand = player.getItemInHand(hand);
			itemInHand.shrink(1);
			if (itemInHand.isEmpty())
				player.setItemInHand(hand, ItemStack.EMPTY);
		}

		return true;
	}

	public boolean enterAssemblyMode(@Nullable ServerPlayer sender) {
		if (isAssembling())
			return false;

		tryDisassembleTrain(sender);
		if (!tryEnterAssemblyMode())
			return false;

		BlockState newState = getBlockState().setValue(StationBlock.ASSEMBLING, true);
		level.setBlock(getBlockPos(), newState, 3);
		refreshBlockState();
		refreshAssemblyInfo();

		updateStationState(station -> station.assembling = true);
		GlobalStation station = getStation();
		if (station != null) {
			for (Train train : Create.RAILWAYS.sided(level).trains.values()) {
				if (train.navigation.destination != station)
					continue;

				GlobalStation preferredDestination = train.runtime.startCurrentInstruction();
				train.navigation.startNavigation(preferredDestination != null ? preferredDestination : station, Double.MAX_VALUE, false);
			}
		}

		return true;
	}

	public boolean exitAssemblyMode() {
		if (!isAssembling())
			return false;

		cancelAssembly();
		BlockState newState = getBlockState().setValue(StationBlock.ASSEMBLING, false);
		level.setBlock(getBlockPos(), newState, 3);
		refreshBlockState();

		return updateStationState(station -> station.assembling = false);
	}

	public boolean tryDisassembleTrain(@Nullable ServerPlayer sender) {
		GlobalStation station = getStation();
		if (station == null)
			return false;

		Train train = station.getPresentTrain();
		if (train == null)
			return false;

		BlockPos trackPosition = edgePoint.getGlobalPosition();
		if (!train.disassemble(getAssemblyDirection(), trackPosition.above()))
			return false;

		dropSchedule(sender);
		return true;
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

	public void dropSchedule(@Nullable ServerPlayer sender) {
		GlobalStation station = getStation();
		if (station == null)
			return;

		Train train = station.getPresentTrain();
		if (train == null)
			return;

		ItemStack schedule = train.runtime.returnSchedule();
		if (schedule.isEmpty())
			return;
		if (sender != null && sender.getMainHandItem().isEmpty()) {
			sender.getInventory()
					.placeItemBackInInventory(schedule);
			return;
		}

		Vec3 v = VecHelper.getCenterOf(getBlockPos());
		ItemEntity itemEntity = new ItemEntity(getLevel(), v.x, v.y, v.z, schedule);
		itemEntity.setDeltaMovement(Vec3.ZERO);
		getLevel().addFreshEntity(itemEntity);
	}

	private boolean updateStationState(Consumer<GlobalStation> updateState) {
		GlobalStation station = getStation();
		TrackGraphLocation graphLocation = edgePoint.determineGraphLocation();
		if (station == null || graphLocation == null)
			return false;

		updateState.accept(station);
		Create.RAILWAYS.sync.pointAdded(graphLocation.graph, station);
		Create.RAILWAYS.markTracksDirty();
		return true;
	}

	public void refreshAssemblyInfo() {
		if (!edgePoint.hasValidTrack())
			return;

		if (!isVirtual()) {
			GlobalStation station = getStation();
			if (station == null || station.getPresentTrain() != null)
				return;
		}

		int prevLength = assemblyLength;
		BlockPos targetPosition = edgePoint.getGlobalPosition();
		BlockState trackState = edgePoint.getTrackBlockState();
		ITrackBlock track = edgePoint.getTrack();
		getAssemblyDirection();

		MutableBlockPos currentPos = targetPosition.mutable();
		currentPos.move(assemblyDirection);

		BlockPos bogeyOffset = BlockPos.containing(track.getUpNormal(level, targetPosition, trackState));

		int MAX_LENGTH = AllConfigs.server().trains.maxAssemblyLength.get();
		int MAX_BOGEY_COUNT = AllConfigs.server().trains.maxBogeyCount.get();

		int bogeyIndex = 0;
		int maxBogeyCount = MAX_BOGEY_COUNT;
		if (bogeyLocations == null)
			bogeyLocations = new int[maxBogeyCount];
		if (bogeyTypes == null)
			bogeyTypes = new AbstractBogeyBlock[maxBogeyCount];
		if (upsideDownBogeys == null)
			upsideDownBogeys = new boolean[maxBogeyCount];
		Arrays.fill(bogeyLocations, -1);
		Arrays.fill(bogeyTypes, null);
		Arrays.fill(upsideDownBogeys, false);

		for (int i = 0; i < MAX_LENGTH; i++) {
			if (i == MAX_LENGTH - 1) {
				assemblyLength = i;
				break;
			}
			if (!track.trackEquals(trackState, level.getBlockState(currentPos))) {
				assemblyLength = Math.max(0, i - 1);
				break;
			}

			BlockState potentialBogeyState = level.getBlockState(bogeyOffset.offset(currentPos));
			BlockPos upsideDownBogeyOffset = new BlockPos(bogeyOffset.getX(), bogeyOffset.getY()*-1, bogeyOffset.getZ());
			if (bogeyIndex < bogeyLocations.length) {
				if (potentialBogeyState.getBlock() instanceof AbstractBogeyBlock<?> bogey && !bogey.isUpsideDown(potentialBogeyState)) {
					bogeyTypes[bogeyIndex] = bogey;
					bogeyLocations[bogeyIndex] = i;
					upsideDownBogeys[bogeyIndex] = false;
					bogeyIndex++;
				} else if ((potentialBogeyState = level.getBlockState(upsideDownBogeyOffset.offset(currentPos))).getBlock() instanceof AbstractBogeyBlock<?> bogey && bogey.isUpsideDown(potentialBogeyState)) {
					bogeyTypes[bogeyIndex] = bogey;
					bogeyLocations[bogeyIndex] = i;
					upsideDownBogeys[bogeyIndex] = true;
					bogeyIndex++;
				}
			}

			currentPos.move(assemblyDirection);
		}

		bogeyCount = bogeyIndex;

		if (level.isClientSide)
			return;
		if (prevLength == assemblyLength)
			return;
		if (isVirtual())
			return;

		Map<BlockPos, BoundingBox> map = assemblyAreas.get(level);
		BlockPos startPosition = targetPosition.relative(assemblyDirection);
		BlockPos trackEnd = startPosition.relative(assemblyDirection, assemblyLength - 1);
		map.put(worldPosition, BoundingBox.fromCorners(startPosition, trackEnd));
	}

	public boolean updateName(String name) {
		if (!updateStationState(station -> station.name = name))
			return false;
		notifyUpdate();

		return true;
	}

	public boolean isValidBogeyOffset(int i) {
		if ((i < 3 || bogeyCount == 0) && i != 0)
			return false;
		for (int j : bogeyLocations) {
			if (j == -1)
				break;
			if (i >= j - 2 && i <= j + 2)
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
	public void remove() {
		assemblyAreas.get(level)
			.remove(worldPosition);
		super.remove();
	}

	public void assemble(UUID playerUUID) {
		refreshAssemblyInfo();

		if (bogeyLocations == null)
			return;

		if (bogeyLocations[0] != 0) {
			exception(new AssemblyException(Lang.translateDirect("train_assembly.frontmost_bogey_at_station")), -1);
			return;
		}

		if (!edgePoint.hasValidTrack())
			return;

		BlockPos trackPosition = edgePoint.getGlobalPosition();
		BlockState trackState = edgePoint.getTrackBlockState();
		ITrackBlock track = edgePoint.getTrack();
		BlockPos bogeyOffset = BlockPos.containing(track.getUpNormal(level, trackPosition, trackState));

		TrackNodeLocation location = null;
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
		int iPrevious = -100;
		for (int i = 0; i < bogeyLocations.length; i++) {
			int loc = bogeyLocations[i];
			if (loc == -1)
				break;

			if (loc - iPrevious < 3) {
				exception(new AssemblyException(Lang.translateDirect("train_assembly.bogeys_too_close", i, i + 1)), -1);
				return;
			}

			double bogeySize = bogeyTypes[i].getWheelPointSpacing();
			pointOffsets.add(Double.valueOf(loc + .5 - bogeySize / 2));
			pointOffsets.add(Double.valueOf(loc + .5 + bogeySize / 2));
			iPrevious = loc;
		}

		List<TravellingPoint> points = new ArrayList<>();
		Vec3 directionVec = Vec3.atLowerCornerOf(assemblyDirection.getNormal());
		TrackGraph graph = null;
		TrackNode secondNode = null;

		for (int j = 0; j < assemblyLength * 2 + 40; j++) {
			double i = j / 2d;
			if (points.size() == pointOffsets.size())
				break;

			TrackNodeLocation currentLocation = location;
			location = new TrackNodeLocation(location.getLocation()
				.add(directionVec.scale(.5))).in(location.dimension);

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
						Vec3 edgeDirection = edge.getDirection(true);
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

				points.add(new TravellingPoint(node, secondNode, edge, positionOnEdge, false));
			}

			secondNode = node;
		}

		if (points.size() != pointOffsets.size()) {
			Create.LOGGER.warn("Cannot assemble: Not all Points created");
			return;
		}

		if (points.size() == 0) {
			exception(new AssemblyException(Lang.translateDirect("train_assembly.no_bogeys")), -1);
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
			BlockPos upsideDownBogeyPosOffset = trackPosition.offset(new BlockPos(bogeyOffset.getX(), bogeyOffset.getY() * -1, bogeyOffset.getZ()));

			try {
				int offset = bogeyLocations[bogeyIndex] + 1;
				boolean success = contraption.assemble(level, upsideDownBogeys[bogeyIndex] ? upsideDownBogeyPosOffset.relative(assemblyDirection, offset) : bogeyPosOffset.relative(assemblyDirection, offset));
				atLeastOneForwardControls |= contraption.hasForwardControls();
				contraption.setSoundQueueOffset(offset);
				if (!success) {
					exception(new AssemblyException(Lang.translateDirect("train_assembly.nothing_attached", bogeyIndex + 1)),
						-1);
					return;
				}
			} catch (AssemblyException e) {
				exception(e, contraptions.size() + 1);
				return;
			}

			AbstractBogeyBlock<?> typeOfFirstBogey = bogeyTypes[bogeyIndex];
			boolean firstBogeyIsUpsideDown = upsideDownBogeys[bogeyIndex];
			BlockPos firstBogeyPos = contraption.anchor;
			AbstractBogeyBlockEntity firstBogeyBlockEntity = (AbstractBogeyBlockEntity) level.getBlockEntity(firstBogeyPos);
			CarriageBogey firstBogey =
				new CarriageBogey(typeOfFirstBogey, firstBogeyIsUpsideDown, firstBogeyBlockEntity.getBogeyData(), points.get(pointIndex), points.get(pointIndex + 1));
			CarriageBogey secondBogey = null;
			BlockPos secondBogeyPos = contraption.getSecondBogeyPos();
			int bogeySpacing = 0;

			if (secondBogeyPos != null) {
				if (bogeyIndex == bogeyCount - 1 || !secondBogeyPos
					.equals((upsideDownBogeys[bogeyIndex + 1] ? upsideDownBogeyPosOffset : bogeyPosOffset).relative(assemblyDirection, bogeyLocations[bogeyIndex + 1] + 1))) {
					exception(new AssemblyException(Lang.translateDirect("train_assembly.not_connected_in_order")),
						contraptions.size() + 1);
					return;
				}
				AbstractBogeyBlockEntity secondBogeyBlockEntity =
						(AbstractBogeyBlockEntity) level.getBlockEntity(secondBogeyPos);
				bogeySpacing = bogeyLocations[bogeyIndex + 1] - bogeyLocations[bogeyIndex];
				secondBogey = new CarriageBogey(bogeyTypes[bogeyIndex + 1], upsideDownBogeys[bogeyIndex + 1], secondBogeyBlockEntity.getBogeyData(),
						points.get(pointIndex + 2), points.get(pointIndex + 3));
				bogeyIndex++;

			} else if (!typeOfFirstBogey.allowsSingleBogeyCarriage()) {
				exception(new AssemblyException(Lang.translateDirect("train_assembly.single_bogey_carriage")),
					contraptions.size() + 1);
				return;
			}

			contraptions.add(contraption);
			carriages.add(new Carriage(firstBogey, secondBogey, bogeySpacing));
		}

		if (!atLeastOneForwardControls) {
			exception(new AssemblyException(Lang.translateDirect("train_assembly.no_controls")), -1);
			return;
		}

		for (CarriageContraption contraption : contraptions) {
			contraption.removeBlocksFromWorld(level, BlockPos.ZERO);
			contraption.expandBoundsAroundAxis(Axis.Y);
		}

		Train train = new Train(UUID.randomUUID(), playerUUID, graph, carriages, spacing, contraptions.stream()
			.anyMatch(CarriageContraption::hasBackwardControls));

		if (lastDisassembledTrainName != null) {
			train.name = lastDisassembledTrainName;
			lastDisassembledTrainName = null;
		}

		for (int i = 0; i < contraptions.size(); i++) {
			CarriageContraption contraption = contraptions.get(i);
			Carriage carriage = carriages.get(i);
			carriage.setContraption(level, contraption);
			if (contraption.containsBlockBreakers())
				award(AllAdvancements.CONTRAPTION_ACTORS);
		}

		GlobalStation station = getStation();
		if (station != null) {
			train.setCurrentStation(station);
			station.reserveFor(train);
		}

		train.collectInitiallyOccupiedSignalBlocks();
		Create.RAILWAYS.addTrain(train);
		AllPackets.getChannel().send(PacketDistributor.ALL.noArg(), new TrainPacket(train, true));
		clearException();

		award(AllAdvancements.TRAIN);
		if (contraptions.size() >= 6)
			award(AllAdvancements.LONG_TRAIN);
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

	public ItemStack getAutoSchedule() {
		return depotBehaviour.getHeldItemStack();
	}

	@Override
	public <T> @NotNull LazyOptional<T> getCapability(@NotNull Capability<T> cap, Direction side) {
		if (isItemHandlerCap(cap))
			return depotBehaviour.getItemCapability(cap, side);
		if (computerBehaviour.isPeripheralCap(cap))
			return computerBehaviour.getPeripheralCapability();
		return super.getCapability(cap, side);
	}

	@Override
	public void invalidateCaps() {
		super.invalidateCaps();
		computerBehaviour.removePeripheral();
	}

	private void applyAutoSchedule() {
		ItemStack stack = getAutoSchedule();
		if (!AllItems.SCHEDULE.isIn(stack))
			return;
		Schedule schedule = ScheduleItem.getSchedule(stack);
		if (schedule == null || schedule.entries.isEmpty())
			return;
		GlobalStation station = getStation();
		if (station == null)
			return;
		Train imminentTrain = station.getImminentTrain();
		if (imminentTrain == null || imminentTrain.getCurrentStation() != station)
			return;

		award(AllAdvancements.CONDUCTOR);
		imminentTrain.runtime.setSchedule(schedule, true);
		AllSoundEvents.CONFIRM.playOnServer(level, worldPosition, 1, 1);

		if (!(level instanceof ServerLevel server))
			return;

		Vec3 v = Vec3.atBottomCenterOf(worldPosition.above());
		server.sendParticles(ParticleTypes.HAPPY_VILLAGER, v.x, v.y, v.z, 8, 0.35, 0.05, 0.35, 1);
		server.sendParticles(ParticleTypes.END_ROD, v.x, v.y + .25f, v.z, 10, 0.05, 1, 0.05, 0.005f);
	}

	public boolean resolveFlagAngle() {
		if (flagYRot != -1)
			return true;

		BlockState target = edgePoint.getTrackBlockState();
		if (!(target.getBlock() instanceof ITrackBlock def))
			return false;

		Vec3 axis = null;
		BlockPos trackPos = edgePoint.getGlobalPosition();
		for (Vec3 vec3 : def.getTrackAxes(level, trackPos, target))
			axis = vec3.scale(edgePoint.getTargetDirection()
				.getStep());
		if (axis == null)
			return false;

		Direction nearest = Direction.getNearest(axis.x, 0, axis.z);
		flagYRot = (int) (-nearest.toYRot() - 90);

		Vec3 diff = Vec3.atLowerCornerOf(trackPos.subtract(worldPosition))
			.multiply(1, 0, 1);
		if (diff.lengthSqr() == 0)
			return true;

		flagFlipped = diff.dot(Vec3.atLowerCornerOf(nearest.getClockWise()
			.getNormal())) > 0;

		return true;
	}

	@Override
	public void transform(StructureTransform transform) {
		edgePoint.transform(transform);
	}

}
