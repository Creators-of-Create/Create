package com.simibubi.create.content.logistics.block.display.source;

import com.simibubi.create.content.logistics.block.display.DisplayLinkContext;
import com.simibubi.create.content.logistics.block.redstone.StockpileSwitchTileEntity;
import com.simibubi.create.foundation.gui.ModularGuiLineBuilder;
import com.simibubi.create.foundation.utility.Lang;

import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class FillLevelDisplaySource extends PercentOrProgressBarDisplaySource {

	@Override
	protected Float getProgress(DisplayLinkContext context) {
		BlockEntity te = context.getSourceTE();
		if (!(te instanceof StockpileSwitchTileEntity sste))
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
