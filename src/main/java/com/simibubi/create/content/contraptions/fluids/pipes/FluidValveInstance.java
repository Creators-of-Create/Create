package com.simibubi.create.content.contraptions.fluids.pipes;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.content.contraptions.base.KineticTileEntity;
import com.simibubi.create.content.contraptions.base.KineticTileEntityRenderer;
import com.simibubi.create.content.contraptions.relays.encased.ShaftInstance;
import com.simibubi.create.foundation.render.backend.instancing.ITickableInstance;
import com.simibubi.create.foundation.render.backend.instancing.InstanceKey;
import com.simibubi.create.foundation.render.backend.instancing.InstancedTileRenderer;
import com.simibubi.create.foundation.render.backend.instancing.impl.ModelData;
import com.simibubi.create.foundation.utility.AngleHelper;
import com.simibubi.create.foundation.utility.AnimationTickHolder;
import com.simibubi.create.foundation.utility.MatrixStacker;
import net.minecraft.util.Direction;
import net.minecraft.util.math.MathHelper;

public class FluidValveInstance extends ShaftInstance implements ITickableInstance {

    protected InstanceKey<ModelData> pointer;

    protected double xRot;
    protected double yRot;
    protected int pointerRotationOffset;

    public FluidValveInstance(InstancedTileRenderer<?> dispatcher, KineticTileEntity tile) {
        super(dispatcher, tile);
    }

    @Override
    protected void init() {
        super.init();

        Direction facing = lastState.get(FluidValveBlock.FACING);

        yRot = AngleHelper.horizontalAngle(facing);
        xRot = facing == Direction.UP ? 0 : facing == Direction.DOWN ? 180 : 90;

        Direction.Axis pipeAxis = FluidValveBlock.getPipeAxis(lastState);
        Direction.Axis shaftAxis = KineticTileEntityRenderer.getRotationAxisOf(tile);

        pointerRotationOffset = 0;
        if (pipeAxis.isHorizontal() && shaftAxis == Direction.Axis.Z || pipeAxis.isVertical())
            pointerRotationOffset = 90;

        pointer = modelManager.basicMaterial().getModel(AllBlockPartials.FLUID_VALVE_POINTER, lastState).createInstance();

        updateLight();
        transformPointer((FluidValveTileEntity) tile);
    }

    @Override
    public void tick() {

        FluidValveTileEntity valve = (FluidValveTileEntity) tile;

        if (valve.pointer.settled()) return;

        transformPointer(valve);
    }

    private void transformPointer(FluidValveTileEntity valve) {
        float pointerRotation = MathHelper.lerp(valve.pointer.getValue(AnimationTickHolder.getPartialTicks()), 0, -90);

        MatrixStack ms = new MatrixStack();
        MatrixStacker.of(ms)
                     .translate(getFloatingPos())
                     .centre()
                     .rotateY(yRot)
                     .rotateX(xRot)
                     .rotateY(pointerRotationOffset + pointerRotation)
                     .unCentre();

        pointer.getInstance().setTransform(ms);
    }

    @Override
    public void updateLight() {
        super.updateLight();
        relight(pos, pointer.getInstance());
    }

    @Override
    public void remove() {
        super.remove();
        pointer.delete();
    }
}
