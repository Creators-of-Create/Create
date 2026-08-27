package com.simibubi.create.content.processing.basin;

import com.simibubi.create.api.contraption.storage.item.MountedItemStorageType;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import org.jetbrains.annotations.Nullable;

public class BasinMountedItemStorageType extends MountedItemStorageType<BasinMountedItemStorage> {
	public BasinMountedItemStorageType() {
		super(BasinMountedItemStorage.CODEC);
	}

	@Override
	public @Nullable BasinMountedItemStorage mount(Level level, BlockState state, BlockPos pos, @Nullable BlockEntity be) {
		return be instanceof BasinBlockEntity basin ? BasinMountedItemStorage.fromBasin(basin) : null;
	}
}
