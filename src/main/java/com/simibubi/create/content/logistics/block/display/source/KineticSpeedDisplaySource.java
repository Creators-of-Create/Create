package com.simibubi.create.content.logistics.block.display.source;

import com.simibubi.create.content.contraptions.goggles.IHaveGoggleInformation;
import com.simibubi.create.content.contraptions.relays.gauge.SpeedGaugeTileEntity;
import com.simibubi.create.content.logistics.block.display.DisplayLinkContext;
import com.simibubi.create.content.logistics.block.display.target.DisplayTargetStats;
import com.simibubi.create.foundation.gui.ModularGuiLineBuilder;
import com.simibubi.create.foundation.utility.Lang;

import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class KineticSpeedDisplaySource extends NumericSingleLineDisplaySource {

	@Override
	protected MutableComponent provideLine(DisplayLinkContext context, DisplayTargetStats stats) {
		if (!(context.getSourceTE() instanceof SpeedGaugeTileEntity gaugeTile))
			return ZERO;

		boolean absoluteValue = context.sourceConfig().getInt("Directional") == 0;
		float speed = absoluteValue ? Math.abs(gaugeTile.getSpeed()) : gaugeTile.getSpeed();

		return new TextComponent(IHaveGoggleInformation.format(speed));
	}

	@Override
	protected String getTranslationKey() {
		return "kinetic_speed";
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void initConfigurationWidgets(DisplayLinkContext context, ModularGuiLineBuilder builder, boolean isFirstLine) {
		super.initConfigurationWidgets(context, builder, isFirstLine);
		if (isFirstLine)
			return;

		builder.addSelectionScrollInput(0, 95, (selectionScrollInput, label) -> {
			selectionScrollInput.forOptions(Lang.translatedOptions("display_source.kinetic_speed", "absolute", "directional"));
		}, "Directional");
	}

	@Override
	protected boolean allowsLabeling(DisplayLinkContext context) {
		return true;
	}
}
