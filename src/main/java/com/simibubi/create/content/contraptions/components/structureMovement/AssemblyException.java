package com.simibubi.create.content.contraptions.components.structureMovement;

import com.simibubi.create.foundation.config.AllConfigs;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

public class AssemblyException extends Exception {
	public final ITextComponent component;

	public AssemblyException(ITextComponent component) {
		this.component = component;
	}

	public AssemblyException(String langKey, Object... objects) {
		this(new TranslationTextComponent("create.gui.assembly.exception." + langKey, objects));
	}

	public static AssemblyException unmovableBlock(BlockPos pos, BlockState state) {
		return new AssemblyException("unmovableBlock",
				pos.getX(),
				pos.getY(),
				pos.getZ(),
				new TranslationTextComponent(state.getBlock().getTranslationKey()));
	}

	public static AssemblyException unloadedChunk(BlockPos pos) {
		return new AssemblyException("chunkNotLoaded",
				pos.getX(),
				pos.getY(),
				pos.getZ());
	}

	public static AssemblyException structureTooLarge() {
		return new AssemblyException("structureTooLarge",
				AllConfigs.SERVER.kinetics.maxBlocksMoved.get());
	}

	public static AssemblyException tooManyPistonPoles() {
		return new AssemblyException("tooManyPistonPoles",
				AllConfigs.SERVER.kinetics.maxPistonPoles.get());
	}

	public static AssemblyException noPistonPoles() {
		return new AssemblyException("noPistonPoles");
	}

	public String getFormattedText() {
		return component.getFormattedText();
	}
}
