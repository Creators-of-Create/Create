package com.simibubi.create.modules.kinetics.relays;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllTileEntities;
import com.simibubi.create.modules.kinetics.base.KineticTileEntity;
import com.simibubi.create.modules.kinetics.relays.BeltBlock.Part;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.math.BlockPos;

public class BeltTileEntity extends KineticTileEntity {

	protected BlockPos controller;

	public BeltTileEntity() {
		super(AllTileEntities.BELT.type);
		controller = BlockPos.ZERO;
	}

	@Override
	public CompoundNBT write(CompoundNBT compound) {
		compound.put("Controller", NBTUtil.writeBlockPos(controller));
		return super.write(compound);
	}

	@Override
	public void read(CompoundNBT compound) {
		controller = NBTUtil.readBlockPos(compound.getCompound("Controller"));
		super.read(compound);
	}

	public void setController(BlockPos controller) {
		this.controller = controller;
	}

	public BlockPos getController() {
		return controller;
	}

	public boolean isController() {
		return controller.equals(pos);
	}

	public boolean hasPulley() {
		if (!AllBlocks.BELT.typeOf(getBlockState()))
			return false;
		return getBlockState().get(BeltBlock.PART) == Part.END || getBlockState().get(BeltBlock.PART) == Part.START;
	}

}
