package com.simibubi.create.content.redstone.displayLink;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

public class DisplayLinkContext {

	private Level level;
	private DisplayLinkBlockEntity blockEntity;

	public Object flapDisplayContext;

	public DisplayLinkContext(Level level, DisplayLinkBlockEntity blockEntity) {
		this.level = level;
		this.blockEntity = blockEntity;
	}

	public Level level() {
		return level;
	}

	public DisplayLinkBlockEntity blockEntity() {
		return blockEntity;
	}

	public BlockEntity getSourceBlockEntity() {
		return level.getBlockEntity(getSourcePos());
	}

	public BlockPos getSourcePos() {
		return blockEntity.getSourcePosition();
	}

	public BlockEntity getTargetBlockEntity() {
		return level.getBlockEntity(getTargetPos());
	}

	public BlockPos getTargetPos() {
		return blockEntity.getTargetPosition();
	}

	public CompoundTag sourceConfig() {
		return blockEntity.getSourceConfig();
	}

}
