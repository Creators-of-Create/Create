package com.simibubi.create.content.contraptions.relays.encased;

import java.util.ArrayList;

import com.jozufozu.flywheel.api.InstanceData;
import com.jozufozu.flywheel.api.Instancer;
import com.jozufozu.flywheel.api.Material;
import com.jozufozu.flywheel.api.MaterialManager;
import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.content.contraptions.base.IRotate;
import com.simibubi.create.content.contraptions.base.KineticTileInstance;
import com.simibubi.create.content.contraptions.base.flwdata.RotatingData;
import com.simibubi.create.foundation.utility.Iterate;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;

public class SplitShaftInstance extends KineticTileInstance<SplitShaftTileEntity> {

    protected final ArrayList<RotatingData> keys;

    public SplitShaftInstance(MaterialManager modelManager, SplitShaftTileEntity tile) {
        super(modelManager, tile);

        keys = new ArrayList<>(2);

        float speed = tile.getSpeed();

        Material<RotatingData> rotatingMaterial = getRotatingMaterial();

        for (Direction dir : Iterate.directionsInAxis(getRotationAxis())) {

			Instancer<RotatingData> half = rotatingMaterial.getModel(AllBlockPartials.SHAFT_HALF, blockState, dir);

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
            updateRotation(keys.get(i), blockEntity.getSpeed() * blockEntity.getRotationSpeedModifier(directions[i]));
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
