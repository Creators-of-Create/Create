package com.simibubi.create.content.contraptions.relays.gearbox;

import java.util.EnumMap;
import java.util.Map;

import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.content.contraptions.base.KineticTileInstance;
import com.simibubi.create.content.contraptions.base.RotatingData;
import com.simibubi.create.foundation.render.backend.instancing.InstanceKey;
import com.simibubi.create.foundation.render.backend.instancing.InstancedModel;
import com.simibubi.create.foundation.render.backend.instancing.InstancedTileRenderer;
import com.simibubi.create.foundation.utility.Iterate;

import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.LightType;

public class GearboxInstance extends KineticTileInstance<GearboxTileEntity> {

    protected final EnumMap<Direction, InstanceKey<RotatingData>> keys;
    protected Direction sourceFacing;

    public GearboxInstance(InstancedTileRenderer<?> modelManager, GearboxTileEntity tile) {
        super(modelManager, tile);

        keys = new EnumMap<>(Direction.class);

        final Direction.Axis boxAxis = blockState.get(BlockStateProperties.AXIS);

        int blockLight = world.getLightLevel(LightType.BLOCK, pos);
        int skyLight = world.getLightLevel(LightType.SKY, pos);
        updateSourceFacing();

        for (Direction direction : Iterate.directions) {
            final Direction.Axis axis = direction.getAxis();
            if (boxAxis == axis)
                continue;

            InstancedModel<RotatingData> shaft = AllBlockPartials.SHAFT_HALF.renderOnDirectionalSouthRotating(modelManager, blockState, direction);

            InstanceKey<RotatingData> key = shaft.createInstance();

            key.getInstance()
                    .setRotationAxis(Direction.getFacingFromAxis(Direction.AxisDirection.POSITIVE, axis).getUnitVector())
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
            sourceFacing = Direction.getFacingFromVector(source.getX(), source.getY(), source.getZ());
        } else {
            sourceFacing = null;
        }
    }

    @Override
    public void update() {
        updateSourceFacing();
        for (Map.Entry<Direction, InstanceKey<RotatingData>> key : keys.entrySet()) {
            Direction direction = key.getKey();
            Direction.Axis axis = direction.getAxis();

            updateRotation(key.getValue().getInstance(), axis, getSpeed(direction));
        }
    }

    @Override
    public void updateLight() {
        relight(pos, keys.values().stream().map(InstanceKey::getInstance));
    }

    @Override
    public void remove() {
        keys.values().forEach(InstanceKey::delete);
        keys.clear();
    }
}
