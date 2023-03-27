package com.simibubi.create.content.curiosities.clipboard;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;

public interface ClipboardCloneable {

	public String getClipboardKey();
	
	public boolean writeToClipboard(CompoundTag tag, Direction side);
	
	public boolean readFromClipboard(CompoundTag tag, Player player, Direction side, boolean simulate);
	
}
