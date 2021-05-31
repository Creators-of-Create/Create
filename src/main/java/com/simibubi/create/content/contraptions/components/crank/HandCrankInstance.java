package com.simibubi.create.content.contraptions.components.crank;

import com.jozufozu.flywheel.backend.instancing.IDynamicInstance;
import com.jozufozu.flywheel.backend.instancing.InstancedTileRenderer;
import com.jozufozu.flywheel.backend.instancing.Instancer;
import com.jozufozu.flywheel.core.PartialModel;
import com.jozufozu.flywheel.core.materials.ModelData;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.content.contraptions.base.SingleRotatingInstance;
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
		PartialModel renderedHandle = null;
		if (block instanceof HandCrankBlock)
			renderedHandle = ((HandCrankBlock) block).getRenderedHandle();
		if (renderedHandle == null)
			return;

		facing = blockState.get(BlockStateProperties.FACING);
		Direction opposite = facing.getOpposite();
		Instancer<ModelData> model = getTransformMaterial().getModel(renderedHandle, blockState, opposite);
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
