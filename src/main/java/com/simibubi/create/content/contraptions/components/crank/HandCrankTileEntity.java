package com.simibubi.create.content.contraptions.components.crank;

import com.simibubi.create.content.contraptions.base.GeneratingKineticTileEntity;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntityType;

public class HandCrankTileEntity extends GeneratingKineticTileEntity {

	public int inUse;
	public boolean backwards;
	public float independentAngle;
	public float chasingVelocity;

	public HandCrankTileEntity(TileEntityType<? extends HandCrankTileEntity> type) {
		super(type);
	}

	public void turn(boolean back) {
		boolean update = false;

		if (getGeneratedSpeed() == 0 || back != backwards)
			update = true;

		inUse = 10;
		this.backwards = back;
		if (update && !world.isRemote)
			updateGeneratedRotation();
	}

	@Override
	public float getGeneratedSpeed() {
		return inUse == 0 ? 0 : backwards ? -32 : 32;
	}
	
	@Override
	public CompoundNBT write(CompoundNBT compound) {
		compound.putInt("InUse", inUse);
		return super.write(compound);
	}
	
	@Override
	public void read(CompoundNBT compound) {
		inUse = compound.getInt("InUse");
		super.read(compound);
	}

	@Override
	public void tick() {
		super.tick();

		float actualSpeed = getSpeed();
		chasingVelocity += ((actualSpeed * 10 / 3f) - chasingVelocity) * .25f;
		independentAngle += chasingVelocity;

		if (inUse > 0) {
			inUse--;

			if (inUse == 0 && !world.isRemote)
				updateGeneratedRotation();
		}
	}

}
