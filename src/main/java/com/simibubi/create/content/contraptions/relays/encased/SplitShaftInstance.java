package com.simibubi.create.content.contraptions.relays.encased;

import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.content.contraptions.base.IRotate;
import com.simibubi.create.content.contraptions.base.KineticTileInstance;
import com.simibubi.create.foundation.render.InstancedTileRenderDispatcher;
import com.simibubi.create.foundation.render.instancing.InstanceKey;
import com.simibubi.create.foundation.render.instancing.InstancedModel;
import com.simibubi.create.foundation.render.instancing.InstancedTileRenderRegistry;
import com.simibubi.create.foundation.render.instancing.RotatingData;
import com.simibubi.create.foundation.utility.Iterate;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;

public class SplitShaftInstance extends KineticTileInstance<SplitShaftTileEntity> {
    public static void register(TileEntityType<? extends SplitShaftTileEntity> type) {
        InstancedTileRenderRegistry.instance.register(type, SplitShaftInstance::new);
    }

    protected ArrayList<InstanceKey<RotatingData>> keys;

    public SplitShaftInstance(InstancedTileRenderDispatcher modelManager, SplitShaftTileEntity tile) {
        super(modelManager, tile);
    }

    @Override
    protected void init() {
        keys = new ArrayList<>(2);

        BlockState state = tile.getBlockState();
        Block block = state.getBlock();
        final Direction.Axis boxAxis = ((IRotate) block).getRotationAxis(state);

        float speed = tile.getSpeed();

        for (Direction dir : Iterate.directionsInAxis(boxAxis)) {

            InstancedModel<RotatingData> half = AllBlockPartials.SHAFT_HALF.renderOnDirectionalSouthRotating(modelManager, state, dir);

            float splitSpeed = speed * tile.getRotationSpeedModifier(dir);

            keys.add(half.setupInstance(setupFunc(splitSpeed, boxAxis)));
        }
    }

    @Override
    public void onUpdate() {
        BlockState state = tile.getBlockState();
        Block block = state.getBlock();
        final Direction.Axis boxAxis = ((IRotate) block).getRotationAxis(state);

        Direction[] directions = Iterate.directionsInAxis(boxAxis);

        for (int i : Iterate.zeroAndOne) {
            updateRotation(keys.get(i), directions[i]);
        }
    }

    @Override
    public void updateLight() {
        for (InstanceKey<RotatingData> key : keys) {
            key.modifyInstance(this::relight);
        }
    }

    @Override
    public void remove() {
        keys.forEach(InstanceKey::delete);
        keys.clear();
    }

    protected void updateRotation(InstanceKey<RotatingData> key, Direction dir) {
        key.modifyInstance(data -> {
            Direction.Axis axis = dir.getAxis();
            final BlockPos pos = tile.getPos();

            data.setRotationalSpeed(tile.getSpeed() * tile.getRotationSpeedModifier(dir))
                .setRotationOffset(getRotationOffsetForPosition(tile, pos, axis))
                .setRotationAxis(Direction.getFacingFromAxis(Direction.AxisDirection.POSITIVE, axis).getUnitVector());
        });
    }
}
