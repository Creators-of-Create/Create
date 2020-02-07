package com.simibubi.create.modules.contraptions.base;

import com.simibubi.create.config.AllConfigs;
import com.simibubi.create.modules.contraptions.IWrenchable;

import net.minecraft.block.BlockState;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.IWorldReader;

public interface IRotate extends IWrenchable {

	public enum SpeedLevel {
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

	}

	public enum StressImpact {
		LOW,
		MEDIUM,
		HIGH;

		public TextFormatting getColor() {
			return this == LOW ? TextFormatting.YELLOW : this == MEDIUM ? TextFormatting.GOLD : TextFormatting.RED;
		}
	}

	public boolean hasShaftTowards(IWorldReader world, BlockPos pos, BlockState state, Direction face);

	public boolean hasCogsTowards(IWorldReader world, BlockPos pos, BlockState state, Direction face);

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
