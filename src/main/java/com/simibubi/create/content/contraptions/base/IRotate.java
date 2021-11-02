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
		NONE,
		MEDIUM,
		FAST;

		public ChatFormatting getTextColor() {
			return this == NONE ? ChatFormatting.GREEN
					: this == MEDIUM ? ChatFormatting.AQUA : ChatFormatting.LIGHT_PURPLE;
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

		public static Component getFormattedSpeedText(float speed, boolean overstressed){
			SpeedLevel speedLevel = of(speed);

			MutableComponent level = new TextComponent(ItemDescription.makeProgressBar(3, speedLevel.ordinal()));

			if (speedLevel == SpeedLevel.MEDIUM)
				level.append(Lang.translate("tooltip.speedRequirement.medium"));
			if (speedLevel == SpeedLevel.FAST)
				level.append(Lang.translate("tooltip.speedRequirement.high"));

			level.append(" (" + IHaveGoggleInformation.format(Math.abs(speed))).append(Lang.translate("generic.unit.rpm")).append(") ");

			if (overstressed)
				level.withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.STRIKETHROUGH);
			else
				level.withStyle(speedLevel.getTextColor());

			return level;
		}

	}

	enum StressImpact {
		LOW,
		MEDIUM,
		HIGH,
		OVERSTRESSED;

		public ChatFormatting getAbsoluteColor() {
			return this == LOW ? ChatFormatting.YELLOW : this == MEDIUM ? ChatFormatting.GOLD : ChatFormatting.RED;
		}

		public ChatFormatting getRelativeColor() {
			return this == LOW ? ChatFormatting.GREEN : this == MEDIUM ? ChatFormatting.YELLOW : this == HIGH ? ChatFormatting.GOLD : ChatFormatting.RED;
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

		public static Component getFormattedStressText(double stressPercent){
			StressImpact stressLevel = of(stressPercent);
			ChatFormatting color = stressLevel.getRelativeColor();

			MutableComponent level = new TextComponent(ItemDescription.makeProgressBar(3, Math.min(stressLevel.ordinal(), 2)));
			level.append(Lang.translate("tooltip.stressImpact." + Lang.asId(stressLevel.name())));

			level.append(String.format(" (%s%%) ", (int) (stressPercent * 100)));

			return level.withStyle(color);
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
