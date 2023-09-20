package com.simibubi.create.api.event;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.eventbus.api.Event;

import org.jetbrains.annotations.Nullable;

/**
 * Event that is fired when a two fluids meet in a pipe ({@link Flow})<br>
 * or when a fluid in a pipe meets with a fluid in the world ({@link Spill}).<br>
 * <br>
 * If it is not null, the event's BlockState will be placed in world after firing.
 */
public class PipeCollisionEvent extends Event {

	private final Level level;
	private final BlockPos pos;
	protected final Fluid fluid0, fluid1;
	@Nullable
	private BlockState state;

	protected PipeCollisionEvent(Level level, BlockPos pos, Fluid fluid0, Fluid fluid1, @Nullable BlockState defaultState) {
		this.level = level;
		this.pos = pos;
		this.fluid0 = fluid0;
		this.fluid1 =fluid1;
		this.state = defaultState;
	}

	public Level getLevel() {
		return level;
	}

	public BlockPos getPos() {
		return pos;
	}

	@Nullable
	public BlockState getState() {
		return state;
	}

	public void setState(@Nullable BlockState state) {
		this.state = state;
	}

	public static class Flow extends PipeCollisionEvent {

		public Flow(Level level, BlockPos pos, Fluid firstFluid, Fluid secondFluid, @Nullable BlockState defaultState) {
			super(level, pos, firstFluid, secondFluid, defaultState);
		}

		public Fluid getFirstFluid() {
			return fluid0;
		}

		public Fluid getSecondFluid() {
			return fluid1;
		}
	}

	public static class Spill extends PipeCollisionEvent {

		public Spill(Level level, BlockPos pos, Fluid worldFluid, Fluid pipeFluid, @Nullable BlockState defaultState) {
			super(level, pos, worldFluid, pipeFluid, defaultState);
		}

		public Fluid getWorldFluid() {
			return fluid0;
		}

		public Fluid getPipeFluid() {
			return fluid1;
		}
	}
}
