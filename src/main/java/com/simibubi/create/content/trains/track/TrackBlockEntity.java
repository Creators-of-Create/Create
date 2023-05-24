package com.simibubi.create.content.trains.track;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;

import com.jozufozu.flywheel.backend.instancing.InstancedRenderDispatcher;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllPackets;
import com.simibubi.create.AllTags;
import com.simibubi.create.content.contraptions.ITransformableBlockEntity;
import com.simibubi.create.content.contraptions.StructureTransform;
import com.simibubi.create.content.trains.graph.TrackNodeLocation;
import com.simibubi.create.foundation.block.ProperWaterloggedBlock;
import com.simibubi.create.foundation.blockEntity.IMergeableBE;
import com.simibubi.create.foundation.blockEntity.RemoveBlockEntityPacket;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.utility.Pair;
import com.simibubi.create.foundation.utility.VecHelper;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.client.model.data.ModelDataMap;
import net.minecraftforge.fml.DistExecutor;

public class TrackBlockEntity extends SmartBlockEntity implements ITransformableBlockEntity, IMergeableBE {

	Map<BlockPos, BezierConnection> connections;
	boolean cancelDrops;

	public Pair<ResourceKey<Level>, BlockPos> boundLocation;
	public TrackBlockEntityTilt tilt;

	public TrackBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
		connections = new HashMap<>();
		setLazyTickRate(100);
		tilt = new TrackBlockEntityTilt(this);
	}

	public Map<BlockPos, BezierConnection> getConnections() {
		return connections;
	}

	@Override
	public void initialize() {
		super.initialize();
		if (!level.isClientSide && hasInteractableConnections())
			registerToCurveInteraction();
	}

	@Override
	public void tick() {
		super.tick();
		tilt.undoSmoothing();
	}

	@Override
	public void lazyTick() {
		for (BezierConnection connection : connections.values())
			if (connection.isPrimary())
				manageFakeTracksAlong(connection, false);
	}

	public void validateConnections() {
		Set<BlockPos> invalid = new HashSet<>();

		for (Entry<BlockPos, BezierConnection> entry : connections.entrySet()) {
			BlockPos key = entry.getKey();
			BezierConnection bc = entry.getValue();

			if (!key.equals(bc.getKey()) || !worldPosition.equals(bc.tePositions.getFirst())) {
				invalid.add(key);
				continue;
			}

			BlockState blockState = level.getBlockState(key);
			if (blockState.getBlock()instanceof ITrackBlock trackBlock && !blockState.getValue(TrackBlock.HAS_BE))
				for (Vec3 v : trackBlock.getTrackAxes(level, key, blockState)) {
					Vec3 bcEndAxis = bc.axes.getSecond();
					if (v.distanceTo(bcEndAxis) < 1 / 1024f || v.distanceTo(bcEndAxis.scale(-1)) < 1 / 1024f)
						level.setBlock(key, blockState.setValue(TrackBlock.HAS_BE, true), 3);
				}

			BlockEntity blockEntity = level.getBlockEntity(key);
			if (!(blockEntity instanceof TrackBlockEntity trackBE) || blockEntity.isRemoved()) {
				invalid.add(key);
				continue;
			}

			if (!trackBE.connections.containsKey(worldPosition)) {
				trackBE.addConnection(bc.secondary());
				trackBE.tilt.tryApplySmoothing();
			}
		}

		for (BlockPos blockPos : invalid)
			removeConnection(blockPos);
	}

	public void addConnection(BezierConnection connection) {
		// don't replace existing connections with different materials
		if (connections.containsKey(connection.getKey()) && connection.equalsSansMaterial(connections.get(connection.getKey())))
			return;
		connections.put(connection.getKey(), connection);
		level.scheduleTick(worldPosition, getBlockState().getBlock(), 1);
		notifyUpdate();

		if (connection.isPrimary())
			manageFakeTracksAlong(connection, false);
	}

	public void removeConnection(BlockPos target) {
		if (isTilted())
			tilt.captureSmoothingHandles();

		BezierConnection removed = connections.remove(target);
		notifyUpdate();

		if (removed != null)
			manageFakeTracksAlong(removed, true);

		if (!connections.isEmpty() || getBlockState().getOptionalValue(TrackBlock.SHAPE)
			.orElse(TrackShape.NONE)
			.isPortal())
			return;

		BlockState blockState = level.getBlockState(worldPosition);
		if (blockState.hasProperty(TrackBlock.HAS_BE))
			level.setBlockAndUpdate(worldPosition, blockState.setValue(TrackBlock.HAS_BE, false));
		AllPackets.getChannel().send(packetTarget(), new RemoveBlockEntityPacket(worldPosition));
	}

	public void removeInboundConnections(boolean dropAndDiscard) {
		for (BezierConnection bezierConnection : connections.values()) {
			if (!(level.getBlockEntity(bezierConnection.getKey())instanceof TrackBlockEntity tbe))
				return;
			tbe.removeConnection(bezierConnection.tePositions.getFirst());
			if (!dropAndDiscard)
				continue;
			if (!cancelDrops)
				bezierConnection.spawnItems(level);
			bezierConnection.spawnDestroyParticles(level);
		}
		if (dropAndDiscard)
			AllPackets.getChannel().send(packetTarget(), new RemoveBlockEntityPacket(worldPosition));
	}

	public void bind(ResourceKey<Level> boundDimension, BlockPos boundLocation) {
		this.boundLocation = Pair.of(boundDimension, boundLocation);
		setChanged();
	}

	public boolean isTilted() {
		return tilt.smoothingAngle.isPresent();
	}

	@Override
	public void writeSafe(CompoundTag tag) {
		super.writeSafe(tag);
		writeTurns(tag, true);
	}

	@Override
	protected void write(CompoundTag tag, boolean clientPacket) {
		super.write(tag, clientPacket);
		writeTurns(tag, false);
		if (isTilted())
			tag.putDouble("Smoothing", tilt.smoothingAngle.get());
		if (boundLocation == null)
			return;
		tag.put("BoundLocation", NbtUtils.writeBlockPos(boundLocation.getSecond()));
		tag.putString("BoundDimension", boundLocation.getFirst()
			.location()
			.toString());
	}

	private void writeTurns(CompoundTag tag, boolean restored) {
		ListTag listTag = new ListTag();
		for (BezierConnection bezierConnection : connections.values())
			listTag.add((restored ? tilt.restoreToOriginalCurve(bezierConnection.clone()) : bezierConnection)
				.write(worldPosition));
		tag.put("Connections", listTag);
	}

	@Override
	protected void read(CompoundTag tag, boolean clientPacket) {
		super.read(tag, clientPacket);
		connections.clear();
		for (Tag t : tag.getList("Connections", Tag.TAG_COMPOUND)) {
			if (!(t instanceof CompoundTag))
				return;
			BezierConnection connection = new BezierConnection((CompoundTag) t, worldPosition);
			connections.put(connection.getKey(), connection);
		}

		boolean smoothingPreviously = tilt.smoothingAngle.isPresent();
		tilt.smoothingAngle = Optional.ofNullable(tag.contains("Smoothing") ? tag.getDouble("Smoothing") : null);
		if (smoothingPreviously != tilt.smoothingAngle.isPresent() && clientPacket) {
			requestModelDataUpdate();
			level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 16);
		}

		DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> InstancedRenderDispatcher.enqueueUpdate(this));

		if (hasInteractableConnections())
			registerToCurveInteraction();
		else
			removeFromCurveInteraction();

		if (tag.contains("BoundLocation"))
			boundLocation = Pair.of(
				ResourceKey.create(Registry.DIMENSION_REGISTRY, new ResourceLocation(tag.getString("BoundDimension"))),
				NbtUtils.readBlockPos(tag.getCompound("BoundLocation")));
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public AABB getRenderBoundingBox() {
		return INFINITE_EXTENT_AABB;
	}

	@Override
	public void addBehaviours(List<BlockEntityBehaviour> behaviours) {}

	@Override
	public void accept(BlockEntity other) {
		if (other instanceof TrackBlockEntity track)
			connections.putAll(track.connections);
		validateConnections();
		level.scheduleTick(worldPosition, getBlockState().getBlock(), 1);
	}

	public boolean hasInteractableConnections() {
		for (BezierConnection connection : connections.values())
			if (connection.isPrimary())
				return true;
		return false;
	}

	@Override
	public void transform(StructureTransform transform) {
		Map<BlockPos, BezierConnection> restoredConnections = new HashMap<>();
		for (Entry<BlockPos, BezierConnection> entry : connections.entrySet())
			restoredConnections.put(entry.getKey(),
				tilt.restoreToOriginalCurve(tilt.restoreToOriginalCurve(entry.getValue()
					.secondary())
					.secondary()));
		connections = restoredConnections;
		tilt.smoothingAngle = Optional.empty();

		if (transform.rotationAxis != Axis.Y)
			return;

		Map<BlockPos, BezierConnection> transformedConnections = new HashMap<>();
		for (Entry<BlockPos, BezierConnection> entry : connections.entrySet()) {
			BezierConnection newConnection = entry.getValue();
			newConnection.normals.replace(transform::applyWithoutOffsetUncentered);
			newConnection.axes.replace(transform::applyWithoutOffsetUncentered);

			BlockPos diff = newConnection.tePositions.getSecond()
				.subtract(newConnection.tePositions.getFirst());
			newConnection.tePositions.setSecond(new BlockPos(Vec3.atCenterOf(newConnection.tePositions.getFirst())
				.add(transform.applyWithoutOffsetUncentered(Vec3.atLowerCornerOf(diff)))));

			Vec3 beVec = Vec3.atLowerCornerOf(worldPosition);
			Vec3 teCenterVec = beVec.add(0.5, 0.5, 0.5);
			Vec3 start = newConnection.starts.getFirst();
			Vec3 startToBE = start.subtract(teCenterVec);
			Vec3 endToStart = newConnection.starts.getSecond()
				.subtract(start);
			startToBE = transform.applyWithoutOffsetUncentered(startToBE)
				.add(teCenterVec);
			endToStart = transform.applyWithoutOffsetUncentered(endToStart)
				.add(startToBE);

			newConnection.starts.setFirst(new TrackNodeLocation(startToBE).getLocation());
			newConnection.starts.setSecond(new TrackNodeLocation(endToStart).getLocation());

			BlockPos newTarget = newConnection.getKey();
			transformedConnections.put(newTarget, newConnection);
		}

		connections = transformedConnections;
	}

	@Override
	public void invalidate() {
		super.invalidate();
		if (level.isClientSide)
			removeFromCurveInteraction();
	}

	@Override
	public void remove() {
		super.remove();

		for (BezierConnection connection : connections.values())
			manageFakeTracksAlong(connection, true);

		if (boundLocation != null && level instanceof ServerLevel) {
			ServerLevel otherLevel = level.getServer()
				.getLevel(boundLocation.getFirst());
			if (otherLevel == null)
				return;
			if (AllTags.AllBlockTags.TRACKS.matches(otherLevel.getBlockState(boundLocation.getSecond())))
				otherLevel.destroyBlock(boundLocation.getSecond(), false);
		}
	}

	private void registerToCurveInteraction() {
		DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> this::registerToCurveInteractionUnsafe);
	}

	private void removeFromCurveInteraction() {
		DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> this::removeFromCurveInteractionUnsafe);
	}

	@Override
	public IModelData getModelData() {
		if (!isTilted())
			return super.getModelData();
		return new ModelDataMap.Builder()
			.withInitial(TrackBlockEntityTilt.ASCENDING_PROPERTY, tilt.smoothingAngle.get())
			.build();
	}

	@OnlyIn(Dist.CLIENT)
	private void registerToCurveInteractionUnsafe() {
		TrackBlockOutline.TRACKS_WITH_TURNS.get(level)
			.put(worldPosition, this);
	}

	@OnlyIn(Dist.CLIENT)
	private void removeFromCurveInteractionUnsafe() {
		TrackBlockOutline.TRACKS_WITH_TURNS.get(level)
			.remove(worldPosition);
	}

	public void manageFakeTracksAlong(BezierConnection bc, boolean remove) {
		Map<Pair<Integer, Integer>, Double> yLevels = new HashMap<>();
		BlockPos tePosition = bc.tePositions.getFirst();
		Vec3 end1 = bc.starts.getFirst()
			.subtract(Vec3.atLowerCornerOf(tePosition))
			.add(0, 3 / 16f, 0);
		Vec3 end2 = bc.starts.getSecond()
			.subtract(Vec3.atLowerCornerOf(tePosition))
			.add(0, 3 / 16f, 0);
		Vec3 axis1 = bc.axes.getFirst();
		Vec3 axis2 = bc.axes.getSecond();

		double handleLength = bc.getHandleLength();

		Vec3 finish1 = axis1.scale(handleLength)
			.add(end1);
		Vec3 finish2 = axis2.scale(handleLength)
			.add(end2);

		Vec3 faceNormal1 = bc.normals.getFirst();
		Vec3 faceNormal2 = bc.normals.getSecond();

		int segCount = bc.getSegmentCount();
		float[] lut = bc.getStepLUT();

		for (int i = 0; i < segCount; i++) {
			float t = i == segCount ? 1 : i * lut[i] / segCount;
			t += 0.5f / segCount;

			Vec3 result = VecHelper.bezier(end1, end2, finish1, finish2, t);
			Vec3 derivative = VecHelper.bezierDerivative(end1, end2, finish1, finish2, t)
				.normalize();
			Vec3 faceNormal =
				faceNormal1.equals(faceNormal2) ? faceNormal1 : VecHelper.slerp(t, faceNormal1, faceNormal2);
			Vec3 normal = faceNormal.cross(derivative)
				.normalize();
			Vec3 below = result.add(faceNormal.scale(-.25f));
			Vec3 rail1 = below.add(normal.scale(.05f));
			Vec3 rail2 = below.subtract(normal.scale(.05f));
			Vec3 railMiddle = rail1.add(rail2)
				.scale(.5);

			for (Vec3 vec : new Vec3[] { railMiddle }) {
				BlockPos pos = new BlockPos(vec);
				Pair<Integer, Integer> key = Pair.of(pos.getX(), pos.getZ());
				if (!yLevels.containsKey(key) || yLevels.get(key) > vec.y)
					yLevels.put(key, vec.y);
			}
		}

		for (Entry<Pair<Integer, Integer>, Double> entry : yLevels.entrySet()) {
			double yValue = entry.getValue();
			int floor = Mth.floor(yValue);
			BlockPos targetPos = new BlockPos(entry.getKey()
				.getFirst(), floor,
				entry.getKey()
					.getSecond());
			targetPos = targetPos.offset(tePosition)
				.above(1);

			BlockState stateAtPos = level.getBlockState(targetPos);
			boolean present = AllBlocks.FAKE_TRACK.has(stateAtPos);

			if (remove) {
				if (present)
					level.removeBlock(targetPos, false);
				continue;
			}

			FluidState fluidState = stateAtPos.getFluidState();
			if (!fluidState.isEmpty() && !fluidState.isSourceOfType(Fluids.WATER))
				continue;

			if (!present && stateAtPos.getMaterial()
				.isReplaceable())
				level.setBlock(targetPos,
					ProperWaterloggedBlock.withWater(level, AllBlocks.FAKE_TRACK.getDefaultState(), targetPos), 3);
			FakeTrackBlock.keepAlive(level, targetPos);
		}
	}

}
