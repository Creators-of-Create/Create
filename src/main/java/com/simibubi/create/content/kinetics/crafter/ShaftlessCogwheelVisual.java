package com.simibubi.create.content.kinetics.crafter;

import com.jozufozu.flywheel.api.model.Model;
import com.jozufozu.flywheel.api.visualization.VisualizationContext;
import com.jozufozu.flywheel.lib.model.Models;
import com.jozufozu.flywheel.lib.transform.TransformStack;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.content.kinetics.base.SingleRotatingVisual;

import net.minecraft.core.Direction;

public class ShaftlessCogwheelVisual extends SingleRotatingVisual<KineticBlockEntity> {

    public ShaftlessCogwheelVisual(VisualizationContext context, KineticBlockEntity blockEntity) {
        super(context, blockEntity);
    }

	@Override
	protected Model model() {
		Direction facing = blockState.getValue(MechanicalCrafterBlock.HORIZONTAL_FACING);

		return Models.partial(AllPartialModels.SHAFTLESS_COGWHEEL, facing, ShaftlessCogwheelVisual::rotateToFace);
	}

	private static void rotateToFace(Direction facing, PoseStack stack) {
		var stacker = TransformStack.of(stack)
				.center();

		if (facing.getAxis() == Direction.Axis.X) stacker.rotateZ(90);
		else if (facing.getAxis() == Direction.Axis.Z) stacker.rotateX(90);

		stacker.uncenter();
	}
}
