package com.simibubi.create.content.logistics.block.data;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;

public class DataGathererContext {

	private LevelAccessor level;
	private DataGathererTileEntity te;
	
	public Object flapDisplayContext;

	public DataGathererContext(LevelAccessor level, DataGathererTileEntity te) {
		this.level = level;
		this.te = te;
	}

	public LevelAccessor level() {
		return level;
	}
	
	public DataGathererTileEntity te() {
		return te;
	}
	
	public BlockEntity getSourceTE() {
		return level.getBlockEntity(getSourcePos());
	}

	public BlockPos getSourcePos() {
		return te.getSourcePosition();
	}

	public BlockEntity getTargetTE() {
		return level.getBlockEntity(getTargetPos());
	}

	public BlockPos getTargetPos() {
		return te.getTargetPosition();
	}

	public CompoundTag sourceConfig() {
		return te.getSourceConfig();
	}

}
