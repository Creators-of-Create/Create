package com.simibubi.create.content.kinetics.crafter;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.content.kinetics.base.SingleRotatingVisual;

import dev.engine_room.flywheel.api.model.Model;
import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import dev.engine_room.flywheel.lib.model.Models;
import dev.engine_room.flywheel.lib.transform.TransformStack;
import net.minecraft.core.Direction;

public class ShaftlessCogwheelVisual extends SingleRotatingVisual<KineticBlockEntity> {

    public ShaftlessCogwheelVisual(VisualizationContext context, KineticBlockEntity blockEntity, float partialTick) {
        super(context, blockEntity, partialTick);
    }

	@Override
	protected Model model() {
		Direction facing = blockState.getValue(MechanicalCrafterBlock.HORIZONTAL_FACING);

		return Models.partial(AllPartialModels.SHAFTLESS_COGWHEEL, facing, ShaftlessCogwheelVisual::rotateToFace);
	}

	private static void rotateToFace(Direction facing, PoseStack stack) {
		var stacker = TransformStack.of(stack)
				.center();

		if (facing.getAxis() == Direction.Axis.X) stacker.rotateZDegrees(90);
		else if (facing.getAxis() == Direction.Axis.Z) stacker.rotateXDegrees(90);

		stacker.uncenter();
	}
}
