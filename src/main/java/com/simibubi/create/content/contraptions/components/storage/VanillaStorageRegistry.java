package com.simibubi.create.content.contraptions.components.storage;

import com.simibubi.create.api.contraption.ContraptionItemStackHandler;
import com.simibubi.create.api.contraption.ContraptionStorageRegistry;

import net.minecraft.block.ChestBlock;
import net.minecraft.state.properties.ChestType;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;

public class VanillaStorageRegistry extends ContraptionStorageRegistry {
	@Override
	public Priority getPriority() {
		return Priority.NATIVE;
	}

	@Override
	public TileEntityType<?>[] affectedStorages() {
		return new TileEntityType[]{
				TileEntityType.CHEST,
				TileEntityType.TRAPPED_CHEST,
				TileEntityType.BARREL,
				TileEntityType.SHULKER_BOX,
		};
	}

	@Override
	public ContraptionItemStackHandler createHandler(TileEntity te) {
		// Split double chests
		if (te.getType() == TileEntityType.CHEST || te.getType() == TileEntityType.TRAPPED_CHEST) {
			if (te.getBlockState()
					.getValue(ChestBlock.TYPE) != ChestType.SINGLE)
				te.getLevel()
						.setBlockAndUpdate(te.getBlockPos(), te.getBlockState()
								.setValue(ChestBlock.TYPE, ChestType.SINGLE));
			te.clearCache();
		}

		return super.createHandler(te);
	}
}
