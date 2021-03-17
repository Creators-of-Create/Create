package com.simibubi.create.content.contraptions.components.deployer;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.content.contraptions.base.*;
import com.simibubi.create.content.contraptions.components.structureMovement.MovementContext;
import com.simibubi.create.content.contraptions.components.structureMovement.render.ActorInstance;
import com.simibubi.create.content.contraptions.components.structureMovement.render.ContraptionKineticRenderer;
import com.simibubi.create.content.contraptions.components.structureMovement.render.ContraptionProgram;
import com.simibubi.create.foundation.render.Compartment;
import com.simibubi.create.foundation.render.backend.instancing.InstanceKey;
import com.simibubi.create.foundation.render.backend.instancing.InstancedModel;
import com.simibubi.create.foundation.render.backend.instancing.RenderMaterial;
import com.simibubi.create.foundation.render.backend.instancing.impl.ModelData;
import com.simibubi.create.foundation.utility.*;
import net.minecraft.block.BlockState;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import static com.simibubi.create.content.contraptions.base.DirectionalAxisKineticBlock.AXIS_ALONG_FIRST_COORDINATE;
import static com.simibubi.create.content.contraptions.base.DirectionalKineticBlock.FACING;

public class DeployerActorInstance extends ActorInstance {

    Direction facing;
    boolean stationaryTimer;

    float yRot;
    float zRot;
    float zRotPole;

    InstanceKey<ModelData> pole;
    InstanceKey<ModelData> hand;
    InstanceKey<RotatingData> shaft;

    public DeployerActorInstance(ContraptionKineticRenderer modelManager, MovementContext context) {
        super(modelManager, context);

        RenderMaterial<ContraptionProgram, InstancedModel<ModelData>> mat = modelManager.basicMaterial();

        BlockState state = context.state;
        DeployerTileEntity.Mode mode = NBTHelper.readEnum(context.tileData, "Mode", DeployerTileEntity.Mode.class);
        AllBlockPartials handPose = DeployerRenderer.getHandPose(mode);

        stationaryTimer = context.data.contains("StationaryTimer");
        facing = state.get(FACING);

        boolean rotatePole = state.get(AXIS_ALONG_FIRST_COORDINATE) ^ facing.getAxis() == Direction.Axis.Z;
        yRot = AngleHelper.horizontalAngle(facing);
        zRot = facing == Direction.UP ? 270 : facing == Direction.DOWN ? 90 : 0;
        zRotPole = rotatePole ? 90 : 0;

        pole = mat.getModel(AllBlockPartials.DEPLOYER_POLE, state).createInstance();
        hand = mat.getModel(handPose, state).createInstance();

        Direction.Axis axis = ((IRotate) state.getBlock()).getRotationAxis(state);
        shaft = modelManager.getMaterial(KineticRenderMaterials.ROTATING)
                            .getModel(KineticTileEntityRenderer.KINETIC_TILE, KineticTileInstance.shaft(axis))
                            .createInstance();

        int blockLight = localBlockLight();

        shaft.getInstance()
             .setBlockLight(blockLight)
             .setRotationAxis(axis)
             .setPosition(context.localPos);

        pole.getInstance().setBlockLight(blockLight);
        hand.getInstance().setBlockLight(blockLight);
    }

    @Override
    protected void tick() {
        double factor;
        if (context.contraption.stalled || context.position == null || context.data.contains("StationaryTimer")) {
            factor = MathHelper.sin(AnimationTickHolder.getRenderTime() * .5f) * .25f + .25f;
        } else {
            Vec3d center = VecHelper.getCenterOf(new BlockPos(context.position));
            double distance = context.position.distanceTo(center);
            double nextDistance = context.position.add(context.motion)
                                                  .distanceTo(center);
            factor = .5f - MathHelper.clamp(MathHelper.lerp(AnimationTickHolder.getPartialTicks(), distance, nextDistance), 0, 1);
        }

        Vec3d offset = new Vec3d(facing.getDirectionVec()).scale(factor);

        MatrixStack ms = new MatrixStack();
        MatrixStacker msr = MatrixStacker.of(ms);

        msr.translate(context.localPos)
           .translate(offset);

        DeployerInstance.transformModel(msr, pole, hand, yRot, zRot, zRotPole);
    }
}
