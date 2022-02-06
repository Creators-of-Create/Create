package com.simibubi.create.content.logistics.trains.track;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.jozufozu.flywheel.backend.instancing.InstancedRenderDispatcher;
import com.simibubi.create.content.logistics.trains.BezierConnection;
import com.simibubi.create.foundation.tileEntity.SmartTileEntity;
import com.simibubi.create.foundation.tileEntity.TileEntityBehaviour;
import com.simibubi.create.foundation.utility.Couple;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;

public class TrackTileEntity extends SmartTileEntity {

	Couple<Map<BlockPos, BezierConnection>> connections;

	public TrackTileEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
		connections = Couple.create(HashMap::new);
	}

	public Couple<Map<BlockPos, BezierConnection>> getConnections() {
		return connections;
	}

	public void addConnection(boolean front, BezierConnection connection) {
		connections.get(front)
			.put(connection.getKey(), connection);
		notifyUpdate();
		level.scheduleTick(worldPosition, getBlockState().getBlock(), 1);
	}

	public void removeConnection(boolean front, BlockPos target) {
		connections.get(front)
			.remove(target);
		notifyUpdate();

		// TODO remove TE when all connections removed. seems tricky without a packet
		// because level.removeBlockEntity apparently no longer syncs

//		if (connections.getFirst()
//			.isEmpty()
//			&& connections.getSecond()
//				.isEmpty()) {
//			BlockState blockState = getBlockState();
//			if (!blockState.hasProperty(TrackBlock.HAS_TURN))
//				return;
//		}
	}

	public void removeInboundConnections() {
		connections.forEach(map -> map.values()
			.forEach(bc -> {
				BlockEntity blockEntity = level.getBlockEntity(bc.getKey());
				if (!(blockEntity instanceof TrackTileEntity))
					return;
				TrackTileEntity other = (TrackTileEntity) blockEntity;
				other.removeConnection(bc.trackEnds.getSecond(), bc.tePositions.getFirst());
			}));
	}

	@Override
	protected void write(CompoundTag tag, boolean clientPacket) {
		super.write(tag, clientPacket);

		CompoundTag connectionsTag = new CompoundTag();
		connections.forEachWithContext((map, first) -> {
			ListTag listTag = new ListTag();
			map.values()
				.forEach(e -> listTag.add(e.write()));
			connectionsTag.put(first ? "Front" : "Back", listTag);
		});

		tag.put("Connections", connectionsTag);
	}

	@Override
	protected void read(CompoundTag tag, boolean clientPacket) {
		super.read(tag, clientPacket);

		CompoundTag connectionsTag = tag.getCompound("Connections");
		connections.forEach(Map::clear);
		connections.forEachWithContext((map, first) -> connectionsTag.getList(first ? "Front" : "Back", 10)
			.forEach(t -> {
				if (!(t instanceof CompoundTag))
					return;
				BezierConnection connection = new BezierConnection((CompoundTag) t);
				map.put(connection.getKey(), connection);
			}));

		DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> InstancedRenderDispatcher.enqueueUpdate(this));
	}

	@Override
	public AABB getRenderBoundingBox() {
		return INFINITE_EXTENT_AABB;
	}

	@Override
	public void addBehaviours(List<TileEntityBehaviour> behaviours) {}

}
