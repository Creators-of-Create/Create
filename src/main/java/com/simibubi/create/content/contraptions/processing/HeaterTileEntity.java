package com.simibubi.create.content.contraptions.processing;

import java.util.List;

import com.simibubi.create.foundation.tileEntity.SmartTileEntity;
import com.simibubi.create.foundation.tileEntity.TileEntityBehaviour;

import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.common.ForgeHooks;

public class HeaterTileEntity extends SmartTileEntity {

	int fuelLevel;
	private int burnTimeRemaining;
	private int bufferedHeatLevel;

	public HeaterTileEntity(TileEntityType<? extends HeaterTileEntity> tileEntityTypeIn) {
		super(tileEntityTypeIn);
		fuelLevel = 0;
		burnTimeRemaining = 0;
		bufferedHeatLevel = 1;
	}

	@Override
	public void tick() {
		super.tick();
		if (burnTimeRemaining > 0) {
			burnTimeRemaining--;
			if (burnTimeRemaining == 0 && fuelLevel > 0) {
				fuelLevel--;
				sendData();
			}
			markDirty();
		}
	}

	@Override
	public void lazyTick() {
		super.lazyTick();
		updateHeatLevel();
	}

	@Override
	public void addBehaviours(List<TileEntityBehaviour> behaviours) {}

	@Override
	public CompoundNBT write(CompoundNBT compound) {
		compound.putInt("fuelLevel", fuelLevel);
		compound.putInt("burnTimeRemaining", burnTimeRemaining);
		return super.write(compound);
	}

	@Override
	public void read(CompoundNBT compound) {
		fuelLevel = compound.getInt("fuelLevel");
		burnTimeRemaining = compound.getInt("burnTimeRemaining");
		super.read(compound);
		if (fuelLevel == 0)
			burnTimeRemaining = 0;
		updateHeatLevel();
	}

	boolean tryUpdateFuel(ItemStack itemStack) {
		int burnTime = itemStack.getItem() == Items.EGG ? 150 : itemStack.getItem()
			.getBurnTime(itemStack);
		if (burnTime == -1)
			burnTime = ForgeHooks.getBurnTime(itemStack);
		if (burnTime <= 0)
			return false;
		
		int newFuelLevel = 1; // todo: + (itemStack.getItem() == AllItems.SUPER_SPECIAL_FUEL.get() ? 1 : 0);
		if (newFuelLevel < fuelLevel ^ burnTime <= burnTimeRemaining) {
			return false;
		}
		burnTimeRemaining = burnTime;
		fuelLevel = newFuelLevel;
		updateHeatLevel();
		return true;
	}

	public int getHeatLevel() {
		return bufferedHeatLevel;
	}

	private void updateHeatLevel() {
		int newHeatLevel = 1 + fuelLevel;
		if (newHeatLevel != bufferedHeatLevel) {
			bufferedHeatLevel = newHeatLevel;
			markDirty();
			if(world != null)
				sendData();
		}
	}
}
