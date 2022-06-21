package com.simibubi.create.content.contraptions.base;

import com.simibubi.create.content.contraptions.goggles.IHaveGoggleInformation;
import com.simibubi.create.content.contraptions.wrench.IWrenchable;
import com.simibubi.create.foundation.config.AllConfigs;
import com.simibubi.create.foundation.item.ItemDescription;
import com.simibubi.create.foundation.utility.Lang;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
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
				return AllConfigs.SERVER.kinetics.fastSpeed.get().floatValue();
			case MEDIUM:
				return AllConfigs.SERVER.kinetics.mediumSpeed.get().floatValue();
			case SLOW:
				return 1;
			case NONE:
			default:
				return 0;
			}
		}

		public static SpeedLevel of(float speed) {
			speed = Math.abs(speed);

			if (speed >= AllConfigs.SERVER.kinetics.fastSpeed.get())
				return FAST;
			if (speed >= AllConfigs.SERVER.kinetics.mediumSpeed.get())
				return MEDIUM;
			if (speed >= 1)
				return SLOW;
			return NONE;
		}

		public static Component getFormattedSpeedText(float speed, boolean overstressed) {
			SpeedLevel speedLevel = of(speed);

			MutableComponent level = new TextComponent(ItemDescription.makeProgressBar(3, speedLevel.ordinal()));
			level.append(Lang.translate("tooltip.speedRequirement." + Lang.asId(speedLevel.name())));
			level.append(" (" + IHaveGoggleInformation.format(Math.abs(speed))).append(Lang.translate("generic.unit.rpm")).append(") ");

			if (overstressed)
				level.withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.STRIKETHROUGH);
			else
				level.withStyle(speedLevel.getTextColor());

			return level;
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
			return !AllConfigs.SERVER.kinetics.disableStress.get();
		}

		public static Component getFormattedStressText(double stressPercent) {
			StressImpact stressLevel = of(stressPercent);

			MutableComponent level = new TextComponent(ItemDescription.makeProgressBar(3, Math.min(stressLevel.ordinal() + 1, 3)));
			level.append(Lang.translate("tooltip.stressImpact." + Lang.asId(stressLevel.name())));
			level.append(String.format(" (%s%%) ", (int) (stressPercent * 100)));

			return level.withStyle(stressLevel.getRelativeColor());
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
