package com.simibubi.create.compat.thresholdSwitch;

import com.simibubi.create.compat.Mods;
import com.simibubi.create.foundation.utility.RegisteredObjects;

import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.items.IItemHandler;

public class StorageDrawers implements ThresholdSwitchCompat {

	@Override
	public boolean isFromThisMod(BlockEntity blockEntity) {
		return blockEntity != null && Mods.STORAGEDRAWERS.id()
			.equals(RegisteredObjects.getKeyOrThrow(blockEntity.getType())
				.getNamespace());
	}

	@Override
	public long getSpaceInSlot(IItemHandler inv, int slot) {
		if (slot == 0)
			return 0;

		return inv.getSlotLimit(slot);
	}
}
