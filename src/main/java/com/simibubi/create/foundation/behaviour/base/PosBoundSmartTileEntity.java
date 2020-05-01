package com.simibubi.create.foundation.behaviour.base;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.math.BlockPos;

public abstract class PosBoundSmartTileEntity extends SmartTileEntity {

	private boolean newPositionVisited;

	public PosBoundSmartTileEntity(TileEntityType<?> tileEntityTypeIn) {
		super(tileEntityTypeIn);
		newPositionVisited = true;
	}

	@Override
	public void initialize() {
		if (!world.isRemote && newPositionVisited) {
			newPositionVisited = false;
			initInNewPosition();
		}
		super.initialize();
	}

	@Override
	public void read(CompoundNBT compound) {
		long positionInTag = new BlockPos(compound.getInt("x"), compound.getInt("y"), compound.getInt("z")).toLong();
		long positionKey = compound.getLong("BoundPosition");

		newPositionVisited = false;
		if (!hasWorld() || !world.isRemote) {
			if (positionInTag != positionKey) {
				removePositionDependentData(compound);
				newPositionVisited = true;
			}
		}

		super.read(compound);
	}

	/**
	 * Server-only. When this TE realizes, that it's reading its tag in a different
	 * position, it should remove all position dependent information here.
	 * 
	 * @param nbt
	 */
	protected void removePositionDependentData(CompoundNBT nbt) {

	}

	/**
	 * Server-only. When a TE has been created or moved, it will call this before the
	 * regular init.
	 */
	protected void initInNewPosition() {

	}

}
