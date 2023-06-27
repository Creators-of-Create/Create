package com.simibubi.create.content.kinetics.base;

import com.jozufozu.flywheel.api.Instancer;
import com.jozufozu.flywheel.api.MaterialManager;
import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.kinetics.base.flwdata.RotatingData;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public class HalfShaftInstance<T extends KineticBlockEntity> extends SingleRotatingInstance<T> {
    public HalfShaftInstance(MaterialManager materialManager, T blockEntity) {
        super(materialManager, blockEntity);
    }

    @Override
    protected Instancer<RotatingData> getModel() {
		Direction dir = getShaftDirection();
		return getRotatingMaterial().getModel(AllPartialModels.SHAFT_HALF, blockState, dir);
	}

    protected Direction getShaftDirection() {
        return blockState.getValue(BlockStateProperties.FACING);
    }
}
