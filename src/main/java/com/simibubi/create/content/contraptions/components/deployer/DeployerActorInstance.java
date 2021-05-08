package com.simibubi.create.content.contraptions.components.deployer;

import static com.simibubi.create.content.contraptions.base.DirectionalAxisKineticBlock.AXIS_ALONG_FIRST_COORDINATE;
import static com.simibubi.create.content.contraptions.base.DirectionalKineticBlock.FACING;

import com.jozufozu.flywheel.backend.core.PartialModel;
import com.jozufozu.flywheel.backend.core.materials.ModelData;
import com.jozufozu.flywheel.backend.instancing.InstancedModel;
import com.jozufozu.flywheel.backend.instancing.RenderMaterial;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.content.contraptions.base.IRotate;
import com.simibubi.create.content.contraptions.base.KineticTileInstance;
import com.simibubi.create.content.contraptions.base.RotatingData;
import com.simibubi.create.content.contraptions.components.structureMovement.MovementContext;
import com.simibubi.create.content.contraptions.components.structureMovement.render.ActorInstance;
import com.simibubi.create.content.contraptions.components.structureMovement.render.ContraptionKineticRenderer;
import com.simibubi.create.content.contraptions.components.structureMovement.render.ContraptionProgram;
import com.simibubi.create.foundation.render.AllMaterialSpecs;
import com.simibubi.create.foundation.utility.AngleHelper;
import com.simibubi.create.foundation.utility.AnimationTickHolder;
import com.simibubi.create.foundation.utility.MatrixStacker;
import com.simibubi.create.foundation.utility.NBTHelper;
import com.simibubi.create.foundation.utility.VecHelper;

import net.minecraft.block.BlockState;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;

public class DeployerActorInstance extends ActorInstance {

    Direction facing;
    boolean stationaryTimer;

    float yRot;
    float zRot;
    float zRotPole;

    ModelData pole;
    ModelData hand;
    RotatingData shaft;

    public DeployerActorInstance(ContraptionKineticRenderer modelManager, MovementContext context) {
        super(modelManager, context);

        RenderMaterial<ContraptionProgram, InstancedModel<ModelData>> mat = modelManager.getTransformMaterial();

        BlockState state = context.state;
        DeployerTileEntity.Mode mode = NBTHelper.readEnum(context.tileData, "Mode", DeployerTileEntity.Mode.class);
        PartialModel handPose = DeployerRenderer.getHandPose(mode);

        stationaryTimer = context.data.contains("StationaryTimer");
        facing = state.get(FACING);

        boolean rotatePole = state.get(AXIS_ALONG_FIRST_COORDINATE) ^ facing.getAxis() == Direction.Axis.Z;
        yRot = AngleHelper.horizontalAngle(facing);
        zRot = facing == Direction.UP ? 270 : facing == Direction.DOWN ? 90 : 0;
        zRotPole = rotatePole ? 90 : 0;

        pole = mat.getModel(AllBlockPartials.DEPLOYER_POLE, state).createInstance();
        hand = mat.getModel(handPose, state).createInstance();

        Direction.Axis axis = ((IRotate) state.getBlock()).getRotationAxis(state);
		shaft = modelManager.getMaterial(AllMaterialSpecs.ROTATING)
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
            factor = MathHelper.sin(AnimationTickHolder.getRenderTime() * .5f) * .25f + .25f;
        } else {
            Vector3d center = VecHelper.getCenterOf(new BlockPos(context.position));
            double distance = context.position.distanceTo(center);
            double nextDistance = context.position.add(context.motion)
                                                  .distanceTo(center);
            factor = .5f - MathHelper.clamp(MathHelper.lerp(AnimationTickHolder.getPartialTicks(), distance, nextDistance), 0, 1);
        }

        Vector3d offset = Vector3d.of(facing.getDirectionVec()).scale(factor);

        MatrixStack ms = new MatrixStack();
        MatrixStacker msr = MatrixStacker.of(ms);

        msr.translate(context.localPos)
           .translate(offset);

        transformModel(msr, pole, hand, yRot, zRot, zRotPole);
    }

    static void transformModel(MatrixStacker msr, ModelData pole, ModelData hand, float yRot, float zRot, float zRotPole) {

        msr.centre();
        msr.rotate(Direction.SOUTH, (float) ((zRot) / 180 * Math.PI));
        msr.rotate(Direction.UP, (float) ((yRot) / 180 * Math.PI));

        msr.push();
        msr.rotate(Direction.SOUTH, (float) ((zRotPole) / 180 * Math.PI));
        msr.unCentre();
        pole.setTransform(msr.unwrap());
        msr.pop();

        msr.unCentre();

        hand.setTransform(msr.unwrap());
    }
}
