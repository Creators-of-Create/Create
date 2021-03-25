package com.simibubi.create.content.contraptions.components.crank;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.content.contraptions.base.KineticTileEntity;
import com.simibubi.create.content.contraptions.base.SingleRotatingInstance;
import com.simibubi.create.foundation.render.backend.instancing.*;
import com.simibubi.create.foundation.render.backend.instancing.impl.ModelData;
import com.simibubi.create.foundation.utility.AnimationTickHolder;
import com.simibubi.create.foundation.utility.MatrixStacker;
import net.minecraft.block.Block;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.Direction;

public class HandCrankInstance extends SingleRotatingInstance implements IDynamicInstance {

    private InstanceKey<ModelData> crank;
    private Direction facing;

    public HandCrankInstance(InstancedTileRenderer<?> modelManager, KineticTileEntity tile) {
        super(modelManager, tile);

        Block block = blockState.getBlock();
        AllBlockPartials renderedHandle = null;
        if (block instanceof HandCrankBlock)
            renderedHandle = ((HandCrankBlock) block).getRenderedHandle();
        if (renderedHandle == null)
            return;

        facing = blockState.get(BlockStateProperties.FACING);
        InstancedModel<ModelData> model = renderedHandle.renderOnDirectionalSouthModel(modelManager, blockState, facing.getOpposite());
        crank = model.createInstance();
    }

    @Override
    public void beginFrame() {
        if (crank == null) return;

        HandCrankTileEntity crankTile = (HandCrankTileEntity) tile;

        Direction.Axis axis = facing.getAxis();
        float angle = (crankTile.independentAngle + AnimationTickHolder.getPartialTicks() * crankTile.chasingVelocity) / 360;

        MatrixStack ms = new MatrixStack();
        MatrixStacker.of(ms)
                     .translate(getInstancePosition())
                     .centre()
                     .rotate(Direction.getFacingFromAxis(Direction.AxisDirection.POSITIVE, axis), angle)
                     .unCentre();

        crank.getInstance().setTransform(ms);
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
