package com.simibubi.create.content.contraptions.components.storage;

import com.simibubi.create.AllTileEntities;
import com.simibubi.create.api.contraption.ContraptionItemStackHandler;
import com.simibubi.create.api.contraption.ContraptionStorageRegistry;
import com.simibubi.create.content.logistics.block.inventories.CrateItemHandler;
import com.simibubi.create.content.logistics.block.inventories.AdjustableCrateBlock;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;

public class FlexCrateRegistry extends ContraptionStorageRegistry {
	@Override
	public Priority getPriority() {
		return Priority.NATIVE;
	}

	@Override
	public TileEntityType<?>[] affectedStorages() {
		return new TileEntityType[]{
				AllTileEntities.ADJUSTABLE_CRATE.get()
		};
	}

	@Override
	public ContraptionItemStackHandler createHandler(TileEntity te) {
		// Split double flexcrates
		if (te.getBlockState()
				.getValue(AdjustableCrateBlock.DOUBLE))
			te.getLevel()
					.setBlockAndUpdate(te.getBlockPos(), te.getBlockState()
							.setValue(AdjustableCrateBlock.DOUBLE, false));
		te.clearCache();

		return super.createHandler(te);
	}

	@Override
	public ContraptionItemStackHandler deserializeHandler(CompoundNBT nbt) {
		return deserializeHandler(new CrateItemHandler(), nbt);
	}
}
