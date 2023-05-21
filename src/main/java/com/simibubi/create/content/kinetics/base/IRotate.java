package com.simibubi.create.content.kinetics.base;

import com.simibubi.create.content.equipment.wrench.IWrenchable;
import com.simibubi.create.foundation.item.TooltipHelper;
import com.simibubi.create.foundation.utility.Lang;
import com.simibubi.create.foundation.utility.LangBuilder;
import com.simibubi.create.infrastructure.config.AllConfigs;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;

public interface IRotate extends IWrenchable {

	enum SpeedLevel {
		NONE(ChatFormatting.DARK_GRAY, 0x000000, 0),
		SLOW(ChatFormatting.GREEN, 0x22FF22, 10),
		MEDIUM(ChatFormatting.AQUA, 0x0084FF, 20),
		FAST(ChatFormatting.LIGHT_PURPLE, 0xFF55FF, 30);

		private final ChatFormatting textColor;
		private final int color;
		private final int particleSpeed;

		SpeedLevel(ChatFormatting textColor, int color, int particleSpeed) {
			this.textColor = textColor;
			this.color = color;
			this.particleSpeed = particleSpeed;
		}

		public ChatFormatting getTextColor() {
			return textColor;
		}

		public int getColor() {
			return color;
		}

		public int getParticleSpeed() {
			return particleSpeed;
		}

		public float getSpeedValue() {
			switch (this) {
			case FAST:
				return AllConfigs.server().kinetics.fastSpeed.get()
					.floatValue();
			case MEDIUM:
				return AllConfigs.server().kinetics.mediumSpeed.get()
					.floatValue();
			case SLOW:
				return 1;
			case NONE:
			default:
				return 0;
			}
		}

		public static SpeedLevel of(float speed) {
			speed = Math.abs(speed);

			if (speed >= AllConfigs.server().kinetics.fastSpeed.get())
				return FAST;
			if (speed >= AllConfigs.server().kinetics.mediumSpeed.get())
				return MEDIUM;
			if (speed >= 1)
				return SLOW;
			return NONE;
		}

		public static LangBuilder getFormattedSpeedText(float speed, boolean overstressed) {
			SpeedLevel speedLevel = of(speed);
			LangBuilder builder = Lang.text(TooltipHelper.makeProgressBar(3, speedLevel.ordinal()));

			builder.translate("tooltip.speedRequirement." + Lang.asId(speedLevel.name()))
				.space()
				.text("(")
				.add(Lang.number(Math.abs(speed)))
				.space()
				.translate("generic.unit.rpm")
				.text(")")
				.space();

			if (overstressed)
				builder.style(ChatFormatting.DARK_GRAY)
					.style(ChatFormatting.STRIKETHROUGH);
			else
				builder.style(speedLevel.getTextColor());

			return builder;
		}

	}

	enum StressImpact {
		LOW(ChatFormatting.YELLOW, ChatFormatting.GREEN),
		MEDIUM(ChatFormatting.GOLD, ChatFormatting.YELLOW),
		HIGH(ChatFormatting.RED, ChatFormatting.GOLD),
		OVERSTRESSED(ChatFormatting.RED, ChatFormatting.RED);

		private final ChatFormatting absoluteColor;
		private final ChatFormatting relativeColor;

		StressImpact(ChatFormatting absoluteColor, ChatFormatting relativeColor) {
			this.absoluteColor = absoluteColor;
			this.relativeColor = relativeColor;
		}

		public ChatFormatting getAbsoluteColor() {
			return absoluteColor;
		}

		public ChatFormatting getRelativeColor() {
			return relativeColor;
		}

		public static StressImpact of(double stressPercent) {
			if (stressPercent > 1)
				return StressImpact.OVERSTRESSED;
			if (stressPercent > .75d)
				return StressImpact.HIGH;
			if (stressPercent > .5d)
				return StressImpact.MEDIUM;
			return StressImpact.LOW;
		}

		public static boolean isEnabled() {
			return !AllConfigs.server().kinetics.disableStress.get();
		}

		public static LangBuilder getFormattedStressText(double stressPercent) {
			StressImpact stressLevel = of(stressPercent);
			return Lang.text(TooltipHelper.makeProgressBar(3, Math.min(stressLevel.ordinal() + 1, 3)))
				.translate("tooltip.stressImpact." + Lang.asId(stressLevel.name()))
				.text(String.format(" (%s%%) ", (int) (stressPercent * 100)))
				.style(stressLevel.getRelativeColor());
		}
	}

	public boolean hasShaftTowards(LevelReader world, BlockPos pos, BlockState state, Direction face);

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
