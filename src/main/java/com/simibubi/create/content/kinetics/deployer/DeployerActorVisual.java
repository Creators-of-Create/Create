package com.simibubi.create.content.kinetics.deployer;

import static com.simibubi.create.content.kinetics.base.DirectionalAxisKineticBlock.AXIS_ALONG_FIRST_COORDINATE;
import static com.simibubi.create.content.kinetics.base.DirectionalKineticBlock.FACING;

import org.joml.Matrix4f;
import org.joml.Matrix4fc;

import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.contraptions.behaviour.MovementContext;
import com.simibubi.create.content.contraptions.render.ActorVisual;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityVisual;
import com.simibubi.create.content.kinetics.base.RotatingInstance;
import com.simibubi.create.foundation.render.AllInstanceTypes;
import com.simibubi.create.foundation.virtualWorld.VirtualRenderWorld;

import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import dev.engine_room.flywheel.lib.instance.InstanceTypes;
import dev.engine_room.flywheel.lib.instance.TransformedInstance;
import dev.engine_room.flywheel.lib.model.Models;
import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import net.createmod.catnip.api.client.animation.AnimationTickHolder;
import net.createmod.catnip.api.nbt.NBTHelper;
import net.createmod.catnip.api.math.VecHelper;
import net.createmod.catnip.api.math.AngleHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class DeployerActorVisual extends ActorVisual {

	Direction facing;
    boolean stationaryTimer;

    TransformedInstance pole;
    TransformedInstance hand;
    RotatingInstance shaft;

	Matrix4fc baseHandTransform;
	Matrix4fc basePoleTransform;

	public DeployerActorVisual(VisualizationContext visualizationContext, VirtualRenderWorld simulationWorld, MovementContext context) {
        super(visualizationContext, simulationWorld, context);
        BlockState state = context.state;
        DeployerBlockEntity.Mode mode = NBTHelper.readEnum(context.blockEntityData, "Mode", DeployerBlockEntity.Mode.class);
        PartialModel handPose = DeployerRenderer.getHandPose(mode);

        stationaryTimer = context.data.contains("StationaryTimer");
        facing = state.getValue(FACING);

        boolean rotatePole = state.getValue(AXIS_ALONG_FIRST_COORDINATE) ^ facing.getAxis() == Direction.Axis.Z;
		float yRot = AngleHelper.horizontalAngle(facing);
		float xRot = facing == Direction.UP ? 270 : facing == Direction.DOWN ? 90 : 0;
		float zRot = rotatePole ? 90 : 0;

		pole = instancerProvider.instancer(InstanceTypes.TRANSFORMED, Models.partial(AllPartialModels.DEPLOYER_POLE)).createInstance();
        hand = instancerProvider.instancer(InstanceTypes.TRANSFORMED, Models.partial(handPose)).createInstance();

        Direction.Axis axis = KineticBlockEntityVisual.rotationAxis(state);
        shaft = instancerProvider.instancer(AllInstanceTypes.ROTATING, Models.partial(AllPartialModels.SHAFT))
			.createInstance()
			.rotateToFace(axis);

        int blockLight = localBlockLight();

        shaft.setRotationAxis(axis)
			.setRotationOffset(KineticBlockEntityVisual.rotationOffset(state, axis, context.localPos))
			.setPosition(context.localPos)
			.light(blockLight, 0)
			.setChanged();

		pole.translate(context.localPos)
			.center()
			.rotate(yRot * Mth.DEG_TO_RAD, Direction.UP)
			.rotate(xRot * Mth.DEG_TO_RAD, Direction.EAST)
			.rotate(zRot * Mth.DEG_TO_RAD, Direction.SOUTH)
			.uncenter()
			.light(blockLight, 0)
			.setChanged();

		basePoleTransform = new Matrix4f(pole.pose);

		hand.translate(context.localPos)
			.center()
			.rotate(yRot * Mth.DEG_TO_RAD, Direction.UP)
			.rotate(xRot * Mth.DEG_TO_RAD, Direction.EAST)
			.uncenter()
			.light(blockLight, 0)
			.setChanged();

		baseHandTransform = new Matrix4f(hand.pose);
	}

    @Override
    public void beginFrame() {
		float distance = deploymentDistance();

		pole.setTransform(basePoleTransform)
			.translateZ(distance)
			.setChanged();

		hand.setTransform(baseHandTransform)
			.translateZ(distance)
			.setChanged();
	}

	private float deploymentDistance() {
		double factor;
		if (context.disabled) {
			factor = 0;
		} else if (context.contraption.stalled || context.position == null || context.data.contains("StationaryTimer")) {
			factor = Mth.sin(AnimationTickHolder.getRenderTime() * .5f) * .25f + .25f;
		} else {
			Vec3 center = VecHelper.getCenterOf(BlockPos.containing(context.position));
			double distance = context.position.distanceTo(center);
			double nextDistance = context.position.add(context.motion)
												  .distanceTo(center);
			factor = .5f - Mth.clamp(Mth.lerp(AnimationTickHolder.getPartialTicks(), distance, nextDistance), 0, 1);
		}
		return (float) factor;
	}

	@Override
	protected void _delete() {
		pole.delete();
		hand.delete();
		shaft.delete();
	}
}
