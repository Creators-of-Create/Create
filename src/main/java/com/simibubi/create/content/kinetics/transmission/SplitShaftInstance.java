package com.simibubi.create.content.kinetics.transmission;

import java.util.ArrayList;

import com.jozufozu.flywheel.api.InstanceData;
import com.jozufozu.flywheel.api.Instancer;
import com.jozufozu.flywheel.api.Material;
import com.jozufozu.flywheel.api.MaterialManager;
import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.kinetics.base.IRotate;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityInstance;
import com.simibubi.create.content.kinetics.base.flwdata.RotatingInstance;
import com.simibubi.create.foundation.render.AllInstanceTypes;
import com.simibubi.create.foundation.utility.Iterate;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;

public class SplitShaftInstance extends KineticBlockEntityInstance<SplitShaftBlockEntity> {

    protected final ArrayList<RotatingInstance> keys;

    public SplitShaftInstance(VisualizationContext modelManager, SplitShaftBlockEntity blockEntity) {
        super(modelManager, blockEntity);

        keys = new ArrayList<>(2);

        float speed = blockEntity.getSpeed();

		Material<RotatingInstance> rotatingMaterial = materialManager.defaultSolid()
				.material(AllInstanceTypes.ROTATING);

        for (Direction dir : Iterate.directionsInAxis(getRotationAxis())) {

			Instancer<RotatingInstance> half = rotatingMaterial.getModel(AllPartialModels.SHAFT_HALF, blockState, dir);

			float splitSpeed = speed * blockEntity.getRotationSpeedModifier(dir);

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
        keys.forEach(AbstractInstance::delete);
        keys.clear();
    }

}
