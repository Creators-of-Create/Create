package com.simibubi.create.content.contraptions.components.fan;

import static net.minecraft.state.properties.BlockStateProperties.FACING;

import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.content.contraptions.base.IRotate;
import com.simibubi.create.content.contraptions.base.KineticTileInstance;
import com.simibubi.create.content.contraptions.base.RotatingData;
import com.simibubi.create.foundation.render.backend.instancing.InstanceKey;
import com.simibubi.create.foundation.render.backend.instancing.InstancedModel;
import com.simibubi.create.foundation.render.backend.instancing.InstancedTileRenderRegistry;
import com.simibubi.create.foundation.render.backend.instancing.InstancedTileRenderer;

import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.LightType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;

public class FanInstance extends KineticTileInstance<EncasedFanTileEntity> {
    public static void register(TileEntityType<? extends EncasedFanTileEntity> type) {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
                InstancedTileRenderRegistry.instance.register(type, FanInstance::new));
    }

    protected InstanceKey<RotatingData> shaft;
    protected InstanceKey<RotatingData> fan;

    public FanInstance(InstancedTileRenderer modelManager, EncasedFanTileEntity tile) {
        super(modelManager, tile);
    }

    @Override
    protected void init() {
        final Direction direction = lastState.get(FACING);
        final Direction.Axis axis = ((IRotate) lastState.getBlock()).getRotationAxis(lastState);

        InstancedModel<RotatingData> shaftHalf =
                AllBlockPartials.SHAFT_HALF.renderOnDirectionalSouthRotating(modelManager, lastState, direction.getOpposite());
        InstancedModel<RotatingData> fanInner =
                AllBlockPartials.ENCASED_FAN_INNER.renderOnDirectionalSouthRotating(modelManager, lastState, direction.getOpposite());

        shaft = shaftHalf.setupInstance(data -> {
            BlockPos behind = pos.offset(direction.getOpposite());
            int blockLight = world.getLightLevel(LightType.BLOCK, behind);
            int skyLight = world.getLightLevel(LightType.SKY, behind);

            data.setRotationalSpeed(tile.getSpeed())
                .setRotationOffset(getRotationOffset(axis))
                .setRotationAxis(Direction.getFacingFromAxis(Direction.AxisDirection.POSITIVE, axis).getUnitVector())
                .setTileEntity(tile)
                .setBlockLight(blockLight)
                .setSkyLight(skyLight);
        });
        fan = fanInner.setupInstance(data -> {
            BlockPos inFront = pos.offset(direction);
            int blockLight = world.getLightLevel(LightType.BLOCK, inFront);
            int skyLight = world.getLightLevel(LightType.SKY, inFront);

            data.setRotationalSpeed(getFanSpeed())
                .setRotationOffset(getRotationOffset(axis))
                .setRotationAxis(Direction.getFacingFromAxis(Direction.AxisDirection.POSITIVE, axis).getUnitVector())
                .setTileEntity(tile)
                .setBlockLight(blockLight)
                .setSkyLight(skyLight);
        });
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
    protected void onUpdate() {
        Direction.Axis axis = lastState.get(FACING).getAxis();
        updateRotation(shaft, axis);

        fan.modifyInstance(data -> {
            data.setColor(tile.network)
                .setRotationalSpeed(getFanSpeed())
                .setRotationOffset(getRotationOffset(axis))
                .setRotationAxis(Direction.getFacingFromAxis(Direction.AxisDirection.POSITIVE, axis).getUnitVector());
        });
    }

    @Override
    public void updateLight() {
        final Direction direction = lastState.get(FACING);

        shaft.modifyInstance(data -> {
            BlockPos behind = pos.offset(direction.getOpposite());
            int blockLight = world.getLightLevel(LightType.BLOCK, behind);
            int skyLight = world.getLightLevel(LightType.SKY, behind);
            data.setBlockLight(blockLight)
                .setSkyLight(skyLight);
        });
        fan.modifyInstance(data -> {
            BlockPos inFront = pos.offset(direction);
            int blockLight = world.getLightLevel(LightType.BLOCK, inFront);
            int skyLight = world.getLightLevel(LightType.SKY, inFront);
            data.setBlockLight(blockLight)
                .setSkyLight(skyLight);
        });
    }

    @Override
    public void remove() {
        shaft.delete();
        fan.delete();
    }
}
