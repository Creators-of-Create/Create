package com.simibubi.create.content.logistics.block.display.source;

import com.simibubi.create.content.contraptions.goggles.IHaveGoggleInformation;
import com.simibubi.create.content.contraptions.relays.gauge.StressGaugeTileEntity;
import com.simibubi.create.content.logistics.block.display.DisplayLinkContext;
import com.simibubi.create.content.logistics.block.display.target.DisplayTargetStats;

import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;

public abstract class KineticStressDisplaySource extends NumericSingleLineDisplaySource {

	protected abstract double getValue(StressGaugeTileEntity gaugeTile);

	@Override
	protected MutableComponent provideLine(DisplayLinkContext context, DisplayTargetStats stats) {
		if (!(context.getSourceTE() instanceof StressGaugeTileEntity gaugeTile))
			return ZERO;

		return new TextComponent(IHaveGoggleInformation.format(getValue(gaugeTile)));
	}

	@Override
	protected boolean allowsLabeling(DisplayLinkContext context) {
		return true;
	}

	public static class Current extends KineticStressDisplaySource {

		@Override
		protected double getValue(StressGaugeTileEntity gaugeTile) {
			return gaugeTile.getNetworkStress();
		}

		@Override
		protected String getTranslationKey() {
			return "kinetic_stress.current";
		}
	}

	public static class Max extends KineticStressDisplaySource {

		@Override
		protected double getValue(StressGaugeTileEntity gaugeTile) {
			return gaugeTile.getNetworkCapacity();
		}

		@Override
		protected String getTranslationKey() {
			return "kinetic_stress.max";
		}
	}

	public static class Percent extends KineticStressDisplaySource {

		@Override
		protected double getValue(StressGaugeTileEntity gaugeTile) {
			return gaugeTile.getNetworkStress() / gaugeTile.getNetworkCapacity() * 100;
		}

		@Override
		protected String getTranslationKey() {
			return "kinetic_stress.percent";
		}
	}

	public static class Remaining extends KineticStressDisplaySource {

		@Override
		protected double getValue(StressGaugeTileEntity gaugeTile) {
			return gaugeTile.getNetworkCapacity() - gaugeTile.getNetworkStress();
		}

		@Override
		protected String getTranslationKey() {
			return "kinetic_stress.remaining";
		}
	}

}
