package com.simibubi.create.content.kinetics.deployer;

import static com.simibubi.create.content.kinetics.base.DirectionalAxisKineticBlock.AXIS_ALONG_FIRST_COORDINATE;
import static com.simibubi.create.content.kinetics.base.DirectionalKineticBlock.FACING;

import com.jozufozu.flywheel.api.visualization.VisualizationContext;
import com.jozufozu.flywheel.lib.instance.InstanceTypes;
import com.jozufozu.flywheel.lib.instance.TransformedInstance;
import com.jozufozu.flywheel.lib.model.Models;
import com.jozufozu.flywheel.lib.model.baked.PartialModel;
import com.jozufozu.flywheel.lib.transform.TransformStack;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.contraptions.behaviour.MovementContext;
import com.simibubi.create.content.contraptions.render.ActorVisual;
import com.simibubi.create.content.kinetics.base.IRotate;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityVisual;
import com.simibubi.create.content.kinetics.base.RotatingInstance;
import com.simibubi.create.foundation.render.AllInstanceTypes;
import com.simibubi.create.foundation.render.VirtualRenderHelper;
import com.simibubi.create.foundation.utility.AngleHelper;
import com.simibubi.create.foundation.utility.AnimationTickHolder;
import com.simibubi.create.foundation.utility.NBTHelper;
import com.simibubi.create.foundation.utility.VecHelper;
import com.simibubi.create.foundation.virtualWorld.VirtualRenderWorld;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class DeployerActorVisual extends ActorVisual {

	private final PoseStack stack = new PoseStack();
	Direction facing;
    boolean stationaryTimer;

    float yRot;
    float xRot;
    float zRot;

    TransformedInstance pole;
    TransformedInstance hand;
    RotatingInstance shaft;

	public DeployerActorVisual(VisualizationContext visualizationContext, VirtualRenderWorld simulationWorld, MovementContext context) {
        super(visualizationContext, simulationWorld, context);
        BlockState state = context.state;
        DeployerBlockEntity.Mode mode = NBTHelper.readEnum(context.blockEntityData, "Mode", DeployerBlockEntity.Mode.class);
        PartialModel handPose = DeployerRenderer.getHandPose(mode);

        stationaryTimer = context.data.contains("StationaryTimer");
        facing = state.getValue(FACING);

        boolean rotatePole = state.getValue(AXIS_ALONG_FIRST_COORDINATE) ^ facing.getAxis() == Direction.Axis.Z;
        yRot = AngleHelper.horizontalAngle(facing);
        xRot = facing == Direction.UP ? 270 : facing == Direction.DOWN ? 90 : 0;
        zRot = rotatePole ? 90 : 0;

		pole = instancerProvider.instancer(InstanceTypes.TRANSFORMED, Models.partial(AllPartialModels.DEPLOYER_POLE)).createInstance();
        hand = instancerProvider.instancer(InstanceTypes.TRANSFORMED, Models.partial(handPose)).createInstance();

        Direction.Axis axis = ((IRotate) state.getBlock()).getRotationAxis(state);
        shaft = instancerProvider.instancer(AllInstanceTypes.ROTATING, VirtualRenderHelper.blockModel(KineticBlockEntityVisual.shaft(axis)))
				.createInstance();

        int blockLight = localBlockLight();

        shaft.setRotationAxis(axis)
                .setPosition(context.localPos)
                .setBlockLight(blockLight);

        pole.setBlockLight(blockLight);
        hand.setBlockLight(blockLight);
    }

    @Override
    public void beginFrame() {
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

        Vec3 offset = Vec3.atLowerCornerOf(facing.getNormal()).scale(factor);

        var tstack = TransformStack.of(stack);
        stack.setIdentity();
        tstack.translate(context.localPos)
				.translate(offset);

        transformModel(stack, pole, hand, yRot, xRot, zRot);
    }

    static void transformModel(PoseStack stack, TransformedInstance pole, TransformedInstance hand, float yRot, float xRot, float zRot) {
        var tstack = TransformStack.of(stack);

        tstack.center();
        tstack.rotate((float) ((yRot) / 180 * Math.PI), Direction.UP);
        tstack.rotate((float) ((xRot) / 180 * Math.PI), Direction.EAST);

        stack.pushPose();
        tstack.rotate((float) ((zRot) / 180 * Math.PI), Direction.SOUTH);
        tstack.uncenter();
        pole.setTransform(stack);
        stack.popPose();

        tstack.uncenter();

        hand.setTransform(stack);
    }
}
