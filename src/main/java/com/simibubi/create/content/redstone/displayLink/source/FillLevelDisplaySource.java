package com.simibubi.create.content.redstone.displayLink.source;

import com.simibubi.create.content.redstone.displayLink.DisplayLinkContext;
import com.simibubi.create.content.redstone.thresholdSwitch.ThresholdSwitchBlockEntity;
import com.simibubi.create.foundation.gui.ModularGuiLineBuilder;
import com.simibubi.create.foundation.utility.Lang;

import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class FillLevelDisplaySource extends PercentOrProgressBarDisplaySource {

	@Override
	protected Float getProgress(DisplayLinkContext context) {
		BlockEntity be = context.getSourceBlockEntity();
		if (!(be instanceof ThresholdSwitchBlockEntity sste))
			return null;
		return sste.currentLevel;
	}
	
	@Override
	protected boolean progressBarActive(DisplayLinkContext context) {
		return context.sourceConfig()
			.getInt("Mode") != 0;
	}

	@Override
	protected String getTranslationKey() {
		return "fill_level";
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void initConfigurationWidgets(DisplayLinkContext context, ModularGuiLineBuilder builder, boolean isFirstLine) {
		super.initConfigurationWidgets(context, builder, isFirstLine);
		if (isFirstLine)
			return;
		builder.addSelectionScrollInput(0, 120,
			(si, l) -> si.forOptions(Lang.translatedOptions("display_source.fill_level", "percent", "progress_bar"))
				.titled(Lang.translateDirect("display_source.fill_level.display")),
			"Mode");
	}

	@Override
	protected boolean allowsLabeling(DisplayLinkContext context) {
		return true;
	}

}
