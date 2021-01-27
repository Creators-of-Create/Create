package com.simibubi.create.content.contraptions.base;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.contraptions.relays.elementary.CogWheelBlock;
import com.simibubi.create.content.contraptions.relays.elementary.ShaftBlock;
import com.simibubi.create.foundation.render.InstancedTileRenderDispatcher;
import com.simibubi.create.foundation.render.instancing.*;
import net.minecraft.block.BlockState;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.LightType;
import net.minecraft.world.World;

import java.util.function.Consumer;

public abstract class KineticTileInstance<T extends KineticTileEntity> extends TileEntityInstance<T> {

    public KineticTileInstance(InstancedTileRenderDispatcher modelManager, T tile) {
        super(modelManager, tile);
    }

    protected void updateRotation(InstanceKey<RotatingData> key, Direction.Axis axis) {
        key.modifyInstance(data -> {
            final BlockPos pos = tile.getPos();

            data.setRotationalSpeed(tile.getSpeed())
                .setRotationOffset(getRotationOffsetForPosition(tile, pos, axis))
                .setRotationAxis(axis);
        });
    }

    protected final Consumer<RotatingData> setupFunc(float speed, Direction.Axis axis) {
        return data -> {
            final BlockPos pos = tile.getPos();

            data.setBlockLight(tile.getWorld().getLightLevel(LightType.BLOCK, tile.getPos()))
                .setSkyLight(tile.getWorld().getLightLevel(LightType.SKY, tile.getPos()))
                .setTileEntity(tile)
                .setRotationalSpeed(speed)
                .setRotationOffset(getRotationOffsetForPosition(tile, pos, axis))
                .setRotationAxis(axis);
        };
    }

    protected final void relight(KineticData<?> data) {
        World world = tile.getWorld();

        data.setBlockLight(world.getLightLevel(LightType.BLOCK, tile.getPos()))
            .setSkyLight(world.getLightLevel(LightType.SKY, tile.getPos()));
    }

    protected static float getRotationOffsetForPosition(KineticTileEntity te, final BlockPos pos, final Direction.Axis axis) {
        float offset = CogWheelBlock.isLargeCog(te.getBlockState()) ? 11.25f : 0;
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

    public static Direction.Axis getRotationAxisOf(KineticTileEntity te) {
        return ((IRotate) te.getBlockState()
                            .getBlock()).getRotationAxis(te.getBlockState());
    }

    protected final RenderMaterial<InstancedModel<RotatingData>> rotatingMaterial() {
        return modelManager.get(KineticRenderMaterials.ROTATING);
    }
}
