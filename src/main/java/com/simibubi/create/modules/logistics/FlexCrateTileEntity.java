package com.simibubi.create.modules.logistics;

import com.simibubi.create.AllTileEntities;
import com.simibubi.create.foundation.block.SyncedTileEntity;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

public class FlexCrateTileEntity extends SyncedTileEntity implements INamedContainerProvider {

	public FlexCrateTileEntity() {
		super(AllTileEntities.FLEX_CRATE.type);
	}

	@Override
	public Container createMenu(int id, PlayerInventory inventory, PlayerEntity player) {
		return new FlexCrateContainer(id, inventory, this);
	}

	@Override
	public ITextComponent getDisplayName() {
		return new StringTextComponent(getType().getRegistryName().toString());
	}

}
