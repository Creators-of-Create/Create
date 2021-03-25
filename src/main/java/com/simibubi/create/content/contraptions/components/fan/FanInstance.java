package com.simibubi.create.content.contraptions.components.fan;

import static net.minecraft.state.properties.BlockStateProperties.FACING;

import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.content.contraptions.base.IRotate;
import com.simibubi.create.content.contraptions.base.KineticTileInstance;
import com.simibubi.create.content.contraptions.base.RotatingData;
import com.simibubi.create.foundation.render.backend.instancing.InstanceKey;
import com.simibubi.create.foundation.render.backend.instancing.InstancedTileRenderer;

import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;

public class FanInstance extends KineticTileInstance<EncasedFanTileEntity> {

    protected final InstanceKey<RotatingData> shaft;
    protected final InstanceKey<RotatingData> fan;
    final Direction direction;

    public FanInstance(InstancedTileRenderer<?> modelManager, EncasedFanTileEntity tile) {
        super(modelManager, tile);

        direction = blockState.get(FACING);

        shaft = AllBlockPartials.SHAFT_HALF.renderOnDirectionalSouthRotating(modelManager, blockState, direction.getOpposite()).createInstance();
        fan = AllBlockPartials.ENCASED_FAN_INNER.renderOnDirectionalSouthRotating(modelManager, blockState, direction.getOpposite()).createInstance();

        setup(shaft);
        setup(fan, getFanSpeed());
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
        updateRotation(shaft.getInstance());
        updateRotation(fan.getInstance(), getFanSpeed());
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
