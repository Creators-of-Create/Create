package com.simibubi.create.content.kinetics.simpleRelays.encased;

import java.util.function.Consumer;

import org.jetbrains.annotations.Nullable;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.kinetics.base.IRotate;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityVisual;
import com.simibubi.create.content.kinetics.base.RotatingInstance;
import com.simibubi.create.content.kinetics.simpleRelays.BracketedKineticBlockEntityRenderer;
import com.simibubi.create.foundation.render.AllInstanceTypes;
import com.simibubi.create.foundation.utility.Iterate;

import dev.engine_room.flywheel.api.instance.Instance;
import dev.engine_room.flywheel.api.model.Model;
import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import dev.engine_room.flywheel.lib.model.Models;
import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import dev.engine_room.flywheel.lib.transform.TransformStack;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.AxisDirection;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public class EncasedCogVisual extends KineticBlockEntityVisual<KineticBlockEntity> {

	private final boolean large;

	protected final RotatingInstance rotatingModel;
	@Nullable
	protected final RotatingInstance rotatingTopShaft;
	@Nullable
	protected final RotatingInstance rotatingBottomShaft;

	public static EncasedCogVisual small(VisualizationContext modelManager, KineticBlockEntity blockEntity, float partialTick) {
		return new EncasedCogVisual(modelManager, blockEntity, false, partialTick);
	}

	public static EncasedCogVisual large(VisualizationContext modelManager, KineticBlockEntity blockEntity, float partialTick) {
		return new EncasedCogVisual(modelManager, blockEntity, true, partialTick);
	}

	public EncasedCogVisual(VisualizationContext modelManager, KineticBlockEntity blockEntity, boolean large, float partialTick) {
		super(modelManager, blockEntity, partialTick);
		this.large = large;

        var instancer = instancerProvider().instancer(AllInstanceTypes.ROTATING, getCogModel());
		rotatingModel = setup(instancer.createInstance());

		RotatingInstance rotatingTopShaft = null;
		RotatingInstance rotatingBottomShaft = null;

		Block block = blockState.getBlock();
		if (block instanceof IRotate def) {
			for (Direction d : Iterate.directionsInAxis(axis)) {
				if (!def.hasShaftTowards(blockEntity.getLevel(), blockEntity.getBlockPos(), blockState, d))
					continue;
				RotatingInstance data = setup(instancerProvider().instancer(AllInstanceTypes.ROTATING, Models.partial(AllPartialModels.SHAFT_HALF, d))
					.createInstance());
				if (large) {
					data.setRotationOffset(BracketedKineticBlockEntityRenderer.getShaftAngleOffset(axis, pos));
				}
				if (d.getAxisDirection() == AxisDirection.POSITIVE) {
					rotatingTopShaft = data;
				} else {
					rotatingBottomShaft = data;
				}
			}
		}

		this.rotatingTopShaft = rotatingTopShaft;
		this.rotatingBottomShaft = rotatingBottomShaft;
	}

	@Override
	public void update(float pt) {
		updateRotation(rotatingModel);
		if (rotatingTopShaft != null) updateRotation(rotatingTopShaft);
		if (rotatingBottomShaft != null) updateRotation(rotatingBottomShaft);
	}

	@Override
	public void updateLight(float partialTick) {
		relight(rotatingModel, rotatingTopShaft, rotatingBottomShaft);
	}

	@Override
	protected void _delete() {
		rotatingModel.delete();
		if (rotatingTopShaft != null) rotatingTopShaft.delete();
		if (rotatingBottomShaft != null) rotatingBottomShaft.delete();
	}

	protected Model getCogModel() {
		BlockState referenceState = blockEntity.getBlockState();
		Direction facing =
			Direction.fromAxisAndDirection(referenceState.getValue(BlockStateProperties.AXIS), AxisDirection.POSITIVE);
		PartialModel partial = large ? AllPartialModels.SHAFTLESS_LARGE_COGWHEEL : AllPartialModels.SHAFTLESS_COGWHEEL;

		return Models.partial(partial, facing, EncasedCogVisual::transformCog);
	}

	private static void transformCog(Direction dir, PoseStack stack) {
		TransformStack.of(stack)
				.center()
				.rotateToFace(dir)
				.rotate(Axis.XN.rotationDegrees(90))
				.uncenter();
	}

	@Override
	public void collectCrumblingInstances(Consumer<@Nullable Instance> consumer) {
		consumer.accept(rotatingModel);
		consumer.accept(rotatingTopShaft);
		consumer.accept(rotatingBottomShaft);
	}
}
