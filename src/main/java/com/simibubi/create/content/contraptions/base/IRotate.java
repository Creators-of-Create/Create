package com.simibubi.create.content.contraptions.base;

import com.simibubi.create.content.contraptions.goggles.IHaveGoggleInformation;
import com.simibubi.create.content.contraptions.wrench.IWrenchable;
import com.simibubi.create.foundation.config.AllConfigs;
import com.simibubi.create.foundation.item.ItemDescription;
import com.simibubi.create.foundation.utility.Lang;

import net.minecraft.block.BlockState;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.IWorldReader;

public interface IRotate extends IWrenchable {

	enum SpeedLevel {
		NONE,
		MEDIUM,
		FAST;

		public TextFormatting getTextColor() {
			return this == NONE ? TextFormatting.GREEN
					: this == MEDIUM ? TextFormatting.AQUA : TextFormatting.LIGHT_PURPLE;
		}

		public int getColor() {
			return this == NONE ? 0x22FF22 : this == MEDIUM ? 0x0084FF : 0xFF55FF;
		}

		public int getParticleSpeed() {
			return this == NONE ? 10 : this == MEDIUM ? 20 : 30;
		}

		public static SpeedLevel of(float speed) {
			speed = Math.abs(speed);

			if (speed >= AllConfigs.SERVER.kinetics.fastSpeed.get()) {
				return FAST;
			} else if (speed >= AllConfigs.SERVER.kinetics.mediumSpeed.get()) {
				return MEDIUM;
			}
			return NONE;
		}

		public float getSpeedValue() {
			switch (this) {
			case FAST:
				return AllConfigs.SERVER.kinetics.fastSpeed.get().floatValue();
			case MEDIUM:
				return AllConfigs.SERVER.kinetics.mediumSpeed.get().floatValue();
			case NONE:
			default:
				return 0;
			}
		}

		public static String getFormattedSpeedText(float speed, boolean overstressed){
			SpeedLevel speedLevel = of(speed);

			String color;
			if (overstressed)
				color = TextFormatting.DARK_GRAY + "" + TextFormatting.STRIKETHROUGH;
			else
				color = speedLevel.getTextColor() + "";

			String level = color + ItemDescription.makeProgressBar(3, speedLevel.ordinal());

			if (speedLevel == SpeedLevel.MEDIUM)
				level += Lang.translate("tooltip.speedRequirement.medium");
			if (speedLevel == SpeedLevel.FAST)
				level += Lang.translate("tooltip.speedRequirement.high");

			level += String.format(" (%s%s) ", IHaveGoggleInformation.format(Math.abs(speed)), Lang.translate("generic.unit.rpm"));

			return level;
		}

	}

	enum StressImpact {
		LOW,
		MEDIUM,
		HIGH,
		OVERSTRESSED;

		public TextFormatting getAbsoluteColor() {
			return this == LOW ? TextFormatting.YELLOW : this == MEDIUM ? TextFormatting.GOLD : TextFormatting.RED;
		}

		public TextFormatting getRelativeColor() {
			return this == LOW ? TextFormatting.GREEN : this == MEDIUM ? TextFormatting.YELLOW : this == HIGH ? TextFormatting.GOLD : TextFormatting.RED;
		}

		public static StressImpact of(double stressPercent){
			if (stressPercent > 1) return StressImpact.OVERSTRESSED;
			else if (stressPercent > .75d) return StressImpact.HIGH;
			else if (stressPercent > .5d) return StressImpact.MEDIUM;
			else return StressImpact.LOW;
		}
		
		public static boolean isEnabled() {
			return !AllConfigs.SERVER.kinetics.disableStress.get();
		}

		public static String getFormattedStressText(double stressPercent){
			StressImpact stressLevel = of(stressPercent);
			TextFormatting color = stressLevel.getRelativeColor();

			String level = color + ItemDescription.makeProgressBar(3, Math.min(stressLevel.ordinal(), 2));
			level += Lang.translate("tooltip.stressImpact."+Lang.asId(stressLevel.name()));

			level += String.format(" (%s%%) ", (int) (stressPercent * 100));

			return level;
		}
	}

	public boolean hasShaftTowards(IWorldReader world, BlockPos pos, BlockState state, Direction face);
	
	public boolean hasIntegratedCogwheel(IWorldReader world, BlockPos pos, BlockState state);

	public Axis getRotationAxis(BlockState state);

	public default SpeedLevel getMinimumRequiredSpeedLevel() {
		return SpeedLevel.NONE;
	}

	public default boolean hideStressImpact() {
		return false;
	}

	public default boolean showCapacityWithAnnotation() {
		return false;
	}

}
