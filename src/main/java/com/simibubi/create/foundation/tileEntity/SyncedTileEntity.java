package com.simibubi.create.foundation.tileEntity;

import javax.annotation.ParametersAreNonnullByDefault;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.fml.network.PacketDistributor;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public abstract class SyncedTileEntity extends BlockEntity {

	public SyncedTileEntity(BlockEntityType<?> tileEntityTypeIn) {
		super(tileEntityTypeIn);
	}

	@Override
	public CompoundTag getTileData() {
		return super.getTileData();
	}

	@Override
	public CompoundTag getUpdateTag() {
		return save(new CompoundTag());
	}

	@Override
	public void handleUpdateTag(BlockState state, CompoundTag tag) {
		load(state, tag);
	}

	public void sendData() {
		if (level != null && !level.isClientSide)
			level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 2 | 4 | 16);
	}

	public void causeBlockUpdate() {
		if (level != null)
			level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 1);
	}

	@Override
	public ClientboundBlockEntityDataPacket getUpdatePacket() {
		return new ClientboundBlockEntityDataPacket(getBlockPos(), 0, writeToClient(new CompoundTag()));
	}

	@Override
	public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
		readClientUpdate(getBlockState(), pkt.getTag());
	}

	// Special handling for client update packets
	public void readClientUpdate(BlockState state, CompoundTag tag) {
		load(state, tag);
	}

	// Special handling for client update packets
	public CompoundTag writeToClient(CompoundTag tag) {
		return save(tag);
	}

	public void notifyUpdate() {
		setChanged();
		sendData();
	}

	public PacketDistributor.PacketTarget packetTarget() {
		return PacketDistributor.TRACKING_CHUNK.with(this::containedChunk);
	}

	public LevelChunk containedChunk() {
		SectionPos sectionPos = SectionPos.of(worldPosition);
		return level.getChunk(sectionPos.x(), sectionPos.z());
	}
}
