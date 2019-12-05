package com.simibubi.create.modules.contraptions.base;

import com.simibubi.create.CreateConfig;

import net.minecraft.block.BlockState;
import net.minecraft.item.ItemUseContext;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

public interface IRotate {

	public enum SpeedLevel {
		NONE, MEDIUM, FAST;

		public TextFormatting getColor() {
			return this == NONE ? TextFormatting.GREEN
					: this == MEDIUM ? TextFormatting.AQUA : TextFormatting.LIGHT_PURPLE;
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
			return this == LOW ? TextFormatting.YELLOW
					: this == MEDIUM ? TextFormatting.GOLD : TextFormatting.RED;
		}
	}

	public boolean hasShaftTowards(World world, BlockPos pos, BlockState state, Direction face);

	public boolean hasCogsTowards(World world, BlockPos pos, BlockState state, Direction face);

	public Axis getRotationAxis(BlockState state);

	public default ActionResultType onWrenched(BlockState state, ItemUseContext context) {
		return ActionResultType.PASS;
	}

	public default SpeedLevel getMinimumRequiredSpeedLevel() {
		return SpeedLevel.NONE;
	}

}
