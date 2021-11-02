package com.simibubi.create.content.contraptions.components.crank;

import com.jozufozu.flywheel.backend.instancing.IDynamicInstance;
import com.jozufozu.flywheel.backend.instancing.Instancer;
import com.jozufozu.flywheel.backend.material.MaterialManager;
import com.jozufozu.flywheel.core.PartialModel;
import com.jozufozu.flywheel.core.materials.model.ModelData;
import com.jozufozu.flywheel.util.transform.MatrixTransformStack;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.content.contraptions.base.SingleRotatingInstance;
import com.simibubi.create.foundation.utility.AnimationTickHolder;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public class HandCrankInstance extends SingleRotatingInstance implements IDynamicInstance {

    private final HandCrankTileEntity tile;
    private ModelData crank;
    private Direction facing;

    public HandCrankInstance(MaterialManager modelManager, HandCrankTileEntity tile) {
        super(modelManager, tile);
		this.tile = tile;

		Block block = blockState.getBlock();
		PartialModel renderedHandle = null;
		if (block instanceof HandCrankBlock)
			renderedHandle = ((HandCrankBlock) block).getRenderedHandle();
		if (renderedHandle == null)
			return;

		facing = blockState.getValue(BlockStateProperties.FACING);
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
        MatrixTransformStack.of(ms)
                     .translate(getInstancePosition())
                     .centre()
                     .rotate(Direction.get(Direction.AxisDirection.POSITIVE, axis), angle)
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
