package com.simibubi.create.content.contraptions.components.fan;

import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.content.contraptions.base.IRotate;
import com.simibubi.create.content.contraptions.base.KineticTileInstance;
import com.simibubi.create.content.contraptions.base.RotatingData;
import com.simibubi.create.foundation.render.backend.instancing.InstanceKey;
import com.simibubi.create.foundation.render.backend.instancing.InstancedTileRenderer;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;

import static net.minecraft.state.properties.BlockStateProperties.FACING;

public class FanInstance extends KineticTileInstance<EncasedFanTileEntity> {

    protected final InstanceKey<RotatingData> shaft;
    protected final InstanceKey<RotatingData> fan;
    final Direction.Axis axis;
    final Direction direction;

    public FanInstance(InstancedTileRenderer<?> modelManager, EncasedFanTileEntity tile) {
        super(modelManager, tile);

        direction = blockState.get(FACING);
        axis = ((IRotate) blockState.getBlock()).getRotationAxis(blockState);

        shaft = AllBlockPartials.SHAFT_HALF.renderOnDirectionalSouthRotating(modelManager, blockState, direction.getOpposite()).createInstance();
        fan = AllBlockPartials.ENCASED_FAN_INNER.renderOnDirectionalSouthRotating(modelManager, blockState, direction.getOpposite()).createInstance();

        RotatingData shaftInstance = shaft.getInstance();
        shaftInstance.setTileEntity(tile);
        updateRotation(shaftInstance, axis);

        RotatingData fanInstance = fan.getInstance();
        fanInstance.setTileEntity(tile);
        updateRotation(fanInstance, axis, getFanSpeed());

        updateLight();
    }

    private float getFanSpeed() {
        float speed = tile.getSpeed() * 5;
        if (speed > 0)
            speed = MathHelper.clamp(speed, 80, 64 * 20);
        if (speed < 0)
            speed = MathHelper.clamp(speed, -64 * 20, -80);
        return speed;
    }

    @Override
    protected void update() {
        updateRotation(shaft, axis);
        updateRotation(fan, axis, getFanSpeed());
    }

    @Override
    public void updateLight() {
        BlockPos behind = pos.offset(direction.getOpposite());
        relight(behind, shaft.getInstance());

        BlockPos inFront = pos.offset(direction);
        relight(inFront, fan.getInstance());
    }

    @Override
    public void remove() {
        shaft.delete();
        fan.delete();
    }
}
