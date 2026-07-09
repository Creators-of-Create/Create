package com.simibubi.create.foundation.blockEntity;

import javax.annotation.ParametersAreNonnullByDefault;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;

import com.simibubi.create.foundation.utility.LegacyBlockEntityTagBridge;

@ParametersAreNonnullByDefault
public abstract class SyncedBlockEntity extends BlockEntity {
	public SyncedBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
	}

	@Override
	public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
		return writeClient(new CompoundTag(), registries);
	}

	@Override
	public ClientboundBlockEntityDataPacket getUpdatePacket() {
		return ClientboundBlockEntityDataPacket.create(this);
	}

	@Override
	public void handleUpdateTag(ValueInput input) {
		readClient(LegacyBlockEntityTagBridge.read(input), input.lookup());
	}

	@Override
	public void onDataPacket(Connection net, ValueInput input) {
		readClient(LegacyBlockEntityTagBridge.read(input), input.lookup());
	}

	// Special handling for client update packets
	public void readClient(CompoundTag tag, HolderLookup.Provider registries) {
		loadAdditional(LegacyBlockEntityTagBridge.input(tag, registries));
	}

	// Special handling for client update packets
	public CompoundTag writeClient(CompoundTag tag, HolderLookup.Provider registries) {
		tag.merge(LegacyBlockEntityTagBridge.output(registries, this::saveAdditional));
		return tag;
	}

	public void sendData() {
		if (level instanceof ServerLevel serverLevel)
			serverLevel.getChunkSource().blockChanged(getBlockPos());
	}

	public void notifyUpdate() {
		setChanged();
		sendData();
	}

	public HolderGetter<Block> blockHolderGetter() {
		return level != null ? level.registryAccess().lookupOrThrow(Registries.BLOCK) : BuiltInRegistries.BLOCK;
	}
}
