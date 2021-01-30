package com.simibubi.create.content.contraptions.components.structureMovement;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

public class AssemblyException extends RuntimeException {
	public final ITextComponent message;

	public AssemblyException(ITextComponent message) {
		this.message = message;
	}

	public AssemblyException(String langKey, Object... objects) {
		this(new TranslationTextComponent("gui.goggles.contraptions." + langKey, objects));
	}

	public static AssemblyException unmovableBlock(BlockPos pos, BlockState state) {
		return new AssemblyException("unmovableBlock", pos.getX(), pos.getY(), pos.getZ(),
				new TranslationTextComponent(state.getBlock().getTranslationKey()));
	}
}
