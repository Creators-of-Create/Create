package com.simibubi.create.content.redstone.displayLink.source;

import com.simibubi.create.content.contraptions.piston.MechanicalPistonBlockEntity;
import com.simibubi.create.content.fluids.hosePulley.HosePulleyBlockEntity;
import com.simibubi.create.content.redstone.displayLink.DisplayLinkContext;
import com.simibubi.create.content.redstone.displayLink.target.DisplayTargetStats;
import com.simibubi.create.foundation.gui.ModularGuiLineBuilder;
import com.simibubi.create.foundation.utility.Lang;

import com.simibubi.create.infrastructure.config.AllConfigs;

import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import org.jetbrains.annotations.Nullable;

public class HosePulleyDisplaySource extends PercentOrProgressBarDisplaySource {

	@Override
	protected MutableComponent provideLine(DisplayLinkContext context, DisplayTargetStats stats) {
		int mode = context.sourceConfig().getInt("Mode");
		BlockEntity be = context.getSourceBlockEntity();
		if (!(be instanceof HosePulleyBlockEntity hpbe))
			return null;
		return switch (mode) {
			case 0 -> new TextComponent(I18n.get(hpbe.getInternalTank().getFluid().getTranslationKey()));
			case 1 -> new TextComponent(String.valueOf(blocksFilled(hpbe)));
			default -> super.provideLine(context, stats);
		};
	}

	private int blocksFilled(HosePulleyBlockEntity hpbe) {
		int filler = hpbe.filler.getBlocksFilled();
		int drainer = hpbe.drainer.getBlocksFilled();
		if (hpbe.infinite)
			return maxBlocks();
		if ((filler == -1 && drainer == -1) || hpbe.getInternalTank().getFluid().getFluid().isSame(Fluids.EMPTY))
			return 0;
		if (filler == -1) {
			return drainer;
		} else if (drainer == -1) {
			return filler;
		} else {
			return 0;
		}

	}

	private int maxBlocks() {
		return AllConfigs.server().fluids.hosePulleyBlockThreshold.get();
	}

	@Nullable
	@Override
	protected Float getProgress(DisplayLinkContext context) {
		BlockEntity be = context.getSourceBlockEntity();
		int mode = context.sourceConfig().getInt("Mode");
		if (!(be instanceof HosePulleyBlockEntity hpbe))
			return null;
		if (mode == 2 || mode == 3)
			return (float)blocksFilled(hpbe)/(float)maxBlocks();
		if (mode == 4 || mode == 5)
			return Math.abs(1 - ((float)blocksFilled(hpbe)/(float)maxBlocks()));
		return 0f;
	}

	@Override
	protected boolean progressBarActive(DisplayLinkContext context) {
		int mode = context.sourceConfig().getInt("Mode");
		return mode == 3 || mode == 5;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void initConfigurationWidgets(DisplayLinkContext context, ModularGuiLineBuilder builder, boolean isFirstLine) {
		super.initConfigurationWidgets(context, builder, isFirstLine);
		if (isFirstLine)
			return;
		builder.addSelectionScrollInput(0, 137,
				(si, l) -> si.forOptions(Lang.translatedOptions("display_source.hose_pulley", "fluid",
								"fluid_count", "percent_infinite", "progress_bar_infinite", "percent_empty", "progress_bar_empty"))
						.titled(Lang.translateDirect("display_source.hose_pulley.display")),
				"Mode");
	}

	@Override
	protected boolean allowsLabeling(DisplayLinkContext context) {
		return true;
	}
}
