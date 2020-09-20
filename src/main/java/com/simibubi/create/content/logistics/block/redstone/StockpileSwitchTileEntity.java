package com.simibubi.create.content.logistics.block.redstone;

import java.util.List;

import com.simibubi.create.foundation.tileEntity.SmartTileEntity;
import com.simibubi.create.foundation.tileEntity.TileEntityBehaviour;
import com.simibubi.create.foundation.tileEntity.behaviour.filtering.FilteringBehaviour;
import com.simibubi.create.foundation.tileEntity.behaviour.inventory.InvManipulationBehaviour;
import com.simibubi.create.foundation.tileEntity.behaviour.inventory.InvManipulationBehaviour.InterfaceProvider;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.items.IItemHandler;

public class StockpileSwitchTileEntity extends SmartTileEntity {

	public float onWhenAbove;
	public float offWhenBelow;
	public float currentLevel;
	public boolean powered;

	private FilteringBehaviour filtering;
	private InvManipulationBehaviour observedInventory;

	public StockpileSwitchTileEntity(TileEntityType<?> typeIn) {
		super(typeIn);
		onWhenAbove = .75f;
		offWhenBelow = .25f;
		currentLevel = -1;
		powered = false;
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
		if (!observedInventory.hasInventory()) {
			if (currentLevel == -1)
				return;
			world.setBlockState(pos, getBlockState().with(StockpileSwitchBlock.INDICATOR, 0), 3);
			currentLevel = -1;
			powered = false;
			world.notifyNeighbors(pos, getBlockState().getBlock());
			return;
		}

		float occupied = 0;
		float totalSpace = 0;
		IItemHandler inv = observedInventory.getInventory();

		for (int slot = 0; slot < inv.getSlots(); slot++) {
			ItemStack stackInSlot = inv.getStackInSlot(slot);
			int space = Math.min(stackInSlot.getMaxStackSize(), inv.getSlotLimit(slot));
			int count = stackInSlot.getCount();

			if (space == 0)
				continue;

			totalSpace += 1;

			if (filtering.test(stackInSlot))
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
		updateCurrentLevel();
	}

	@Override
	public void addBehaviours(List<TileEntityBehaviour> behaviours) {
		filtering = new FilteringBehaviour(this, new FilteredDetectorFilterSlot()).moveText(new Vec3d(0, 5, 0))
			.withCallback($ -> updateCurrentLevel());
		behaviours.add(filtering);

		observedInventory = new InvManipulationBehaviour(this, InterfaceProvider.towardBlockFacing()).bypassSidedness();
		behaviours.add(observedInventory);
	}
}
