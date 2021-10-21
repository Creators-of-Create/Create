package com.simibubi.create.content.contraptions.relays.gearbox;

import java.util.EnumMap;
import java.util.Map;

import com.jozufozu.flywheel.backend.instancing.InstanceData;
import com.jozufozu.flywheel.backend.instancing.Instancer;
import com.jozufozu.flywheel.backend.material.InstanceMaterial;
import com.jozufozu.flywheel.backend.material.MaterialManager;
import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.content.contraptions.base.KineticTileInstance;
import com.simibubi.create.content.contraptions.base.RotatingData;
import com.simibubi.create.foundation.utility.Iterate;

import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.LightType;

public class GearboxInstance extends KineticTileInstance<GearboxTileEntity> {

    protected final EnumMap<Direction, RotatingData> keys;
    protected Direction sourceFacing;

    public GearboxInstance(MaterialManager<?> modelManager, GearboxTileEntity tile) {
        super(modelManager, tile);

        keys = new EnumMap<>(Direction.class);

        final Direction.Axis boxAxis = blockState.getValue(BlockStateProperties.AXIS);

        int blockLight = world.getBrightness(LightType.BLOCK, pos);
        int skyLight = world.getBrightness(LightType.SKY, pos);
        updateSourceFacing();

        InstanceMaterial<RotatingData> rotatingMaterial = getRotatingMaterial();

        for (Direction direction : Iterate.directions) {
			final Direction.Axis axis = direction.getAxis();
			if (boxAxis == axis)
				continue;

			Instancer<RotatingData> shaft = rotatingMaterial.getModel(AllBlockPartials.SHAFT_HALF, blockState, direction);

			RotatingData key = shaft.createInstance();

			key.setRotationAxis(Direction.get(Direction.AxisDirection.POSITIVE, axis).step())
					.setRotationalSpeed(getSpeed(direction))
					.setRotationOffset(getRotationOffset(axis)).setColor(tile)
					.setPosition(getInstancePosition())
					.setBlockLight(blockLight)
					.setSkyLight(skyLight);

            keys.put(direction, key);
        }
    }

    private float getSpeed(Direction direction) {
        float speed = tile.getSpeed();

        if (speed != 0 && sourceFacing != null) {
            if (sourceFacing.getAxis() == direction.getAxis())
                speed *= sourceFacing == direction ? 1 : -1;
            else if (sourceFacing.getAxisDirection() == direction.getAxisDirection())
                speed *= -1;
        }
        return speed;
    }

    protected void updateSourceFacing() {
        if (tile.hasSource()) {
            BlockPos source = tile.source.subtract(pos);
            sourceFacing = Direction.getNearest(source.getX(), source.getY(), source.getZ());
        } else {
            sourceFacing = null;
        }
    }

    @Override
    public void update() {
        updateSourceFacing();
        for (Map.Entry<Direction, RotatingData> key : keys.entrySet()) {
            Direction direction = key.getKey();
            Direction.Axis axis = direction.getAxis();

            updateRotation(key.getValue(), axis, getSpeed(direction));
        }
    }

    @Override
    public void updateLight() {
        relight(pos, keys.values().stream());
    }

    @Override
    public void remove() {
        keys.values().forEach(InstanceData::delete);
        keys.clear();
    }
}
