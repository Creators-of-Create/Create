package com.simibubi.create.foundation.render;

import org.apache.commons.lang3.tuple.Pair;

import com.simibubi.create.foundation.render.backend.core.PartialModel;

import net.minecraft.block.BlockState;
import net.minecraft.util.Direction;

public class Compartment<T> {
	public static final Compartment<BlockState> GENERIC_TILE = new Compartment<>();
	public static final Compartment<PartialModel> PARTIAL = new Compartment<>();
	public static final Compartment<Pair<Direction, PartialModel>> DIRECTIONAL_PARTIAL = new Compartment<>();
}
