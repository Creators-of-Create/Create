package com.simibubi.create.content.logistics.block.mechanicalArm;

import com.simibubi.create.content.contraptions.base.KineticTileEntity;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntityType;

public class ArmTileEntity extends KineticTileEntity {

	boolean debugRave;
	
	public ArmTileEntity(TileEntityType<?> typeIn) {
		super(typeIn);
	}
	
	@Override
	public void lazyTick() {
		if (hasWorld()) 
			if (world.rand.nextInt(100) == 0) 
				toggleRave();
		super.lazyTick();
	}

	public void toggleRave() {
		if (world.isRemote)
			return;
		debugRave = !debugRave;
		markDirty();
		sendData();
	}
	
	@Override
	public CompoundNBT write(CompoundNBT compound) {
		super.write(compound);
		compound.putBoolean("DebugRave", debugRave);
		return compound;
	}
	
	@Override
	public void read(CompoundNBT compound) {
		super.read(compound);
		debugRave = compound.getBoolean("DebugRave");
	}

}
