package com.simibubi.create.foundation.tileEntity;

import com.simibubi.create.lib.block.CustomDataPacketHandlingTileEntity;
import com.simibubi.create.lib.extensions.BlockEntityExtensions;

import javax.annotation.ParametersAreNonnullByDefault;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public abstract class SyncedTileEntity extends BlockEntity implements BlockEntityExtensions, CustomDataPacketHandlingTileEntity {

	public SyncedTileEntity(BlockEntityType<?> tileEntityTypeIn, BlockPos pos, BlockState state) {
		super(tileEntityTypeIn, pos, state);
	}

	@Override
	public CompoundTag create$getExtraCustomData() {
		return ((BlockEntityExtensions) this).create$getExtraCustomData();
	}

	@Override
	public CompoundTag getUpdateTag() {
		return create$save(new CompoundTag());
	}

	@Override
	public CompoundTag create$save(CompoundTag tag) {
		saveAdditional(tag);
		return BlockEntityExtensions.super.create$save(tag);
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
		return ClientboundBlockEntityDataPacket.create(this,
			te -> ((SyncedTileEntity) te).writeToClient(new CompoundTag()));
	}

	@Override
	public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
		if (pkt.getTag() == null)
			return;
		readClientUpdate(pkt.getTag());
	}

	// Special handling for client update packets
	public void readClientUpdate(CompoundTag tag) {
		load(tag);
	}

	// Special handling for client update packets
	public CompoundTag writeToClient(CompoundTag tag) {
		return create$save(tag);
	}

	public void notifyUpdate() {
		setChanged();
		sendData();
	}

//	public PacketDistributor.PacketTarget packetTarget() {
//		return PacketDistributor.TRACKING_CHUNK.with(this::containedChunk);
//	}

	public LevelChunk containedChunk() {
		SectionPos sectionPos = SectionPos.of(worldPosition);
		return level.getChunk(sectionPos.x(), sectionPos.z());
	}

	@Override
	public void create$deserializeNBT(BlockState state, CompoundTag nbt) {
		this.load(nbt);
	}
}
