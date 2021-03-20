package com.simibubi.create.content.contraptions.components.deployer;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.content.contraptions.base.KineticTileEntity;
import com.simibubi.create.content.contraptions.relays.encased.ShaftInstance;
import com.simibubi.create.foundation.render.backend.instancing.ITickableInstance;
import com.simibubi.create.foundation.render.backend.instancing.InstanceKey;
import com.simibubi.create.foundation.render.backend.instancing.InstancedTileRenderer;
import com.simibubi.create.foundation.render.backend.instancing.impl.ModelData;
import com.simibubi.create.foundation.utility.AngleHelper;
import com.simibubi.create.foundation.utility.AnimationTickHolder;
import com.simibubi.create.foundation.utility.MatrixStacker;
import net.minecraft.util.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;

import static com.simibubi.create.content.contraptions.base.DirectionalAxisKineticBlock.AXIS_ALONG_FIRST_COORDINATE;
import static com.simibubi.create.content.contraptions.base.DirectionalKineticBlock.FACING;

public class DeployerInstance extends ShaftInstance implements ITickableInstance {

    DeployerTileEntity tile;

    Direction facing;

    InstanceKey<ModelData> pole;

    AllBlockPartials currentHand;
    InstanceKey<ModelData> hand;

    float yRot;
    float zRot;
    float zRotPole;

    float progress = Float.NaN;

    public DeployerInstance(InstancedTileRenderer<?> dispatcher, KineticTileEntity tile) {
        super(dispatcher, tile);
    }

    @Override
    protected void init() {
        super.init();

        this.tile = (DeployerTileEntity) super.tile;
        facing = lastState.get(FACING);

        boolean rotatePole = lastState.get(AXIS_ALONG_FIRST_COORDINATE) ^ facing.getAxis() == Direction.Axis.Z;

        yRot = AngleHelper.horizontalAngle(facing);
        zRot = facing == Direction.UP ? 270 : facing == Direction.DOWN ? 90 : 0;
        zRotPole = rotatePole ? 90 : 0;

        pole = modelManager.basicMaterial().getModel(AllBlockPartials.DEPLOYER_POLE, lastState).createInstance();

        updateHandPose();
        relight(pos, pole.getInstance());
    }

    @Override
    public void tick() {

        boolean newHand = updateHandPose();

        float newProgress = getProgress(AnimationTickHolder.getPartialTicks());

        if (!newHand && MathHelper.epsilonEquals(newProgress, progress)) return;

        progress = newProgress;

        MatrixStack ms = new MatrixStack();
        MatrixStacker msr = MatrixStacker.of(ms);

        msr.translate(getFloatingPos())
           .translate(getHandOffset());

        transformModel(msr, pole, hand, yRot, zRot, zRotPole);

    }

    @Override
    public void updateLight() {
        super.updateLight();
        relight(pos, hand.getInstance(), pole.getInstance());
    }

    @Override
    public void remove() {
        super.remove();
        hand.delete();
        pole.delete();
        currentHand = null; // updateHandPose() uses an invalid key after a block update otherwise.
        hand = null;
    }

    private boolean updateHandPose() {
        AllBlockPartials handPose = tile.getHandPose();

        if (currentHand == handPose) return false;
        currentHand = handPose;

        if (hand != null) hand.delete();

        hand = modelManager.basicMaterial().getModel(currentHand, lastState).createInstance();

        relight(pos, hand.getInstance());

        return true;
    }

    protected Vector3d getHandOffset() {
        float handLength = tile.getHandPose() == AllBlockPartials.DEPLOYER_HAND_POINTING ? 0
                : tile.getHandPose() == AllBlockPartials.DEPLOYER_HAND_HOLDING ? 4 / 16f : 3 / 16f;
        float distance = Math.min(MathHelper.clamp(progress, 0, 1) * (tile.reach + handLength), 21 / 16f);
        return Vector3d.of(facing.getDirectionVec()).scale(distance);
    }

    private float getProgress(float partialTicks) {
        if (tile.state == DeployerTileEntity.State.EXPANDING)
            return 1 - (tile.timer - partialTicks * tile.getTimerSpeed()) / 1000f;
        if (tile.state == DeployerTileEntity.State.RETRACTING)
            return (tile.timer - partialTicks * tile.getTimerSpeed()) / 1000f;
        return 0;
    }

    static void transformModel(MatrixStacker msr, InstanceKey<ModelData> pole, InstanceKey<ModelData> hand, float yRot, float zRot, float zRotPole) {

        msr.centre();
        msr.rotate(Direction.SOUTH, (float) ((zRot) / 180 * Math.PI));
        msr.rotate(Direction.UP, (float) ((yRot) / 180 * Math.PI));

        msr.push();
        msr.rotate(Direction.SOUTH, (float) ((zRotPole) / 180 * Math.PI));
        msr.unCentre();
        pole.getInstance().setTransform(msr.unwrap());
        msr.pop();

        msr.unCentre();

        hand.getInstance().setTransform(msr.unwrap());
    }
}
