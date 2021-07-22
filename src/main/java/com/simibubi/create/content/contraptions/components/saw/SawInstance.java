package com.simibubi.create.content.contraptions.components.saw;

import static net.minecraft.state.properties.BlockStateProperties.FACING;

import com.jozufozu.flywheel.backend.instancing.Instancer;
import com.jozufozu.flywheel.backend.material.MaterialManager;
import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.content.contraptions.base.KineticTileEntity;
import com.simibubi.create.content.contraptions.base.RotatingData;
import com.simibubi.create.content.contraptions.base.SingleRotatingInstance;

import net.minecraft.block.BlockState;
import net.minecraft.util.Direction;
import net.minecraft.util.Rotation;

public class SawInstance extends SingleRotatingInstance {

    public SawInstance(MaterialManager<?> modelManager, KineticTileEntity tile) {
        super(modelManager, tile);
    }

    @Override
    protected Instancer<RotatingData> getModel() {
        if (blockState.getValue(FACING).getAxis().isHorizontal()) {
			BlockState referenceState = blockState.rotate(tile.getLevel(), tile.getBlockPos(), Rotation.CLOCKWISE_180);
			Direction facing = referenceState.getValue(FACING);
			return getRotatingMaterial().getModel(AllBlockPartials.SHAFT_HALF, referenceState, facing);
		} else {
			return getRotatingMaterial().getModel(shaft());
		}
    }
}
