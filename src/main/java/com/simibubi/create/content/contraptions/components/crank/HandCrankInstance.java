package com.simibubi.create.content.contraptions.components.crank;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.content.contraptions.base.SingleRotatingInstance;
import com.simibubi.create.foundation.render.backend.MaterialTypes;
import com.simibubi.create.foundation.render.backend.instancing.*;
import com.simibubi.create.foundation.render.backend.core.ModelData;
import com.simibubi.create.foundation.utility.AnimationTickHolder;
import com.simibubi.create.foundation.utility.MatrixStacker;
import net.minecraft.block.Block;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.Direction;

public class HandCrankInstance extends SingleRotatingInstance implements IDynamicInstance {

    private final HandCrankTileEntity tile;
    private ModelData crank;
    private Direction facing;

    public HandCrankInstance(InstancedTileRenderer<?> modelManager, HandCrankTileEntity tile) {
        super(modelManager, tile);
        this.tile = tile;

        Block block = blockState.getBlock();
        AllBlockPartials renderedHandle = null;
        if (block instanceof HandCrankBlock)
            renderedHandle = ((HandCrankBlock) block).getRenderedHandle();
        if (renderedHandle == null)
            return;

        facing = blockState.get(BlockStateProperties.FACING);
		InstancedModel<ModelData> model = renderedHandle.getModel(getTransformMaterial(), blockState, facing.getOpposite());
        crank = model.createInstance();

        rotateCrank();
    }

    @Override
    public void beginFrame() {
        if (crank == null) return;

        rotateCrank();
    }

    private void rotateCrank() {
        Direction.Axis axis = facing.getAxis();
        float angle = (tile.independentAngle + AnimationTickHolder.getPartialTicks() * tile.chasingVelocity) / 360;

        MatrixStack ms = new MatrixStack();
        MatrixStacker.of(ms)
                     .translate(getInstancePosition())
                     .centre()
                     .rotate(Direction.getFacingFromAxis(Direction.AxisDirection.POSITIVE, axis), angle)
                     .unCentre();

        crank.setTransform(ms);
    }

    @Override
    public void remove() {
        super.remove();
        if (crank != null) crank.delete();
    }

    @Override
    public void updateLight() {
        super.updateLight();
        if (crank != null) relight(pos, crank);
    }
}
