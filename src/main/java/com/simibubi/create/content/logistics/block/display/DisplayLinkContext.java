package com.simibubi.create.content.logistics.block.display;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

public class DisplayLinkContext {

	private Level level;
	private DisplayLinkTileEntity te;

	public Object flapDisplayContext;

	public DisplayLinkContext(Level level, DisplayLinkTileEntity te) {
		this.level = level;
		this.te = te;
	}

	public Level level() {
		return level;
	}

	public DisplayLinkTileEntity te() {
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
