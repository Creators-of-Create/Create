package com.simibubi.create.content.logistics.block.display.source;

import com.simibubi.create.content.contraptions.relays.gauge.StressGaugeTileEntity;
import com.simibubi.create.content.logistics.block.display.DisplayLinkContext;
import com.simibubi.create.content.logistics.trains.management.display.FlapDisplayTileEntity;
import com.simibubi.create.foundation.gui.ModularGuiLineBuilder;
import com.simibubi.create.foundation.utility.Lang;
import com.simibubi.create.foundation.utility.LangBuilder;

import net.minecraft.network.chat.MutableComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class KineticStressDisplaySource extends PercentOrProgressBarDisplaySource {

	@Override
	protected MutableComponent formatNumeric(DisplayLinkContext context, Float currentLevel) {
		int mode = getMode(context);
		if (mode == 1)
			return super.formatNumeric(context, currentLevel);
		LangBuilder builder = Lang.number(currentLevel);
		if (context.getTargetTE() instanceof FlapDisplayTileEntity)
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
		if (!(context.getSourceTE()instanceof StressGaugeTileEntity gaugeTile))
			return null;

		float capacity = gaugeTile.getNetworkCapacity();
		float stress = gaugeTile.getNetworkStress();

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
				.forOptions(Lang.translatedOptions("display_source.kinetic_stress", "progress_bar", "percent",
					"current", "max", "remaining"))
				.titled(Lang.translateDirect("display_source.kinetic_stress.display")),
			"Mode");
	}

	@Override
	protected String getTranslationKey() {
		return "kinetic_stress";
	}

}
