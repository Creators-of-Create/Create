package com.simibubi.create.modules.contraptions.base;

import com.simibubi.create.CreateConfig;
import com.simibubi.create.modules.contraptions.IWrenchable;

import net.minecraft.block.BlockState;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

public interface IRotate extends IWrenchable {

	public enum SpeedLevel {
		NONE, MEDIUM, FAST;

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

			if (speed >= CreateConfig.parameters.fastSpeed.get()) {
				return FAST;
			} else if (speed >= CreateConfig.parameters.mediumSpeed.get()) {
				return MEDIUM;
			}
			return NONE;
		}
	}

	public enum StressImpact {
		LOW, MEDIUM, HIGH;

		public TextFormatting getColor() {
			return this == LOW ? TextFormatting.YELLOW : this == MEDIUM ? TextFormatting.GOLD : TextFormatting.RED;
		}
	}

	public boolean hasShaftTowards(World world, BlockPos pos, BlockState state, Direction face);

	public boolean hasCogsTowards(World world, BlockPos pos, BlockState state, Direction face);

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
