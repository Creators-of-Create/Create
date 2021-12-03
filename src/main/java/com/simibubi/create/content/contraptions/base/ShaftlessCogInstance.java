package com.simibubi.create.content.contraptions.base;

import com.jozufozu.flywheel.backend.instancing.Instancer;
import com.jozufozu.flywheel.backend.material.MaterialManager;
import com.jozufozu.flywheel.core.PartialModel;
import com.jozufozu.flywheel.util.transform.TransformStack;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.content.contraptions.base.flwdata.RotatingData;

import net.minecraft.core.Direction;
import net.minecraft.core.Direction.AxisDirection;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public class ShaftlessCogInstance extends SingleRotatingInstance {

	private boolean large;

	public static ShaftlessCogInstance small(MaterialManager modelManager, KineticTileEntity tile) {
		return new ShaftlessCogInstance(modelManager, tile, false);
	}

	public static ShaftlessCogInstance large(MaterialManager modelManager, KineticTileEntity tile) {
		return new ShaftlessCogInstance(modelManager, tile, true);
	}

	public ShaftlessCogInstance(MaterialManager modelManager, KineticTileEntity tile, boolean large) {
		super(modelManager, tile);
		this.large = large;
	}

	@Override
	protected Instancer<RotatingData> getModel() {
		BlockState referenceState = tile.getBlockState();
		Direction facing =
			Direction.fromAxisAndDirection(referenceState.getValue(BlockStateProperties.AXIS), AxisDirection.POSITIVE);
		PartialModel partial = large ? AllBlockPartials.SHAFTLESS_LARGE_COGWHEEL : AllBlockPartials.SHAFTLESS_COGWHEEL;

		return getRotatingMaterial().getModel(partial, referenceState, facing, () -> {
			PoseStack poseStack = new PoseStack();
			TransformStack.cast(poseStack).centre()
				.rotateToFace(facing)
				.multiply(Vector3f.XN.rotationDegrees(90))
				.unCentre();
			return poseStack;
		});
	}

}
