package com.simibubi.create.content.contraptions.base;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.contraptions.relays.elementary.CogWheelBlock;
import com.simibubi.create.content.contraptions.relays.elementary.ShaftBlock;
import com.simibubi.create.foundation.render.backend.instancing.*;
import net.minecraft.block.BlockState;
import net.minecraft.util.Direction;
import net.minecraft.world.LightType;

public abstract class KineticTileInstance<T extends KineticTileEntity> extends TileEntityInstance<T> {

    public KineticTileInstance(InstancedTileRenderer<?> modelManager, T tile) {
        super(modelManager, tile);
    }

    protected final void updateRotation(InstanceKey<RotatingData> key, Direction.Axis axis) {
        updateRotation(key, axis, tile.getSpeed());
    }

    protected final void updateRotation(InstanceKey<RotatingData> key, Direction.Axis axis, float speed) {
        updateRotation(key.getInstance(), axis, speed);
    }

    protected final void updateRotation(RotatingData key, Direction.Axis axis, float speed) {
        key.setRotationAxis(axis)
                .setRotationOffset(getRotationOffset(axis))
                .setRotationalSpeed(speed)
                .setColor(tile.network);
    }

    protected final void updateRotation(RotatingData key, Direction.Axis axis) {
        updateRotation(key, axis, tile.getSpeed());
    }

    protected final InstanceKey<RotatingData> setup(InstanceKey<RotatingData> key, float speed, Direction.Axis axis) {
        key.getInstance()
                .setRotationAxis(axis)
                .setRotationalSpeed(speed)
                .setRotationOffset(getRotationOffset(axis))
                .setTileEntity(tile)
                .setSkyLight(world.getLightLevel(LightType.SKY, pos))
                .setBlockLight(world.getLightLevel(LightType.BLOCK, pos));

        return key;
    }

    protected float getRotationOffset(final Direction.Axis axis) {
        float offset = CogWheelBlock.isLargeCog(blockState) ? 11.25f : 0;
        double d = (((axis == Direction.Axis.X) ? 0 : pos.getX()) + ((axis == Direction.Axis.Y) ? 0 : pos.getY())
                + ((axis == Direction.Axis.Z) ? 0 : pos.getZ())) % 2;
        if (d == 0) {
            offset = 22.5f;
        }
        return offset;
    }

    public static BlockState shaft(Direction.Axis axis) {
        return AllBlocks.SHAFT.getDefaultState()
                              .with(ShaftBlock.AXIS, axis);
    }

    public Direction.Axis getRotationAxis() {
        return ((IRotate) blockState.getBlock()).getRotationAxis(blockState);
    }

    protected final RenderMaterial<?, InstancedModel<RotatingData>> rotatingMaterial() {
        return modelManager.getMaterial(KineticRenderMaterials.ROTATING);
    }
}
