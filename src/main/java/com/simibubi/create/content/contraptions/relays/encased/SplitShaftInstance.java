package com.simibubi.create.content.contraptions.relays.encased;

import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.content.contraptions.base.IRotate;
import com.simibubi.create.content.contraptions.base.KineticTileInstance;
import com.simibubi.create.content.contraptions.base.RotatingData;
import com.simibubi.create.foundation.render.backend.instancing.InstanceData;
import com.simibubi.create.foundation.render.backend.instancing.InstancedModel;
import com.simibubi.create.foundation.render.backend.instancing.InstancedTileRenderer;
import com.simibubi.create.foundation.utility.Iterate;
import net.minecraft.block.Block;
import net.minecraft.util.Direction;

import java.util.ArrayList;

public class SplitShaftInstance extends KineticTileInstance<SplitShaftTileEntity> {

    protected final ArrayList<RotatingData> keys;

    public SplitShaftInstance(InstancedTileRenderer<?> modelManager, SplitShaftTileEntity tile) {
        super(modelManager, tile);

        keys = new ArrayList<>(2);

        float speed = tile.getSpeed();

        for (Direction dir : Iterate.directionsInAxis(getRotationAxis())) {

            InstancedModel<RotatingData> half = AllBlockPartials.SHAFT_HALF.renderOnDirectionalSouthRotating(modelManager, blockState, dir);

            float splitSpeed = speed * tile.getRotationSpeedModifier(dir);

            keys.add(setup(half.createInstance(), splitSpeed));
        }
    }

    @Override
    public void update() {
        Block block = blockState.getBlock();
        final Direction.Axis boxAxis = ((IRotate) block).getRotationAxis(blockState);

        Direction[] directions = Iterate.directionsInAxis(boxAxis);

        for (int i : Iterate.zeroAndOne) {
            updateRotation(keys.get(i), tile.getSpeed() * tile.getRotationSpeedModifier(directions[i]));
        }
    }

    @Override
    public void updateLight() {
        relight(pos, keys.stream());
    }

    @Override
    public void remove() {
        keys.forEach(InstanceData::delete);
        keys.clear();
    }

}
