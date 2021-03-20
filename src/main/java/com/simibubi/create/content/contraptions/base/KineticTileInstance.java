package com.simibubi.create.content.contraptions.base;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.contraptions.relays.elementary.CogWheelBlock;
import com.simibubi.create.content.contraptions.relays.elementary.ShaftBlock;
import com.simibubi.create.foundation.render.backend.instancing.*;
import com.simibubi.create.foundation.render.backend.instancing.InstancedModel;

import net.minecraft.block.BlockState;
import net.minecraft.util.Direction;
import net.minecraft.world.LightType;

public abstract class KineticTileInstance<T extends KineticTileEntity> extends TileEntityInstance<T> {

    public KineticTileInstance(InstancedTileRenderer<?> modelManager, T tile) {
        super(modelManager, tile);
    }

    protected final void updateRotation(InstanceKey<RotatingData> key, Direction.Axis axis) {
        key.getInstance()
           .setColor(tile.network)
           .setRotationalSpeed(tile.getSpeed())
           .setRotationOffset(getRotationOffset(axis))
           .setRotationAxis(axis);
    }

    protected final InstanceKey<RotatingData> setup(InstanceKey<RotatingData> key, float speed, Direction.Axis axis) {
        key.getInstance()
           .setBlockLight(world.getLightLevel(LightType.BLOCK, pos))
           .setSkyLight(world.getLightLevel(LightType.SKY, pos))
           .setTileEntity(tile)
           .setRotationalSpeed(speed)
           .setRotationOffset(getRotationOffset(axis))
           .setRotationAxis(axis);

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
