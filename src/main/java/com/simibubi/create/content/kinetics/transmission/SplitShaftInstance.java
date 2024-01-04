package com.simibubi.create.content.kinetics.transmission;

import java.util.ArrayList;
import java.util.function.Consumer;

import com.jozufozu.flywheel.api.event.RenderStage;
import com.jozufozu.flywheel.api.instance.Instance;
import com.jozufozu.flywheel.api.instance.Instancer;
import com.jozufozu.flywheel.api.visualization.VisualizationContext;
import com.jozufozu.flywheel.lib.instance.AbstractInstance;
import com.jozufozu.flywheel.lib.model.Models;
import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.kinetics.base.IRotate;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityInstance;
import com.simibubi.create.content.kinetics.base.flwdata.RotatingInstance;
import com.simibubi.create.foundation.render.AllInstanceTypes;
import com.simibubi.create.foundation.utility.Iterate;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class SplitShaftInstance extends KineticBlockEntityInstance<SplitShaftBlockEntity> {

    protected final ArrayList<RotatingInstance> keys;

    public SplitShaftInstance(VisualizationContext modelManager, SplitShaftBlockEntity blockEntity) {
        super(modelManager, blockEntity);

        keys = new ArrayList<>(2);

        float speed = blockEntity.getSpeed();

        for (Direction dir : Iterate.directionsInAxis(getRotationAxis())) {

            float splitSpeed = speed * blockEntity.getRotationSpeedModifier(dir);

			var instance = instancerProvider.instancer(AllInstanceTypes.ROTATING, Models.partial(AllPartialModels.SHAFT_HALF, dir), RenderStage.AFTER_BLOCK_ENTITIES)
                .createInstance();

			keys.add(setup(instance, splitSpeed));
		}
    }

    @Override
    public void update(float pt) {
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
    protected void _delete() {
        keys.forEach(AbstractInstance::delete);
        keys.clear();
    }

	@Override
	public void collectCrumblingInstances(Consumer<Instance> consumer) {
		keys.forEach(consumer);
	}
}
