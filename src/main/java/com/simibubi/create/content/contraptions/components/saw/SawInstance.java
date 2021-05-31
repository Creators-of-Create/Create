package com.simibubi.create.content.contraptions.components.saw;

import static net.minecraft.state.properties.BlockStateProperties.FACING;

import com.jozufozu.flywheel.backend.instancing.InstancedTileRenderer;
import com.jozufozu.flywheel.backend.instancing.Instancer;
import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.content.contraptions.base.KineticTileEntity;
import com.simibubi.create.content.contraptions.base.RotatingData;
import com.simibubi.create.content.contraptions.base.SingleRotatingInstance;

import net.minecraft.block.BlockState;
import net.minecraft.util.Direction;
import net.minecraft.util.Rotation;

public class SawInstance extends SingleRotatingInstance {

    public SawInstance(InstancedTileRenderer<?> modelManager, KineticTileEntity tile) {
        super(modelManager, tile);
    }

    @Override
    protected Instancer<RotatingData> getModel() {
        if (blockState.get(FACING).getAxis().isHorizontal()) {
			BlockState referenceState = blockState.rotate(tile.getWorld(), tile.getPos(), Rotation.CLOCKWISE_180);
			Direction facing = referenceState.get(FACING);
			return getRotatingMaterial().getModel(AllBlockPartials.SHAFT_HALF, referenceState, facing);
		} else {
			return getRotatingMaterial().getModel(shaft());
		}
    }
}
