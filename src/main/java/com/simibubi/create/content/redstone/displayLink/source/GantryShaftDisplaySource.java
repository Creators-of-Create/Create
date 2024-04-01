package com.simibubi.create.content.redstone.displayLink.source;

import org.jetbrains.annotations.Nullable;

import com.simibubi.create.content.kinetics.gantry.GantryShaftBlockEntity;
import com.simibubi.create.content.redstone.displayLink.DisplayLinkContext;
import com.simibubi.create.content.redstone.displayLink.target.DisplayTargetStats;
import com.simibubi.create.foundation.gui.ModularGuiLineBuilder;
import com.simibubi.create.foundation.utility.Lang;

import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class GantryShaftDisplaySource extends PercentOrProgressBarDisplaySource {

	@Override
	protected MutableComponent provideLine(DisplayLinkContext context, DisplayTargetStats stats) {
		int mode = context.sourceConfig().getInt("Mode");
		BlockEntity be = context.getSourceBlockEntity();
		if (!(be instanceof GantryShaftBlockEntity gsbe))
			return null;
		gsbe.checkAttachedCarriageBlocks();
		return switch (mode) {
			case 2 -> new TextComponent(String.valueOf(gsbe.findGantryOffset()));
			case 3 -> new TextComponent(String.valueOf(gsbe.attachedShafts()));
			default -> super.provideLine(context, stats);
		};
	}

	@Nullable
	@Override
	protected Float getProgress(DisplayLinkContext context) {
		BlockEntity be = context.getSourceBlockEntity();
		if (!(be instanceof GantryShaftBlockEntity gsbe))
			return null;
		return (float)gsbe.findGantryOffset()/(float)gsbe.attachedShafts();
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
				(si, l) -> si.forOptions(Lang.translatedOptions("display_source.gantry_shaft",
								"percent", "progress_bar", "blocks", "blocks_max"))
						.titled(Lang.translateDirect("display_source.gantry_shaft.display")),
				"Mode");
	}

	@Override
	protected boolean allowsLabeling(DisplayLinkContext context) {
		return true;
	}
}
