package com.simibubi.create.content.contraptions;

import com.simibubi.create.foundation.utility.Lang;
import com.simibubi.create.infrastructure.config.AllConfigs;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.state.BlockState;

public class AssemblyException extends Exception {

	private static final long serialVersionUID = 1L;
	public final Component component;
	private BlockPos position = null;

	public static void write(CompoundTag compound, AssemblyException exception) {
		if (exception == null)
			return;

		CompoundTag nbt = new CompoundTag();
		nbt.putString("Component", Component.Serializer.toJson(exception.component));
		if (exception.hasPosition())
			nbt.putLong("Position", exception.getPosition()
				.asLong());

		compound.put("LastException", nbt);
	}

	public static AssemblyException read(CompoundTag compound) {
		if (!compound.contains("LastException"))
			return null;

		CompoundTag nbt = compound.getCompound("LastException");
		String string = nbt.getString("Component");
		AssemblyException exception = new AssemblyException(Component.Serializer.fromJson(string));
		if (nbt.contains("Position"))
			exception.position = BlockPos.of(nbt.getLong("Position"));

		return exception;
	}

	public AssemblyException(Component component) {
		this.component = component;
	}

	public AssemblyException(String langKey, Object... objects) {
		this(Lang.translateDirect("gui.assembly.exception." + langKey, objects));
	}

	public static AssemblyException unmovableBlock(BlockPos pos, BlockState state) {
		AssemblyException e = new AssemblyException("unmovableBlock", pos.getX(), pos.getY(), pos.getZ(),
			state.getBlock().getName());
		e.position = pos;
		return e;
	}

	public static AssemblyException unloadedChunk(BlockPos pos) {
		AssemblyException e = new AssemblyException("chunkNotLoaded", pos.getX(), pos.getY(), pos.getZ());
		e.position = pos;
		return e;
	}

	public static AssemblyException structureTooLarge() {
		return new AssemblyException("structureTooLarge", AllConfigs.server().kinetics.maxBlocksMoved.get());
	}

	public static AssemblyException tooManyPistonPoles() {
		return new AssemblyException("tooManyPistonPoles", AllConfigs.server().kinetics.maxPistonPoles.get());
	}

	public static AssemblyException noPistonPoles() {
		return new AssemblyException("noPistonPoles");
	}
	
	public static AssemblyException notEnoughSails(int sails) {
		return new AssemblyException("not_enough_sails", sails, AllConfigs.server().kinetics.minimumWindmillSails.get());
	}

	public boolean hasPosition() {
		return position != null;
	}

	public BlockPos getPosition() {
		return position;
	}
}
