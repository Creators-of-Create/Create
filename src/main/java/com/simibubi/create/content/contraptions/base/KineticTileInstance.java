package com.simibubi.create.content.contraptions.base;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.contraptions.relays.elementary.CogWheelBlock;
import com.simibubi.create.content.contraptions.relays.elementary.ShaftBlock;
import com.simibubi.create.foundation.render.backend.instancing.*;
import net.minecraft.block.BlockState;
import net.minecraft.util.Direction;

public abstract class KineticTileInstance<T extends KineticTileEntity> extends TileEntityInstance<T> {

    protected final Direction.Axis axis;

    public KineticTileInstance(InstancedTileRenderer<?> modelManager, T tile) {
        super(modelManager, tile);

        axis = ((IRotate) blockState.getBlock()).getRotationAxis(blockState);
    }

    protected final void updateRotation(RotatingData instance) {
        updateRotation(instance, getRotationAxis(), getTileSpeed());
    }

    protected final void updateRotation(RotatingData instance, Direction.Axis axis) {
        updateRotation(instance, axis, getTileSpeed());
    }

    protected final void updateRotation(RotatingData instance, float speed) {
        updateRotation(instance, getRotationAxis(), speed);
    }

    protected final void updateRotation(RotatingData instance, Direction.Axis axis, float speed) {
        instance.setRotationAxis(axis)
                .setRotationOffset(getRotationOffset(axis))
                .setRotationalSpeed(speed)
                .setColor(tile);
    }

    protected final InstanceKey<RotatingData> setup(InstanceKey<RotatingData> key) {
        return setup(key, getRotationAxis(), getTileSpeed());
    }

    protected final InstanceKey<RotatingData> setup(InstanceKey<RotatingData> key, Direction.Axis axis) {
        return setup(key, axis, getTileSpeed());
    }

    protected final InstanceKey<RotatingData> setup(InstanceKey<RotatingData> key, float speed) {
        return setup(key, getRotationAxis(), speed);
    }

    protected final InstanceKey<RotatingData> setup(InstanceKey<RotatingData> key, Direction.Axis axis, float speed) {
        key.getInstance()
                .setRotationAxis(axis)
                .setRotationalSpeed(speed)
                .setRotationOffset(getRotationOffset(axis))
                .setColor(tile)
                .setPosition(getInstancePosition());

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

    protected Direction.Axis getRotationAxis() {
        return axis;
    }

    protected float getTileSpeed() {
        return tile.getSpeed();
    }

    protected BlockState shaft() {
        return shaft(getRotationAxis());
    }

    protected final RenderMaterial<?, InstancedModel<RotatingData>> getRotatingMaterial() {
        return renderer.getMaterial(KineticRenderMaterials.ROTATING);
    }

    public static BlockState shaft(Direction.Axis axis) {
        return AllBlocks.SHAFT.getDefaultState()
                .with(ShaftBlock.AXIS, axis);
    }
}
