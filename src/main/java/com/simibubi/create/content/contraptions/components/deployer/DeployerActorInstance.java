package com.simibubi.create.content.contraptions.components.deployer;

import static com.simibubi.create.content.contraptions.base.DirectionalAxisKineticBlock.AXIS_ALONG_FIRST_COORDINATE;
import static com.simibubi.create.content.contraptions.base.DirectionalKineticBlock.FACING;

import com.jozufozu.flywheel.api.Material;
import com.jozufozu.flywheel.api.MaterialManager;
import com.jozufozu.flywheel.core.Materials;
import com.jozufozu.flywheel.core.PartialModel;
import com.jozufozu.flywheel.core.materials.model.ModelData;
import com.jozufozu.flywheel.core.virtual.VirtualRenderWorld;
import com.jozufozu.flywheel.util.transform.TransformStack;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.content.contraptions.base.IRotate;
import com.simibubi.create.content.contraptions.base.KineticTileInstance;
import com.simibubi.create.content.contraptions.base.flwdata.RotatingData;
import com.simibubi.create.content.contraptions.components.structureMovement.MovementContext;
import com.simibubi.create.content.contraptions.components.structureMovement.render.ActorInstance;
import com.simibubi.create.foundation.render.AllMaterialSpecs;
import com.simibubi.create.foundation.utility.AngleHelper;
import com.simibubi.create.foundation.utility.AnimationTickHolder;
import com.simibubi.create.foundation.utility.NBTHelper;
import com.simibubi.create.foundation.utility.VecHelper;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class DeployerActorInstance extends ActorInstance {

	private final PoseStack stack = new PoseStack();
	Direction facing;
    boolean stationaryTimer;

    float yRot;
    float xRot;
    float zRot;

    ModelData pole;
    ModelData hand;
    RotatingData shaft;

	public DeployerActorInstance(MaterialManager materialManager, VirtualRenderWorld simulationWorld, MovementContext context) {
        super(materialManager, simulationWorld, context);

		Material<ModelData> mat = materialManager.defaultSolid()
				.material(Materials.TRANSFORMED);

        BlockState state = context.state;
        DeployerTileEntity.Mode mode = NBTHelper.readEnum(context.tileData, "Mode", DeployerTileEntity.Mode.class);
        PartialModel handPose = DeployerRenderer.getHandPose(mode);

        stationaryTimer = context.data.contains("StationaryTimer");
        facing = state.getValue(FACING);

        boolean rotatePole = state.getValue(AXIS_ALONG_FIRST_COORDINATE) ^ facing.getAxis() == Direction.Axis.Z;
        yRot = AngleHelper.horizontalAngle(facing);
        xRot = facing == Direction.UP ? 270 : facing == Direction.DOWN ? 90 : 0;
        zRot = rotatePole ? 90 : 0;

        pole = mat.getModel(AllBlockPartials.DEPLOYER_POLE, state).createInstance();
        hand = mat.getModel(handPose, state).createInstance();

        Direction.Axis axis = ((IRotate) state.getBlock()).getRotationAxis(state);
        shaft = materialManager.defaultSolid()
                .material(AllMaterialSpecs.ROTATING)
				.getModel(KineticTileInstance.shaft(axis))
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
        if (context.contraption.stalled || context.position == null || context.data.contains("StationaryTimer")) {
            factor = Mth.sin(AnimationTickHolder.getRenderTime() * .5f) * .25f + .25f;
        } else {
        	Vec3 center = VecHelper.getCenterOf(new BlockPos(context.position));
            double distance = context.position.distanceTo(center);
            double nextDistance = context.position.add(context.motion)
                                                  .distanceTo(center);
            factor = .5f - Mth.clamp(Mth.lerp(AnimationTickHolder.getPartialTicks(), distance, nextDistance), 0, 1);
        }

        Vec3 offset = Vec3.atLowerCornerOf(facing.getNormal()).scale(factor);

        TransformStack tstack = TransformStack.cast(stack);
        stack.setIdentity();
        tstack.translate(context.localPos)
				.translate(offset);

        transformModel(stack, pole, hand, yRot, xRot, zRot);
    }

    static void transformModel(PoseStack stack, ModelData pole, ModelData hand, float yRot, float xRot, float zRot) {
        TransformStack tstack = TransformStack.cast(stack);

        tstack.centre();
        tstack.rotate(Direction.UP, (float) ((yRot) / 180 * Math.PI));
        tstack.rotate(Direction.EAST, (float) ((xRot) / 180 * Math.PI));

        stack.pushPose();
        tstack.rotate(Direction.SOUTH, (float) ((zRot) / 180 * Math.PI));
        tstack.unCentre();
        pole.setTransform(stack);
        stack.popPose();

        tstack.unCentre();

        hand.setTransform(stack);
    }
}
