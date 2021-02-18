package com.simibubi.create.foundation.render;

import com.simibubi.create.AllBlockPartials;
import net.minecraft.block.BlockState;
import net.minecraft.util.Direction;
import org.apache.commons.lang3.tuple.Pair;

public class Compartment<T> {
    public static final Compartment<BlockState> GENERIC_TILE = new Compartment<>();
    public static final Compartment<AllBlockPartials> PARTIAL = new Compartment<>();
    public static final Compartment<Pair<Direction, AllBlockPartials>> DIRECTIONAL_PARTIAL = new Compartment<>();
}
