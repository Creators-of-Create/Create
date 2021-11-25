package com.simibubi.create.content.contraptions.components.storage;

import com.simibubi.create.AllTileEntities;
import com.simibubi.create.api.contraption.ContraptionItemStackHandler;
import com.simibubi.create.api.contraption.ContraptionStorageRegistry;
import com.simibubi.create.content.logistics.block.inventories.BottomlessItemHandler;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntityType;

public class CreativeCrateRegistry extends ContraptionStorageRegistry {
	@Override
	public Priority getPriority() {
		return Priority.NATIVE;
	}

	@Override
	public TileEntityType<?>[] affectedStorages() {
		return new TileEntityType[]{
				AllTileEntities.CREATIVE_CRATE.get()
		};
	}

	@Override
	public ContraptionItemStackHandler deserializeHandler(CompoundNBT nbt) {
		return deserializeHandler(new BottomlessItemHandler(() -> ItemStack.EMPTY), nbt);
	}
}
