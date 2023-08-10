package com.simibubi.create.content.redstone.displayLink.source;

import com.simibubi.create.content.kinetics.gauge.StressGaugeBlockEntity;
import com.simibubi.create.content.redstone.displayLink.DisplayLinkContext;
import com.simibubi.create.content.trains.display.FlapDisplayBlockEntity;
import com.simibubi.create.foundation.gui.ModularGuiLineBuilder;
import com.simibubi.create.foundation.utility.CreateLang;

import net.createmod.catnip.utility.lang.LangBuilder;
import net.minecraft.network.chat.MutableComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class KineticStressDisplaySource extends PercentOrProgressBarDisplaySource {

	@Override
	protected MutableComponent formatNumeric(DisplayLinkContext context, Float currentLevel) {
		int mode = getMode(context);
		if (mode == 1)
			return super.formatNumeric(context, currentLevel);
		LangBuilder builder = CreateLang.number(currentLevel);
		if (context.getTargetBlockEntity() instanceof FlapDisplayBlockEntity)
			builder.space();
		return builder.translate("generic.unit.stress")
			.component();
	}

	private int getMode(DisplayLinkContext context) {
		return context.sourceConfig()
			.getInt("Mode");
	}

	@Override
	protected Float getProgress(DisplayLinkContext context) {
		if (!(context.getSourceBlockEntity()instanceof StressGaugeBlockEntity stressGauge))
			return null;

		float capacity = stressGauge.getNetworkCapacity();
		float stress = stressGauge.getNetworkStress();

		if (capacity == 0)
			return 0f;

		return switch (getMode(context)) {
		case 0, 1 -> stress / capacity;
		case 2 -> stress;
		case 3 -> capacity;
		case 4 -> capacity - stress;
		default -> 0f;
		};
	}

	@Override
	protected boolean allowsLabeling(DisplayLinkContext context) {
		return true;
	}

	@Override
	protected boolean progressBarActive(DisplayLinkContext context) {
		return getMode(context) == 0;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void initConfigurationWidgets(DisplayLinkContext context, ModularGuiLineBuilder builder,
		boolean isFirstLine) {
		super.initConfigurationWidgets(context, builder, isFirstLine);
		if (isFirstLine)
			return;
		builder.addSelectionScrollInput(0, 120,
			(si, l) -> si
				.forOptions(CreateLang.translatedOptions("display_source.kinetic_stress", "progress_bar", "percent",
					"current", "max", "remaining"))
				.titled(CreateLang.translateDirect("display_source.kinetic_stress.display")),
			"Mode");
	}

	@Override
	protected String getTranslationKey() {
		return "kinetic_stress";
	}

}
