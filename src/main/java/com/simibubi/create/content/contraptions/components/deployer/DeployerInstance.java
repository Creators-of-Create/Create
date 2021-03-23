package com.simibubi.create.content.contraptions.components.deployer;

import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.content.contraptions.base.KineticTileEntity;
import com.simibubi.create.content.contraptions.relays.encased.ShaftInstance;
import com.simibubi.create.foundation.render.backend.RenderMaterials;
import com.simibubi.create.foundation.render.backend.instancing.*;
import com.simibubi.create.foundation.render.backend.instancing.impl.OrientedData;
import com.simibubi.create.foundation.utility.AngleHelper;
import com.simibubi.create.foundation.utility.AnimationTickHolder;

import net.minecraft.client.renderer.Quaternion;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3i;

import static com.simibubi.create.content.contraptions.base.DirectionalAxisKineticBlock.AXIS_ALONG_FIRST_COORDINATE;
import static com.simibubi.create.content.contraptions.base.DirectionalKineticBlock.FACING;

public class DeployerInstance extends ShaftInstance implements IDynamicInstance, ITickableInstance {

    final DeployerTileEntity tile;
    final Direction facing;
    final float yRot;
    final float zRot;
    final float zRotPole;

    protected final InstanceKey<OrientedData> pole;

    protected InstanceKey<OrientedData> hand;

    AllBlockPartials currentHand;
    float progress = Float.NaN;
    private boolean newHand = false;

    public DeployerInstance(InstancedTileRenderer<?> dispatcher, KineticTileEntity tile) {
        super(dispatcher, tile);

        this.tile = (DeployerTileEntity) super.tile;
        facing = blockState.get(FACING);

        boolean rotatePole = blockState.get(AXIS_ALONG_FIRST_COORDINATE) ^ facing.getAxis() == Direction.Axis.Z;

        yRot = AngleHelper.horizontalAngle(facing);
        zRot = facing == Direction.UP ? 270 : facing == Direction.DOWN ? 90 : 0;
        zRotPole = rotatePole ? 90 : 0;

        pole = RenderMaterials.ORIENTED.get(modelManager).getModel(AllBlockPartials.DEPLOYER_POLE, blockState).createInstance();

        updateHandPose();
        relight(pos, pole.getInstance());

        updateRotation(pole, hand, yRot, zRot, zRotPole);
    }

    @Override
    public void tick() {
        newHand = updateHandPose();
    }

    @Override
    public void beginFrame() {

        float newProgress = getProgress(AnimationTickHolder.getPartialTicks());

        if (!newHand && MathHelper.epsilonEquals(newProgress, progress)) return;

        progress = newProgress;
        newHand = false;

        float handLength = currentHand == AllBlockPartials.DEPLOYER_HAND_POINTING ? 0
                : currentHand == AllBlockPartials.DEPLOYER_HAND_HOLDING ? 4 / 16f : 3 / 16f;
        float distance = Math.min(MathHelper.clamp(progress, 0, 1) * (tile.reach + handLength), 21 / 16f);
        Vec3i facingVec = facing.getDirectionVec();
        BlockPos blockPos = getFloatingPos();

        float x = blockPos.getX() + ((float) facingVec.getX()) * distance;
        float y = blockPos.getY() + ((float) facingVec.getY()) * distance;
        float z = blockPos.getZ() + ((float) facingVec.getZ()) * distance;

        pole.getInstance().setPosition(x, y, z);
        hand.getInstance().setPosition(x, y, z);

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

        hand = RenderMaterials.ORIENTED.get(modelManager).getModel(currentHand, blockState).createInstance();

        relight(pos, hand.getInstance());
        updateRotation(pole, hand, yRot, zRot, zRotPole);

        return true;
    }

    private float getProgress(float partialTicks) {
        if (tile.state == DeployerTileEntity.State.EXPANDING)
            return 1 - (tile.timer - partialTicks * tile.getTimerSpeed()) / 1000f;
        if (tile.state == DeployerTileEntity.State.RETRACTING)
            return (tile.timer - partialTicks * tile.getTimerSpeed()) / 1000f;
        return 0;
    }

    static void updateRotation(InstanceKey<OrientedData> pole, InstanceKey<OrientedData> hand, float yRot, float zRot, float zRotPole) {

        Quaternion q = Direction.SOUTH.getUnitVector().getDegreesQuaternion(zRot);
        q.multiply(Direction.UP.getUnitVector().getDegreesQuaternion(yRot));

        hand.getInstance().setRotation(q);

        q.multiply(Direction.SOUTH.getUnitVector().getDegreesQuaternion(zRotPole));

        pole.getInstance().setRotation(q);
    }
}
