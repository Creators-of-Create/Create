package com.simibubi.create.content.contraptions.components.saw;

import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.content.contraptions.base.KineticTileEntity;
import com.simibubi.create.content.contraptions.base.KineticTileEntityRenderer;
import com.simibubi.create.content.contraptions.base.RotatingData;
import com.simibubi.create.content.contraptions.base.SingleRotatingInstance;
import com.simibubi.create.foundation.render.backend.instancing.InstancedModel;
import com.simibubi.create.foundation.render.backend.instancing.InstancedTileRenderer;
import net.minecraft.util.Rotation;

import static net.minecraft.state.properties.BlockStateProperties.FACING;

public class SawInstance extends SingleRotatingInstance {

    public SawInstance(InstancedTileRenderer<?> modelManager, KineticTileEntity tile) {
        super(modelManager, tile);
    }

    @Override
    protected InstancedModel<RotatingData> getModel() {
        if (blockState.get(FACING).getAxis().isHorizontal())
            return AllBlockPartials.SHAFT_HALF.renderOnDirectionalSouthRotating(modelManager, blockState.rotate(tile.getWorld(), tile.getPos(), Rotation.CLOCKWISE_180));
        else
            return rotatingMaterial().getModel(KineticTileEntityRenderer.KINETIC_TILE, shaft(getRotationAxis()));
    }
}
