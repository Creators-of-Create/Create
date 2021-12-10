package com.simibubi.create.content.contraptions.base;

import com.jozufozu.flywheel.api.Instancer;
import com.jozufozu.flywheel.api.MaterialManager;
import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.content.contraptions.base.flwdata.RotatingData;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public class HalfShaftInstance extends SingleRotatingInstance {
    public HalfShaftInstance(MaterialManager modelManager, KineticTileEntity tile) {
        super(modelManager, tile);
    }

    @Override
    protected Instancer<RotatingData> getModel() {
		Direction dir = getShaftDirection();
		return getRotatingMaterial().getModel(AllBlockPartials.SHAFT_HALF, blockState, dir);
	}

    protected Direction getShaftDirection() {
        return blockState.getValue(BlockStateProperties.FACING);
    }
}
