package com.simibubi.create.api.behaviour.spouting;

import java.util.function.Predicate;
import java.util.function.UnaryOperator;

import com.simibubi.create.content.fluids.spout.SpoutBlockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.Fluid;

import net.neoforged.neoforge.fluids.FluidStack;

/**
 * An implementation of {@link BlockSpoutingBehaviour} that allows for easily modifying a BlockState through spouting.
 * @param amount the amount of fluid consumed when filling
 * @param fluidTest a predicate for fluids that can be used to fill the target block
 * @param canFill a predicate that must match the target BlockState to fill it
 * @param fillFunction a function that converts the current state into the filled one
 */
public record StateChangingBehavior(int amount, Predicate<Fluid> fluidTest, Predicate<BlockState> canFill,
									UnaryOperator<BlockState> fillFunction) implements BlockSpoutingBehaviour {
	@Override
	public int fillBlock(Level level, BlockPos pos, SpoutBlockEntity spout, FluidStack availableFluid, boolean simulate) {
		if (availableFluid.getAmount() < this.amount || !this.fluidTest.test(availableFluid.getFluid()))
			return 0;

		BlockState state = level.getBlockState(pos);
		if (!this.canFill.test(state))
			return 0;

		if (!simulate) {
			BlockState newState = this.fillFunction.apply(state);
			level.setBlockAndUpdate(pos, newState);
		}

		return this.amount;
	}

	/**
	 * Shortcut for {@link #setTo(int, Predicate, BlockState)} that uses the Block's default state.
	 */
	public static BlockSpoutingBehaviour setTo(int amount, Predicate<Fluid> fluidTest, Block block) {
		return setTo(amount, fluidTest, block.defaultBlockState());
	}

	/**
	 * Create a {@link BlockSpoutingBehaviour} that will simply convert the target block to the given state.
	 * @param newState the state that will be set after filling
	 */
	public static BlockSpoutingBehaviour setTo(int amount, Predicate<Fluid> fluidTest, BlockState newState) {
		return new StateChangingBehavior(amount, fluidTest, state -> true, state -> newState);
	}

	/**
	 * Create a {@link BlockSpoutingBehaviour} that will increment the given {@link IntegerProperty} until it reaches
	 * its maximum value, consuming {@code amount} each time fluid is filled.
	 * @param property the property that will be incremented by one on each fill
	 */
	public static BlockSpoutingBehaviour incrementingState(int amount, Predicate<Fluid> fluidTest, IntegerProperty property) {
		int max = property.getPossibleValues().stream().max(Integer::compareTo).orElseThrow();

		Predicate<BlockState> canFill = state -> state.getValue(property) < max;
		UnaryOperator<BlockState> fillFunction = state -> state.setValue(property, state.getValue(property) + 1);

		return new StateChangingBehavior(amount, fluidTest, canFill, fillFunction);
	}
}
