package com.simibubi.create.foundation.tileEntity;

import javax.annotation.ParametersAreNonnullByDefault;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.math.SectionPos;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.fml.network.PacketDistributor;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public abstract class SyncedTileEntity extends TileEntity {

	public SyncedTileEntity(TileEntityType<?> tileEntityTypeIn) {
		super(tileEntityTypeIn);
	}

	@Override
	public CompoundNBT getTileData() {
		return super.getTileData();
	}

	@Override
	public CompoundNBT getUpdateTag() {
		return save(new CompoundNBT());
	}

	@Override
	public void handleUpdateTag(BlockState state, CompoundNBT tag) {
		load(state, tag);
	}

	public void sendData() {
		if (level != null)
			level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 2 | 4 | 16);
	}

	public void causeBlockUpdate() {
		if (level != null)
			level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 1);
	}

	@Override
	public SUpdateTileEntityPacket getUpdatePacket() {
		return new SUpdateTileEntityPacket(getBlockPos(), 1, writeToClient(new CompoundNBT()));
	}

	@Override
	public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt) {
		readClientUpdate(getBlockState(), pkt.getTag());
	}

	// Special handling for client update packets
	public void readClientUpdate(BlockState state, CompoundNBT tag) {
		load(state, tag);
	}

	// Special handling for client update packets
	public CompoundNBT writeToClient(CompoundNBT tag) {
		return save(tag);
	}

	public void notifyUpdate() {
		setChanged();
		sendData();
	}

	public PacketDistributor.PacketTarget packetTarget() {
		return PacketDistributor.TRACKING_CHUNK.with(this::containedChunk);
	}

	public Chunk containedChunk() {
		SectionPos sectionPos = SectionPos.of(worldPosition);
		return level.getChunk(sectionPos.x(), sectionPos.z());
	}
}
