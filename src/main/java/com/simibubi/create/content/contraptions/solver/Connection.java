package com.simibubi.create.content.contraptions.solver;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

public abstract class Connection {
	public final BlockPos from;
	public final BlockPos to;

	public Connection(BlockPos from, BlockPos to) {
		this.from = from;
		this.to = to;
	}

	public abstract boolean isCompatible(Connection that);

	public static final class Shaft extends Connection {

		public Shaft(BlockPos pos, Direction face) {
			super(pos, pos.relative(face));
		}

		@Override
		public boolean isCompatible(Connection that) {
			return that instanceof Shaft;
		}
	}
}
