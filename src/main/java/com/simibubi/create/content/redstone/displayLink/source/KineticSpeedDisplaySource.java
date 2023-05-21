package com.simibubi.create.content.redstone.displayLink.source;

import com.simibubi.create.content.kinetics.gauge.SpeedGaugeBlockEntity;
import com.simibubi.create.content.redstone.displayLink.DisplayLinkContext;
import com.simibubi.create.content.redstone.displayLink.target.DisplayTargetStats;
import com.simibubi.create.foundation.gui.ModularGuiLineBuilder;
import com.simibubi.create.foundation.utility.Lang;

import net.minecraft.network.chat.MutableComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class KineticSpeedDisplaySource extends NumericSingleLineDisplaySource {

	@Override
	protected MutableComponent provideLine(DisplayLinkContext context, DisplayTargetStats stats) {
		if (!(context.getSourceBlockEntity() instanceof SpeedGaugeBlockEntity speedGauge))
			return ZERO.copy();

		boolean absoluteValue = context.sourceConfig()
			.getInt("Directional") == 0;
		float speed = absoluteValue ? Math.abs(speedGauge.getSpeed()) : speedGauge.getSpeed();
		return Lang.number(speed)
			.space()
			.translate("generic.unit.rpm")
			.component();
	}

	@Override
	protected String getTranslationKey() {
		return "kinetic_speed";
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void initConfigurationWidgets(DisplayLinkContext context, ModularGuiLineBuilder builder,
		boolean isFirstLine) {
		super.initConfigurationWidgets(context, builder, isFirstLine);
		if (isFirstLine)
			return;

		builder.addSelectionScrollInput(0, 95, (selectionScrollInput, label) -> {
			selectionScrollInput
				.forOptions(Lang.translatedOptions("display_source.kinetic_speed", "absolute", "directional"));
		}, "Directional");
	}

	@Override
	protected boolean allowsLabeling(DisplayLinkContext context) {
		return true;
	}
}
