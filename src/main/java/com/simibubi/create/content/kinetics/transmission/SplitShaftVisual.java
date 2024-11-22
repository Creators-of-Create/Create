package com.simibubi.create.content.kinetics.transmission;

import java.util.ArrayList;
import java.util.function.Consumer;

import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.kinetics.base.IRotate;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityVisual;
import com.simibubi.create.content.kinetics.base.RotatingInstance;
import com.simibubi.create.foundation.render.AllInstanceTypes;
import com.simibubi.create.foundation.utility.Iterate;

import dev.engine_room.flywheel.api.instance.Instance;
import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import dev.engine_room.flywheel.lib.instance.AbstractInstance;
import dev.engine_room.flywheel.lib.instance.FlatLit;
import dev.engine_room.flywheel.lib.model.Models;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;

public class SplitShaftVisual extends KineticBlockEntityVisual<SplitShaftBlockEntity> {

    protected final ArrayList<RotatingInstance> keys;

    public SplitShaftVisual(VisualizationContext modelManager, SplitShaftBlockEntity blockEntity, float partialTick) {
        super(modelManager, blockEntity, partialTick);

        keys = new ArrayList<>(2);

        float speed = blockEntity.getSpeed();

        for (Direction dir : Iterate.directionsInAxis(getRotationAxis())) {

            float splitSpeed = speed * blockEntity.getRotationSpeedModifier(dir);

			var instance = instancerProvider().instancer(AllInstanceTypes.ROTATING, Models.partial(AllPartialModels.SHAFT_HALF, dir))
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
    public void updateLight(float partialTick) {
        relight(keys.toArray(FlatLit[]::new));
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
