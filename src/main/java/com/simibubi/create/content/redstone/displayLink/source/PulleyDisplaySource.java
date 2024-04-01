package com.simibubi.create.content.redstone.displayLink.source;

import net.minecraft.network.chat.TextComponent;

import org.jetbrains.annotations.Nullable;

import com.simibubi.create.content.contraptions.pulley.PulleyBlockEntity;
import com.simibubi.create.content.redstone.displayLink.DisplayLinkContext;
import com.simibubi.create.content.redstone.displayLink.target.DisplayTargetStats;
import com.simibubi.create.foundation.gui.ModularGuiLineBuilder;
import com.simibubi.create.foundation.utility.Lang;

import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class PulleyDisplaySource extends PercentOrProgressBarDisplaySource {

	@Override
	protected MutableComponent provideLine(DisplayLinkContext context, DisplayTargetStats stats) {
		int mode = context.sourceConfig().getInt("Mode");
		BlockEntity be = context.getSourceBlockEntity();
		if (!(be instanceof PulleyBlockEntity pbe))
			return null;
		return switch (mode) {
			case 2 -> new TextComponent(String.valueOf(pbe.offset));
			case 3 -> new TextComponent(String.valueOf(pbe.blocksToGround()));
			default -> super.provideLine(context, stats);
		};
	}

	@Nullable
	@Override
	protected Float getProgress(DisplayLinkContext context) {
		BlockEntity be = context.getSourceBlockEntity();
		if (!(be instanceof PulleyBlockEntity labe))
			return null;
		return labe.getOffset()/labe.blocksToGround();
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
		builder.addSelectionScrollInput(0, 138,
				(si, l) -> si.forOptions(Lang.translatedOptions("display_source.rope_pulley", "percent", "progress_bar", "blocks", "blocks_max"))
						.titled(Lang.translateDirect("display_source.rope_pulley.display")),
				"Mode");
	}

	@Override
	protected boolean allowsLabeling(DisplayLinkContext context) {
		return true;
	}
}
