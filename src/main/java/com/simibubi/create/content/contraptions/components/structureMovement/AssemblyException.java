package com.simibubi.create.content.contraptions.components.structureMovement;

import com.simibubi.create.foundation.config.AllConfigs;

import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

public class AssemblyException extends Exception {

	private static final long serialVersionUID = 1L;
	public final ITextComponent component;
	private BlockPos position = null;

	public static void write(CompoundNBT compound, AssemblyException exception) {
		if (exception == null)
			return;

		CompoundNBT nbt = new CompoundNBT();
		nbt.putString("Component", ITextComponent.Serializer.toJson(exception.component));
		if (exception.hasPosition())
			nbt.putLong("Position", exception.getPosition()
				.asLong());

		compound.put("LastException", nbt);
	}

	public static AssemblyException read(CompoundNBT compound) {
		if (!compound.contains("LastException"))
			return null;

		CompoundNBT nbt = compound.getCompound("LastException");
		String string = nbt.getString("Component");
		AssemblyException exception = new AssemblyException(ITextComponent.Serializer.fromJson(string));
		if (nbt.contains("Position"))
			exception.position = BlockPos.of(nbt.getLong("Position"));

		return exception;
	}

	public AssemblyException(ITextComponent component) {
		this.component = component;
	}

	public AssemblyException(String langKey, Object... objects) {
		this(new TranslationTextComponent("create.gui.assembly.exception." + langKey, objects));
	}

	public static AssemblyException unmovableBlock(BlockPos pos, BlockState state) {
		AssemblyException e = new AssemblyException("unmovableBlock", pos.getX(), pos.getY(), pos.getZ(),
			new TranslationTextComponent(state.getBlock()
				.getDescriptionId()));
		e.position = pos;
		return e;
	}

	public static AssemblyException unloadedChunk(BlockPos pos) {
		AssemblyException e = new AssemblyException("chunkNotLoaded", pos.getX(), pos.getY(), pos.getZ());
		e.position = pos;
		return e;
	}

	public static AssemblyException structureTooLarge() {
		return new AssemblyException("structureTooLarge", AllConfigs.SERVER.kinetics.maxBlocksMoved.get());
	}

	public static AssemblyException tooManyPistonPoles() {
		return new AssemblyException("tooManyPistonPoles", AllConfigs.SERVER.kinetics.maxPistonPoles.get());
	}

	public static AssemblyException noPistonPoles() {
		return new AssemblyException("noPistonPoles");
	}

	public static AssemblyException notEnoughSails(int sails) {
		return new AssemblyException("not_enough_sails", sails, AllConfigs.SERVER.kinetics.minimumWindmillSails.get());
	}

	public boolean hasPosition() {
		return position != null;
	}

	public BlockPos getPosition() {
		return position;
	}
}
