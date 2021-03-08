package com.simibubi.create.content.contraptions.components.flywheel;

import java.util.function.Consumer;

import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.content.contraptions.base.IRotate;
import com.simibubi.create.content.contraptions.base.KineticTileInstance;
import com.simibubi.create.content.contraptions.base.RotatingData;
import com.simibubi.create.foundation.render.backend.instancing.InstanceKey;
import com.simibubi.create.foundation.render.backend.instancing.InstancedModel;
import com.simibubi.create.foundation.render.backend.instancing.InstancedTileRenderRegistry;
import com.simibubi.create.foundation.render.backend.instancing.InstancedTileRenderer;

import net.minecraft.block.BlockState;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.Rotation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;

public class FlyWheelInstance extends KineticTileInstance<FlywheelTileEntity> {
    public static void register(TileEntityType<? extends FlywheelTileEntity> type) {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
                InstancedTileRenderRegistry.instance.register(type, FlyWheelInstance::new));
    }

    protected Direction facing;

    protected InstanceKey<RotatingData> shaft;
//    protected InstanceKey<RotatingData> wheel;

    public FlyWheelInstance(InstancedTileRenderer<?> modelManager, FlywheelTileEntity tile) {
        super(modelManager, tile);
    }

    @Override
    protected void init() {
        facing = lastState.get(BlockStateProperties.HORIZONTAL_FACING);

        Direction.Axis axis = ((IRotate) lastState.getBlock()).getRotationAxis(lastState);
        shaft = setup(shaftModel().createInstance(), tile.getSpeed(), axis);
//        wheel = wheelModel().setupInstance(setup);
    }

    @Override
    protected void onUpdate() {
        Direction.Axis axis = ((IRotate) lastState.getBlock()).getRotationAxis(lastState);
        updateRotation(shaft, axis);
//        updateRotation(wheel, axis);
    }

    @Override
    public void updateLight() {
        relight(shaft);
//        wheel.modifyInstance(this::relight);
    }

    @Override
    public void remove() {
        shaft.delete();
//        wheel.delete();
//        wheel = null;
    }

    protected InstancedModel<RotatingData> shaftModel() {
        return AllBlockPartials.SHAFT_HALF.renderOnDirectionalSouthRotating(modelManager, lastState, facing.getOpposite());
    }

    protected InstancedModel<RotatingData> wheelModel() {
        BlockState rotate = lastState.rotate(Rotation.CLOCKWISE_90);
        return AllBlockPartials.FLYWHEEL.renderOnDirectionalSouthRotating(modelManager, rotate, rotate.get(BlockStateProperties.HORIZONTAL_FACING));
    }
}
