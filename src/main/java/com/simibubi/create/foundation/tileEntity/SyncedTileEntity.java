package com.simibubi.create.foundation.tileEntity;

import javax.annotation.ParametersAreNonnullByDefault;

import com.simibubi.create.lib.block.CustomDataPacketHandlingBlockEntity;
import com.simibubi.create.lib.extensions.BlockEntityExtensions;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public abstract class SyncedTileEntity extends BlockEntity implements BlockEntityExtensions, CustomDataPacketHandlingBlockEntity {

	public SyncedTileEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
	}

	@Override
	public CompoundTag create$save(CompoundTag tag) {
		BlockEntityExtensions.super.create$save(tag);
		saveAdditional(tag);
		return tag;
	}

	@Override
	public CompoundTag getUpdateTag() {
		return writeClient(new CompoundTag());
	}

	@Override
	public ClientboundBlockEntityDataPacket getUpdatePacket() {
		return ClientboundBlockEntityDataPacket.create(this);
	}

	//@Override
//	public void handleUpdateTag(CompoundTag tag) {
//		readClient(tag);
//	}

	@Override
	public void onDataPacket(Connection connection, ClientboundBlockEntityDataPacket packet) {
		CompoundTag tag = packet.getTag();
		if (tag != null) {
			readClient(tag);
		}
	}

	// Special handling for client update packets
	public void readClient(CompoundTag tag) {
		load(tag);
	}

	// Special handling for client update packets
	public CompoundTag writeClient(CompoundTag tag) {
		return create$save(tag);
	}

	public void sendData() {
		if (level != null && !level.isClientSide)
			level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 2 | 4 | 16);
	}

	public void causeBlockUpdate() {
		if (level != null)
			level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 1);
	}

	public void notifyUpdate() {
		setChanged();
		sendData();
	}

//	public PacketDistributor.PacketTarget packetTarget() {
//		return PacketDistributor.TRACKING_CHUNK.with(this::containedChunk);
//	}

	public LevelChunk containedChunk() {
		return level.getChunkAt(worldPosition);
	}

	@Override
	public void create$deserializeNBT(BlockState state, CompoundTag nbt) {
		this.load(nbt);
	}
}
