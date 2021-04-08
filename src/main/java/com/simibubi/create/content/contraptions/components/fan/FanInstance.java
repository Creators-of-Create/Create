package com.simibubi.create.content.contraptions.components.fan;

import static net.minecraft.state.properties.BlockStateProperties.FACING;

import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.content.contraptions.base.KineticTileInstance;
import com.simibubi.create.content.contraptions.base.RotatingData;
import com.simibubi.create.foundation.render.backend.instancing.InstancedTileRenderer;

import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;

public class FanInstance extends KineticTileInstance<EncasedFanTileEntity> {

    protected final RotatingData shaft;
    protected final RotatingData fan;
    final Direction direction;
    private final Direction opposite;

    public FanInstance(InstancedTileRenderer<?> modelManager, EncasedFanTileEntity tile) {
        super(modelManager, tile);

        direction = blockState.get(FACING);

        opposite = direction.getOpposite();
        shaft = AllBlockPartials.SHAFT_HALF.getModel(getRotatingMaterial(), blockState, opposite).createInstance();
        fan = AllBlockPartials.ENCASED_FAN_INNER.getModel(getRotatingMaterial(), blockState, opposite).createInstance();

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
        updateRotation(shaft);
        updateRotation(fan, getFanSpeed());
    }

    @Override
    public void updateLight() {
        BlockPos behind = pos.offset(opposite);
        relight(behind, shaft);

        BlockPos inFront = pos.offset(direction);
        relight(inFront, fan);
    }

    @Override
    public void remove() {
        shaft.delete();
        fan.delete();
    }
}
