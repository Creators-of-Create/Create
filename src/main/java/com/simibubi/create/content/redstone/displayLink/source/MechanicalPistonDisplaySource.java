package com.simibubi.create.content.redstone.displayLink.source;

import com.simibubi.create.content.contraptions.elevator.ElevatorPulleyBlockEntity;
import com.simibubi.create.content.contraptions.piston.LinearActuatorBlockEntity;
import com.simibubi.create.content.contraptions.piston.MechanicalPistonBlockEntity;
import com.simibubi.create.content.contraptions.pulley.PulleyBlockEntity;
import com.simibubi.create.content.redstone.displayLink.DisplayLinkContext;
import com.simibubi.create.content.redstone.displayLink.target.DisplayTargetStats;

import com.simibubi.create.foundation.gui.ModularGuiLineBuilder;
import com.simibubi.create.foundation.utility.Lang;

import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.level.block.entity.BlockEntity;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import org.jetbrains.annotations.Nullable;

public class MechanicalPistonDisplaySource extends PercentOrProgressBarDisplaySource {

	@Override
	protected MutableComponent provideLine(DisplayLinkContext context, DisplayTargetStats stats) {
		int mode = context.sourceConfig().getInt("Mode");
		BlockEntity be = context.getSourceBlockEntity();
		if (!(be instanceof MechanicalPistonBlockEntity mpbe))
			return null;
		return switch (mode) {
			case 2 -> new TextComponent(String.valueOf(mpbe.offset));
			case 3 -> new TextComponent(String.valueOf(mpbe.getExtensionRange()));
			default -> super.provideLine(context, stats);
		};
	}

	@Nullable
	@Override
	protected Float getProgress(DisplayLinkContext context) {
		BlockEntity be = context.getSourceBlockEntity();
		if (!(be instanceof MechanicalPistonBlockEntity mpbe))
			return null;
		return mpbe.offset/(float)mpbe.getExtensionRange();
	}

	@Override
	protected boolean progressBarActive(DisplayLinkContext context) {
		return context.sourceConfig().getInt("Mode") == 1;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void initConfigurationWidgets(DisplayLinkContext context, ModularGuiLineBuilder builder, boolean isFirstLine) {
		super.initConfigurationWidgets(context, builder, isFirstLine);
		if (isFirstLine)
			return;
		builder.addSelectionScrollInput(0, 137,
				(si, l) -> si.forOptions(Lang.translatedOptions("display_source.mechanical_piston", "percent", "progress_bar", "blocks", "blocks_max"))
						.titled(Lang.translateDirect("display_source.mechanical_piston.display")),
				"Mode");
	}

	@Override
	protected boolean allowsLabeling(DisplayLinkContext context) {
		return true;
	}
}
