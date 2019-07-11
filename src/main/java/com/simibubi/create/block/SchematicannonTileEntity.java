package com.simibubi.create.block;

import com.simibubi.create.AllTileEntities;
import com.simibubi.create.utility.TileEntitySynced;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntityType;

public class SchematicannonTileEntity extends TileEntitySynced implements ITickableTileEntity {

	private int test = 0;
	
	public SchematicannonTileEntity() {
		super(AllTileEntities.Schematicannon.type);
	}
	
	public SchematicannonTileEntity(TileEntityType<?> tileEntityTypeIn) {
		super(tileEntityTypeIn);
	}
	
	@Override
	public void read(CompoundNBT compound) {
		super.read(compound);
		test = compound.getInt("Test");
	}
	
	@Override
	public CompoundNBT write(CompoundNBT compound) {
		compound.putInt("Test", test);
		return super.write(compound);
	}
	
	public int getTest() {
		return test;
	}
	
	public void setTest(int test) {
		this.test = test;
	}

	@Override
	public void tick() {
		test++;
	}
	

}
