package com.simibubi.create.content.kinetics.gearbox;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Consumer;

import dev.engine_room.flywheel.api.instance.Instance;
import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import dev.engine_room.flywheel.lib.instance.AbstractInstance;
import dev.engine_room.flywheel.lib.instance.FlatLit;
import dev.engine_room.flywheel.lib.model.Models;
import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityVisual;
import com.simibubi.create.content.kinetics.base.RotatingInstance;
import com.simibubi.create.foundation.render.AllInstanceTypes;
import com.simibubi.create.foundation.utility.Iterate;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public class GearboxVisual extends KineticBlockEntityVisual<GearboxBlockEntity> {

    protected final EnumMap<Direction, RotatingInstance> keys;
    protected Direction sourceFacing;

    public GearboxVisual(VisualizationContext context, GearboxBlockEntity blockEntity, float partialTick) {
        super(context, blockEntity, partialTick);

        keys = new EnumMap<>(Direction.class);

        final Direction.Axis boxAxis = blockState.getValue(BlockStateProperties.AXIS);

        int blockLight = level.getBrightness(LightLayer.BLOCK, pos);
        int skyLight = level.getBrightness(LightLayer.SKY, pos);
        updateSourceFacing();

        for (Direction direction : Iterate.directions) {
			final Direction.Axis axis = direction.getAxis();
			if (boxAxis == axis)
				continue;

			RotatingInstance key = instancerProvider.instancer(AllInstanceTypes.ROTATING, Models.partial(AllPartialModels.SHAFT_HALF, direction))
					.createInstance();

			key.setRotationAxis(axis)
					.setRotationalSpeed(getSpeed(direction))
					.setRotationOffset(getRotationOffset(axis)).setColor(blockEntity)
					.setPosition(getVisualPosition())
					.light(blockLight, skyLight)
					.setChanged();

            keys.put(direction, key);
        }
    }

    private float getSpeed(Direction direction) {
        float speed = blockEntity.getSpeed();

        if (speed != 0 && sourceFacing != null) {
            if (sourceFacing.getAxis() == direction.getAxis())
                speed *= sourceFacing == direction ? 1 : -1;
            else if (sourceFacing.getAxisDirection() == direction.getAxisDirection())
                speed *= -1;
        }
        return speed;
    }

    protected void updateSourceFacing() {
        if (blockEntity.hasSource()) {
            BlockPos source = blockEntity.source.subtract(pos);
            sourceFacing = Direction.getNearest(source.getX(), source.getY(), source.getZ());
        } else {
            sourceFacing = null;
        }
    }

    @Override
    public void update(float pt) {
        updateSourceFacing();
        for (Map.Entry<Direction, RotatingInstance> key : keys.entrySet()) {
            Direction direction = key.getKey();
            Direction.Axis axis = direction.getAxis();

            updateRotation(key.getValue(), axis, getSpeed(direction));
        }
    }

    @Override
    public void updateLight(float partialTick) {
        relight(pos, keys.values().toArray(FlatLit[]::new));
    }

    @Override
    protected void _delete() {
        keys.values().forEach(AbstractInstance::delete);
        keys.clear();
    }

	@Override
	public void collectCrumblingInstances(Consumer<Instance> consumer) {
		keys.values()
				.forEach(consumer);
	}
}
