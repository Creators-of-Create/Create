package com.simibubi.create.content.contraptions.base;

import com.jozufozu.flywheel.backend.instancing.InstancedTileRenderer;
import com.jozufozu.flywheel.backend.instancing.Instancer;
import com.simibubi.create.AllBlockPartials;

import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.Direction;

public class HalfShaftInstance extends SingleRotatingInstance {
    public HalfShaftInstance(InstancedTileRenderer<?> modelManager, KineticTileEntity tile) {
        super(modelManager, tile);
    }

    @Override
    protected Instancer<RotatingData> getModel() {
		Direction dir = getShaftDirection();
		return getRotatingMaterial().getModel(AllBlockPartials.SHAFT_HALF, blockState, dir);
	}

    protected Direction getShaftDirection() {
        return blockState.get(BlockStateProperties.FACING);
    }
}
