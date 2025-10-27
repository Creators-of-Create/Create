package com.simibubi.create.content.equipment.clipboard;

import org.jetbrains.annotations.NotNull;

import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;

public interface ClipboardCloneable {
	String getClipboardKey();

	boolean writeToClipboard(@NotNull HolderLookup.Provider registries, CompoundTag tag, Direction side);

	boolean readFromClipboard(@NotNull HolderLookup.Provider registries, CompoundTag tag, Player player, Direction side, boolean simulate);
}
