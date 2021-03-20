package com.simibubi.create.content.contraptions.components.crank;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.content.contraptions.base.KineticTileEntity;
import com.simibubi.create.content.contraptions.base.SingleRotatingInstance;
import com.simibubi.create.foundation.render.backend.instancing.ITickableInstance;
import com.simibubi.create.foundation.render.backend.instancing.InstanceKey;
import com.simibubi.create.foundation.render.backend.instancing.InstancedModel;
import com.simibubi.create.foundation.render.backend.instancing.InstancedTileRenderer;
import com.simibubi.create.foundation.render.backend.instancing.impl.ModelData;
import com.simibubi.create.foundation.utility.AnimationTickHolder;
import com.simibubi.create.foundation.utility.MatrixStacker;
import net.minecraft.block.Block;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.Direction;

public class HandCrankInstance extends SingleRotatingInstance implements ITickableInstance {

    private InstanceKey<ModelData> crank;
    private Direction facing;

    public HandCrankInstance(InstancedTileRenderer<?> modelManager, KineticTileEntity tile) {
        super(modelManager, tile);
    }

    @Override
    protected void init() {
        super.init();

        Block block = lastState.getBlock();
        AllBlockPartials renderedHandle = null;
        if (block instanceof HandCrankBlock)
            renderedHandle = ((HandCrankBlock) block).getRenderedHandle();
        if (renderedHandle == null)
            return;

        facing = lastState.get(BlockStateProperties.FACING);
        InstancedModel<ModelData> model = renderedHandle.renderOnDirectionalSouthModel(modelManager, lastState, facing.getOpposite());
        crank = model.createInstance();

        updateLight();
    }

    @Override
    public void tick() {
        if (crank == null) return;

        HandCrankTileEntity crankTile = (HandCrankTileEntity) tile;

        Direction.Axis axis = facing.getAxis();
        float angle = (crankTile.independentAngle + AnimationTickHolder.getPartialTicks() * crankTile.chasingVelocity) / 360;

        MatrixStack ms = new MatrixStack();
        MatrixStacker.of(ms)
                     .translate(getFloatingPos())
                     .centre()
                     .rotate(Direction.getFacingFromAxis(Direction.AxisDirection.POSITIVE, axis), angle)
                     .unCentre();

        crank.getInstance().setTransformNoCopy(ms);
    }

    @Override
    public void remove() {
        super.remove();
        if (crank != null) crank.delete();
    }

    @Override
    public void updateLight() {
        super.updateLight();
        if (crank != null) relight(pos, crank.getInstance());
    }
}
