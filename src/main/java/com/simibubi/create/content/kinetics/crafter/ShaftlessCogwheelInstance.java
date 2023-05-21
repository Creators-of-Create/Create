package com.simibubi.create.content.kinetics.crafter;

import java.util.function.Supplier;

import com.jozufozu.flywheel.api.Instancer;
import com.jozufozu.flywheel.api.MaterialManager;
import com.jozufozu.flywheel.util.transform.TransformStack;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.content.kinetics.base.SingleRotatingInstance;
import com.simibubi.create.content.kinetics.base.flwdata.RotatingData;

import net.minecraft.core.Direction;

public class ShaftlessCogwheelInstance extends SingleRotatingInstance<KineticBlockEntity> {

    public ShaftlessCogwheelInstance(MaterialManager materialManager, KineticBlockEntity blockEntity) {
        super(materialManager, blockEntity);
    }

    @Override
    protected Instancer<RotatingData> getModel() {
        Direction facing = blockState.getValue(MechanicalCrafterBlock.HORIZONTAL_FACING);

		return getRotatingMaterial().getModel(AllPartialModels.SHAFTLESS_COGWHEEL, blockState, facing, rotateToFace(facing));
    }

	private Supplier<PoseStack> rotateToFace(Direction facing) {
		return () -> {
			PoseStack stack = new PoseStack();
			TransformStack stacker = TransformStack.cast(stack)
					.centre();

			if (facing.getAxis() == Direction.Axis.X) stacker.rotateZ(90);
			else if (facing.getAxis() == Direction.Axis.Z) stacker.rotateX(90);

			stacker.unCentre();
			return stack;
		};
	}
}
