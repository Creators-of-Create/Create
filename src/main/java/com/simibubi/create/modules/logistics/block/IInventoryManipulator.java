package com.simibubi.create.modules.logistics.block;

import net.minecraft.block.BlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

public interface IInventoryManipulator {

	public default World getWorld() {
		return ((TileEntity) this).getWorld();
	}
	
	public default BlockPos getPos() {
		return ((TileEntity) this).getPos();
	}

	public BlockPos getInventoryPos();

	public LazyOptional<IItemHandler> getInventory();

	public void setInventory(LazyOptional<IItemHandler> inventory);

	default boolean findNewInventory() {
		BlockPos invPos = getInventoryPos();
		World world = getWorld();

		if (!world.isBlockPresent(invPos))
			return false;
		BlockState invState = world.getBlockState(invPos);

		if (!invState.hasTileEntity())
			return false;
		TileEntity invTE = world.getTileEntity(invPos);
		if (invTE == null)
			return false;
		
		LazyOptional<IItemHandler> inventory = invTE.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY);
		setInventory(inventory);
		if (inventory.isPresent()) {
			return true;
		}

		return false;
	}

	public default void neighborChanged() {
		boolean hasInventory = getInventory().isPresent();
		if (!hasInventory) {
			findNewInventory();
		}
	}

}
