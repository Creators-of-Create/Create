package com.simibubi.create.modules.kinetics.relays;

import com.simibubi.create.AllTileEntities;
import com.simibubi.create.modules.kinetics.base.KineticTileEntity;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.math.BlockPos;

public class BeltPulleyTileEntity extends KineticTileEntity {

	protected boolean controller;
	protected BlockPos target;
	
	public BeltPulleyTileEntity() {
		super(AllTileEntities.BELT_PULLEY.type);
		controller = false;
		target = BlockPos.ZERO;
	}
	
	@Override
	public CompoundNBT write(CompoundNBT compound) {
		compound.putBoolean("Controller", isController());
		compound.put("Target", NBTUtil.writeBlockPos(target));
		return super.write(compound);
	}
	
	@Override
	public void read(CompoundNBT compound) {
		controller = compound.getBoolean("Controller");
		target = NBTUtil.readBlockPos(compound.getCompound("Target"));
		super.read(compound);
	}
	
	public void setController(boolean controller) {
		this.controller = controller;
	}
	
	public boolean isController() {
		return controller;
	}
	
	public void setTarget(BlockPos target) {
		this.target = target;
	}
	
	public BlockPos getTarget() {
		return target;
	}
	
}
