package com.simibubi.create.content.contraptions.components.crafter;

import java.util.function.Supplier;

import com.jozufozu.flywheel.backend.instancing.Instancer;
import com.jozufozu.flywheel.backend.material.MaterialManager;
import com.jozufozu.flywheel.util.transform.MatrixTransformStack;
import com.jozufozu.flywheel.util.transform.TransformStack;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.content.contraptions.base.KineticTileEntity;
import com.simibubi.create.content.contraptions.base.RotatingData;
import com.simibubi.create.content.contraptions.base.SingleRotatingInstance;

import net.minecraft.core.Direction;

public class MechanicalCrafterInstance extends SingleRotatingInstance {

    public MechanicalCrafterInstance(MaterialManager modelManager, KineticTileEntity tile) {
        super(modelManager, tile);
    }

    @Override
    protected Instancer<RotatingData> getModel() {
        Direction facing = blockState.getValue(MechanicalCrafterBlock.HORIZONTAL_FACING);

		return getRotatingMaterial().getModel(AllBlockPartials.SHAFTLESS_COGWHEEL, blockState, facing, rotateToFace(facing));
    }

	private Supplier<PoseStack> rotateToFace(Direction facing) {
		return () -> {
			PoseStack stack = new PoseStack();
			TransformStack stacker = MatrixTransformStack.of(stack)
					.centre();

			if (facing.getAxis() == Direction.Axis.X) stacker.rotateZ(90);
			else if (facing.getAxis() == Direction.Axis.Z) stacker.rotateX(90);

			stacker.unCentre();
			return stack;
		};
	}
}
