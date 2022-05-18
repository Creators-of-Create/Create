package com.simibubi.create.content.logistics.trains.track;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.jozufozu.flywheel.backend.instancing.InstancedRenderDispatcher;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.contraptions.components.structureMovement.ITransformableTE;
import com.simibubi.create.content.contraptions.components.structureMovement.StructureTransform;
import com.simibubi.create.content.logistics.trains.BezierConnection;
import com.simibubi.create.content.logistics.trains.ITrackBlock;
import com.simibubi.create.content.logistics.trains.TrackNodeLocation;
import com.simibubi.create.foundation.networking.AllPackets;
import com.simibubi.create.foundation.tileEntity.IMergeableTE;
import com.simibubi.create.foundation.tileEntity.RemoveTileEntityPacket;
import com.simibubi.create.foundation.tileEntity.SmartTileEntity;
import com.simibubi.create.foundation.tileEntity.TileEntityBehaviour;
import com.simibubi.create.foundation.utility.Debug;
import com.simibubi.create.foundation.utility.Pair;

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
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.DistExecutor;

public class TrackTileEntity extends SmartTileEntity implements ITransformableTE, IMergeableTE {

	Map<BlockPos, BezierConnection> connections;
	boolean connectionsValidated;
	boolean cancelDrops;

	public Pair<ResourceKey<Level>, BlockPos> boundLocation;

	public TrackTileEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
		connections = new HashMap<>();
		connectionsValidated = false;
	}

	public Map<BlockPos, BezierConnection> getConnections() {
		if (!level.isClientSide && !connectionsValidated)
			validateConnections();
		return connections;
	}

	@Override
	public void initialize() {
		super.initialize();
		if (!level.isClientSide && hasInteractableConnections())
			registerToCurveInteraction();
	}

	private void validateConnections() {
		Set<BlockPos> invalid = new HashSet<>();

		for (Entry<BlockPos, BezierConnection> entry : connections.entrySet()) {
			BlockPos key = entry.getKey();
			BezierConnection bc = entry.getValue();

			if (!key.equals(bc.getKey()) || !worldPosition.equals(bc.tePositions.getFirst())) {
				invalid.add(key);
				continue;
			}

			BlockState blockState = level.getBlockState(key);
			if (blockState.getBlock()instanceof ITrackBlock trackBlock && !blockState.getValue(TrackBlock.HAS_TE))
				for (Vec3 v : trackBlock.getTrackAxes(level, key, blockState)) {
					Vec3 bcEndAxis = bc.axes.getSecond();
					if (v.distanceTo(bcEndAxis) < 1 / 1024f || v.distanceTo(bcEndAxis.scale(-1)) < 1 / 1024f)
						level.setBlock(key, blockState.setValue(TrackBlock.HAS_TE, true), 3);
					else
						Debug.debugChat(v + " != " + bcEndAxis);
				}

			BlockEntity blockEntity = level.getBlockEntity(key);
			if (!(blockEntity instanceof TrackTileEntity trackTE) || blockEntity.isRemoved()) {
				invalid.add(key);
				continue;
			}

			if (!trackTE.connections.containsKey(worldPosition))
				trackTE.addConnection(bc.secondary());
		}

		connectionsValidated = true;
		for (BlockPos blockPos : invalid)
			removeConnection(blockPos);
	}

	public void addConnection(BezierConnection connection) {
		connections.put(connection.getKey(), connection);
		level.scheduleTick(worldPosition, getBlockState().getBlock(), 1);
		notifyUpdate();
	}

	public void removeConnection(BlockPos target) {
		connections.remove(target);
		notifyUpdate();
		if (!connections.isEmpty() || getBlockState().getOptionalValue(TrackBlock.SHAPE)
			.orElse(TrackShape.NONE)
			.isPortal())
			return;

		BlockState blockState = level.getBlockState(worldPosition);
		if (blockState.hasProperty(TrackBlock.HAS_TE))
			level.setBlockAndUpdate(worldPosition, blockState.setValue(TrackBlock.HAS_TE, false));
		AllPackets.channel.send(packetTarget(), new RemoveTileEntityPacket(worldPosition));
	}

	public void removeInboundConnections() {
		for (BezierConnection bezierConnection : connections.values()) {
			BlockEntity blockEntity = level.getBlockEntity(bezierConnection.getKey());
			if (!(blockEntity instanceof TrackTileEntity))
				return;
			TrackTileEntity other = (TrackTileEntity) blockEntity;
			other.removeConnection(bezierConnection.tePositions.getFirst());

			if (!cancelDrops)
				bezierConnection.spawnItems(level);
			bezierConnection.spawnDestroyParticles(level);
		}
		AllPackets.channel.send(packetTarget(), new RemoveTileEntityPacket(worldPosition));
	}

	public void bind(ResourceKey<Level> boundDimension, BlockPos boundLocation) {
		this.boundLocation = Pair.of(boundDimension, boundLocation);
		setChanged();
	}

	@Override
	public void writeSafe(CompoundTag tag, boolean clientPacket) {
		super.writeSafe(tag, clientPacket);
		writeTurns(tag);
	}

	@Override
	protected void write(CompoundTag tag, boolean clientPacket) {
		super.write(tag, clientPacket);
		writeTurns(tag);
		if (boundLocation == null)
			return;
		tag.put("BoundLocation", NbtUtils.writeBlockPos(boundLocation.getSecond()));
		tag.putString("BoundDimension", boundLocation.getFirst()
			.location()
			.toString());
	}

	private void writeTurns(CompoundTag tag) {
		ListTag listTag = new ListTag();
		for (BezierConnection bezierConnection : connections.values())
			listTag.add(bezierConnection.write(worldPosition));
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
	public void addBehaviours(List<TileEntityBehaviour> behaviours) {}

	@Override
	public void accept(BlockEntity other) {
		if (other instanceof TrackTileEntity track)
			connections.putAll(track.connections);
		connectionsValidated = false;
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

			Vec3 teVec = Vec3.atLowerCornerOf(worldPosition);
			Vec3 teCenterVec = teVec.add(0.5, 0.5, 0.5);
			Vec3 start = newConnection.starts.getFirst();
			Vec3 startToTE = start.subtract(teCenterVec);
			Vec3 endToStart = newConnection.starts.getSecond()
				.subtract(start);
			startToTE = transform.applyWithoutOffsetUncentered(startToTE)
				.add(teCenterVec);
			endToStart = transform.applyWithoutOffsetUncentered(endToStart)
				.add(startToTE);

			newConnection.starts.setFirst(new TrackNodeLocation(startToTE).getLocation());
			newConnection.starts.setSecond(new TrackNodeLocation(endToStart).getLocation());

			BlockPos newTarget = newConnection.getKey();
			transformedConnections.put(newTarget, newConnection);
		}

		connections = transformedConnections;
	}

	@Override
	public void setRemoved() {
		super.setRemoved();
		if (level.isClientSide)
			removeFromCurveInteraction();
	}

	@Override
	protected void setRemovedNotDueToChunkUnload() {
		super.setRemovedNotDueToChunkUnload();

		if (boundLocation != null && level instanceof ServerLevel) {
			ServerLevel otherLevel = level.getServer()
				.getLevel(boundLocation.getFirst());
			if (otherLevel == null)
				return;
			if (AllBlocks.TRACK.has(otherLevel.getBlockState(boundLocation.getSecond())))
				otherLevel.destroyBlock(boundLocation.getSecond(), false);
		}
	}

	private void registerToCurveInteraction() {
		DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> this::registerToCurveInteractionUnsafe);
	}

	private void removeFromCurveInteraction() {
		DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> this::removeFromCurveInteractionUnsafe);
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

}
