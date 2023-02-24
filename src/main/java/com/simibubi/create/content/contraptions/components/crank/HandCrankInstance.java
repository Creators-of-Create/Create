package com.simibubi.create.content.contraptions.components.crank;

import com.jozufozu.flywheel.api.Instancer;
import com.jozufozu.flywheel.api.MaterialManager;
import com.jozufozu.flywheel.api.instance.DynamicInstance;
import com.jozufozu.flywheel.core.PartialModel;
import com.jozufozu.flywheel.core.materials.model.ModelData;
import com.simibubi.create.content.contraptions.base.SingleRotatingInstance;
import com.simibubi.create.foundation.utility.AnimationTickHolder;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public class HandCrankInstance extends SingleRotatingInstance<HandCrankBlockEntity> implements DynamicInstance {

    private ModelData crank;
    private Direction facing;

    public HandCrankInstance(MaterialManager modelManager, HandCrankBlockEntity blockEntity) {
        super(modelManager, blockEntity);

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
        float angle = (blockEntity.independentAngle + AnimationTickHolder.getPartialTicks() * blockEntity.chasingVelocity) / 360;

        crank.loadIdentity()
                     .translate(getInstancePosition())
                     .centre()
                     .rotate(Direction.get(Direction.AxisDirection.POSITIVE, axis), angle)
                     .unCentre();
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
