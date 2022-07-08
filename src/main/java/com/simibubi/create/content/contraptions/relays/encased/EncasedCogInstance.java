package com.simibubi.create.content.contraptions.relays.encased;

import java.util.Optional;

import com.jozufozu.flywheel.api.InstanceData;
import com.jozufozu.flywheel.api.Instancer;
import com.jozufozu.flywheel.api.MaterialManager;
import com.jozufozu.flywheel.core.PartialModel;
import com.jozufozu.flywheel.util.transform.TransformStack;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.content.contraptions.base.IRotate;
import com.simibubi.create.content.contraptions.base.KineticTileEntity;
import com.simibubi.create.content.contraptions.base.KineticTileInstance;
import com.simibubi.create.content.contraptions.base.flwdata.RotatingData;
import com.simibubi.create.foundation.utility.Iterate;

import net.minecraft.core.Direction;
import net.minecraft.core.Direction.AxisDirection;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public class EncasedCogInstance extends KineticTileInstance<KineticTileEntity> {

	private boolean large;

	protected RotatingData rotatingModel;
	protected Optional<RotatingData> rotatingTopShaft;
	protected Optional<RotatingData> rotatingBottomShaft;

	public static EncasedCogInstance small(MaterialManager modelManager, KineticTileEntity tile) {
		return new EncasedCogInstance(modelManager, tile, false);
	}

	public static EncasedCogInstance large(MaterialManager modelManager, KineticTileEntity tile) {
		return new EncasedCogInstance(modelManager, tile, true);
	}

	public EncasedCogInstance(MaterialManager modelManager, KineticTileEntity tile, boolean large) {
		super(modelManager, tile);
		this.large = large;
	}

	@Override
	public void init() {
		rotatingModel = setup(getCogModel().createInstance());

		Block block = blockState.getBlock();
		if (!(block instanceof IRotate))
			return;

		IRotate def = (IRotate) block;
		rotatingTopShaft = Optional.empty();
		rotatingBottomShaft = Optional.empty();

		for (Direction d : Iterate.directionsInAxis(axis)) {
			if (!def.hasShaftTowards(blockEntity.getLevel(), blockEntity.getBlockPos(), blockState, d))
				continue;
			RotatingData data = setup(getRotatingMaterial().getModel(AllBlockPartials.SHAFT_HALF, blockState, d)
				.createInstance());
			if (d.getAxisDirection() == AxisDirection.POSITIVE)
				rotatingTopShaft = Optional.of(data);
			else
				rotatingBottomShaft = Optional.of(data);
		}
	}

	@Override
	public void update() {
		updateRotation(rotatingModel);
		rotatingTopShaft.ifPresent(this::updateRotation);
		rotatingBottomShaft.ifPresent(this::updateRotation);
	}

	@Override
	public void updateLight() {
		relight(pos, rotatingModel);
		rotatingTopShaft.ifPresent(d -> relight(pos, d));
		rotatingBottomShaft.ifPresent(d -> relight(pos, d));
	}

	@Override
	public void remove() {
		rotatingModel.delete();
		rotatingTopShaft.ifPresent(InstanceData::delete);
		rotatingBottomShaft.ifPresent(InstanceData::delete);
	}

	protected Instancer<RotatingData> getCogModel() {
		BlockState referenceState = blockEntity.getBlockState();
		Direction facing =
			Direction.fromAxisAndDirection(referenceState.getValue(BlockStateProperties.AXIS), AxisDirection.POSITIVE);
		PartialModel partial = large ? AllBlockPartials.SHAFTLESS_LARGE_COGWHEEL : AllBlockPartials.SHAFTLESS_COGWHEEL;

		return getRotatingMaterial().getModel(partial, referenceState, facing, () -> {
			PoseStack poseStack = new PoseStack();
			TransformStack.cast(poseStack)
				.centre()
				.rotateToFace(facing)
				.multiply(Vector3f.XN.rotationDegrees(90))
				.unCentre();
			return poseStack;
		});
	}

}
