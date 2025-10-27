package com.simibubi.create.impl.contraption.storage;

import com.simibubi.create.api.contraption.storage.item.simple.SimpleMountedStorageType;

import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import net.neoforged.neoforge.items.IItemHandler;

public class FallbackMountedStorageType extends SimpleMountedStorageType<FallbackMountedStorage> {
	public FallbackMountedStorageType() {
		super(FallbackMountedStorage.CODEC);
	}

	@Override
	protected IItemHandler getHandler(Level level, BlockEntity be) {
		IItemHandler handler = super.getHandler(level, be);
		return handler != null && FallbackMountedStorage.isValid(handler) ? handler : null;
	}
}
