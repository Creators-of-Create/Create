package com.simibubi.create.foundation.render;

import org.apache.commons.lang3.tuple.Pair;

import com.jozufozu.flywheel.core.PartialModel;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.Direction;

public class Compartment<T> {
	public static final Compartment<BlockState> GENERIC_TILE = new Compartment<>();
	public static final Compartment<PartialModel> PARTIAL = new Compartment<>();
	public static final Compartment<Pair<Direction, PartialModel>> DIRECTIONAL_PARTIAL = new Compartment<>();
}
