package com.simibubi.create.content.contraptions.relays.encased;

import java.util.ArrayList;

import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.content.contraptions.base.IRotate;
import com.simibubi.create.content.contraptions.base.KineticTileInstance;
import com.simibubi.create.content.contraptions.base.RotatingData;
import com.simibubi.create.foundation.render.backend.instancing.InstanceKey;
import com.simibubi.create.foundation.render.backend.instancing.InstancedModel;
import com.simibubi.create.foundation.render.backend.instancing.InstancedTileRenderRegistry;
import com.simibubi.create.foundation.render.backend.instancing.InstancedTileRenderer;
import com.simibubi.create.foundation.utility.Iterate;

import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;

public class SplitShaftInstance extends KineticTileInstance<SplitShaftTileEntity> {
    public static void register(TileEntityType<? extends SplitShaftTileEntity> type) {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
                InstancedTileRenderRegistry.instance.register(type, SplitShaftInstance::new));
    }

    protected ArrayList<InstanceKey<RotatingData>> keys;

    public SplitShaftInstance(InstancedTileRenderer modelManager, SplitShaftTileEntity tile) {
        super(modelManager, tile);
    }

    @Override
    protected void init() {
        keys = new ArrayList<>(2);

        Block block = lastState.getBlock();
        final Direction.Axis boxAxis = ((IRotate) block).getRotationAxis(lastState);

        float speed = tile.getSpeed();

        for (Direction dir : Iterate.directionsInAxis(boxAxis)) {

            InstancedModel<RotatingData> half = AllBlockPartials.SHAFT_HALF.renderOnDirectionalSouthRotating(modelManager, lastState, dir);

            float splitSpeed = speed * tile.getRotationSpeedModifier(dir);

            keys.add(setup(half.createInstance(), splitSpeed, boxAxis));
        }
    }

    @Override
    public void onUpdate() {
        Block block = lastState.getBlock();
        final Direction.Axis boxAxis = ((IRotate) block).getRotationAxis(lastState);

        Direction[] directions = Iterate.directionsInAxis(boxAxis);

        for (int i : Iterate.zeroAndOne) {
            updateRotation(keys.get(i), directions[i]);
        }
    }

    @Override
    public void updateLight() {
        keys.forEach(this::relight);
    }

    @Override
    public void remove() {
        keys.forEach(InstanceKey::delete);
        keys.clear();
    }

    protected void updateRotation(InstanceKey<RotatingData> key, Direction dir) {
        Direction.Axis axis = dir.getAxis();

        key.getInstance()
           .setColor(tile.network)
           .setRotationalSpeed(tile.getSpeed() * tile.getRotationSpeedModifier(dir))
           .setRotationOffset(getRotationOffset(axis))
           .setRotationAxis(Direction.getFacingFromAxis(Direction.AxisDirection.POSITIVE, axis).getUnitVector());
    }
}
