package com.simibubi.create.content.logistics.block.redstone;

import java.util.List;

import com.simibubi.create.foundation.tileEntity.SmartTileEntity;
import com.simibubi.create.foundation.tileEntity.TileEntityBehaviour;

import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

public class StockpileSwitchTileEntity extends SmartTileEntity {

	public float onWhenAbove;
	public float offWhenBelow;
	public float currentLevel;
	public boolean powered;
	private LazyOptional<IItemHandler> observedInventory;

	public StockpileSwitchTileEntity(TileEntityType<?> typeIn) {
		super(typeIn);
		onWhenAbove = .75f;
		offWhenBelow = .25f;
		currentLevel = -1;
		powered = false;
		observedInventory = LazyOptional.empty();
		setLazyTickRate(10);
	}

	@Override
	protected void read(CompoundNBT compound, boolean clientPacket) {
		onWhenAbove = compound.getFloat("OnAbove");
		offWhenBelow = compound.getFloat("OffBelow");
		currentLevel = compound.getFloat("Current");
		powered = compound.getBoolean("Powered");

		super.read(compound, clientPacket);
	}

	@Override
	public void write(CompoundNBT compound, boolean clientPacket) {
		compound.putFloat("OnAbove", onWhenAbove);
		compound.putFloat("OffBelow", offWhenBelow);
		compound.putFloat("Current", currentLevel);
		compound.putBoolean("Powered", powered);

		super.write(compound, clientPacket);
	}

	public float getLevel() {
		return currentLevel;
	}

	public void updateCurrentLevel() {
		if (!observedInventory.isPresent()) {
			if (!findNewInventory() && currentLevel != -1) {
				world.setBlockState(pos, getBlockState().with(StockpileSwitchBlock.INDICATOR, 0), 3);
				currentLevel = -1;
				powered = false;
				world.notifyNeighbors(pos, getBlockState().getBlock());
			}
			return;
		}

		float occupied = 0;
		float totalSpace = 0;
		IItemHandler inv = observedInventory.orElse(null);

		for (int slot = 0; slot < inv.getSlots(); slot++) {
			ItemStack stackInSlot = inv.getStackInSlot(slot);
			int space = Math.min(stackInSlot.getMaxStackSize(), inv.getSlotLimit(slot));
			int count = stackInSlot.getCount();

			if (space == 0)
				continue;

			totalSpace += 1;
			occupied += count * (1f / space);
		}

		currentLevel = (float) occupied / totalSpace;
		currentLevel = MathHelper.clamp(currentLevel, 0, 1);

		boolean previouslyPowered = powered;
		if (powered && currentLevel <= offWhenBelow)
			powered = false;
		else if (!powered && currentLevel >= onWhenAbove)
			powered = true;
		boolean update = previouslyPowered != powered;

		int displayLevel = 0;
		if (currentLevel > 0)
			displayLevel = (int) (currentLevel * 6);
		world.setBlockState(pos, getBlockState().with(StockpileSwitchBlock.INDICATOR, displayLevel), update ? 3 : 2);
		if (update)
			world.notifyNeighbors(pos, getBlockState().getBlock());
	}

	@Override
	public void lazyTick() {
		super.lazyTick();
		if (world.isRemote)
			return;
		findNewInventory();
		updateCurrentLevel();
	}

	private boolean findNewInventory() {
		observedInventory = LazyOptional.empty();
		BlockPos invPos = getPos().offset(getBlockState().get(BlockStateProperties.HORIZONTAL_FACING));

		if (!world.isBlockPresent(invPos))
			return false;
		BlockState invState = world.getBlockState(invPos);

		if (!invState.hasTileEntity())
			return false;
		TileEntity invTE = world.getTileEntity(invPos);
		if (invTE == null)
			return false;

		observedInventory = invTE.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY);
		if (observedInventory.isPresent()) {
			updateCurrentLevel();
			return true;
		}

		return false;
	}

	@Override
	public void addBehaviours(List<TileEntityBehaviour> behaviours) {

	}

}
