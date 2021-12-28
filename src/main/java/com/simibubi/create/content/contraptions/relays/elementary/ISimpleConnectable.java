package com.simibubi.create.content.contraptions.relays.elementary;

import com.simibubi.create.content.contraptions.solver.KineticConnections;

import net.minecraft.world.level.block.state.BlockState;

public interface ISimpleConnectable {
	KineticConnections getConnections(BlockState state);
}
