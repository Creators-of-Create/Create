package com.simibubi.create.content.kinetics.deployer;

import static com.simibubi.create.content.kinetics.base.DirectionalAxisKineticBlock.AXIS_ALONG_FIRST_COORDINATE;
import static com.simibubi.create.content.kinetics.base.DirectionalKineticBlock.FACING;

import java.util.function.Consumer;

import org.joml.Quaternionf;

import com.jozufozu.flywheel.api.event.RenderStage;
import com.jozufozu.flywheel.api.instance.Instance;
import com.jozufozu.flywheel.api.visual.DynamicVisual;
import com.jozufozu.flywheel.api.visual.TickableVisual;
import com.jozufozu.flywheel.api.visual.VisualFrameContext;
import com.jozufozu.flywheel.api.visual.VisualTickContext;
import com.jozufozu.flywheel.api.visualization.VisualizationContext;
import com.jozufozu.flywheel.lib.instance.InstanceTypes;
import com.jozufozu.flywheel.lib.instance.OrientedInstance;
import com.jozufozu.flywheel.lib.model.Models;
import com.jozufozu.flywheel.lib.model.baked.PartialModel;
import com.mojang.math.Axis;
import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.kinetics.base.ShaftVisual;
import com.simibubi.create.foundation.utility.AngleHelper;
import com.simibubi.create.foundation.utility.AnimationTickHolder;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.util.Mth;

public class DeployerVisual extends ShaftVisual<DeployerBlockEntity> implements DynamicVisual, TickableVisual {

    final Direction facing;
    final float yRot;
    final float xRot;
    final float zRot;

    protected final OrientedInstance pole;

    protected OrientedInstance hand;

    PartialModel currentHand;
    float progress;

    public DeployerVisual(VisualizationContext context, DeployerBlockEntity blockEntity) {
        super(context, blockEntity);

        facing = blockState.getValue(FACING);

        boolean rotatePole = blockState.getValue(AXIS_ALONG_FIRST_COORDINATE) ^ facing.getAxis() == Direction.Axis.Z;

        yRot = AngleHelper.horizontalAngle(facing);
        xRot = facing == Direction.UP ? 270 : facing == Direction.DOWN ? 90 : 0;
        zRot = rotatePole ? 90 : 0;

        pole = instancerProvider.instancer(InstanceTypes.ORIENTED, Models.partial(AllPartialModels.DEPLOYER_POLE), RenderStage.AFTER_BLOCK_ENTITIES).createInstance();

		currentHand = this.blockEntity.getHandPose();

		hand = instancerProvider.instancer(InstanceTypes.ORIENTED, Models.partial(currentHand), RenderStage.AFTER_BLOCK_ENTITIES).createInstance();

		progress = getProgress(AnimationTickHolder.getPartialTicks());
        updateRotation(pole, hand, yRot, xRot, zRot);
        updatePosition();
    }

    @Override
    public void tick(VisualTickContext ctx) {
		PartialModel handPose = blockEntity.getHandPose();

		if (currentHand != handPose) {
			currentHand = handPose;
			instancerProvider.instancer(InstanceTypes.ORIENTED, Models.partial(currentHand), RenderStage.AFTER_BLOCK_ENTITIES)
					.stealInstance(hand);
		}
	}

    @Override
    public void beginFrame(VisualFrameContext ctx) {

        float newProgress = getProgress(AnimationTickHolder.getPartialTicks());

        if (Mth.equal(newProgress, progress)) return;

        progress = newProgress;

        updatePosition();
    }

    @Override
    public void updateLight() {
        super.updateLight();
        relight(pos, hand, pole);
    }

    @Override
    protected void _delete() {
        super._delete();
        hand.delete();
        pole.delete();
    }

	private float getProgress(float partialTicks) {
        if (blockEntity.state == DeployerBlockEntity.State.EXPANDING) {
			float f = 1 - (blockEntity.timer - partialTicks * blockEntity.getTimerSpeed()) / 1000f;
			if (blockEntity.fistBump)
				f *= f;
			return f;
		}
        if (blockEntity.state == DeployerBlockEntity.State.RETRACTING)
            return (blockEntity.timer - partialTicks * blockEntity.getTimerSpeed()) / 1000f;
        return 0;
    }

    private void updatePosition() {
        float handLength = currentHand == AllPartialModels.DEPLOYER_HAND_POINTING ? 0
                : currentHand == AllPartialModels.DEPLOYER_HAND_HOLDING ? 4 / 16f : 3 / 16f;
        float distance = Math.min(Mth.clamp(progress, 0, 1) * (blockEntity.reach + handLength), 21 / 16f);
        Vec3i facingVec = facing.getNormal();
        BlockPos blockPos = getVisualPosition();

        float x = blockPos.getX() + ((float) facingVec.getX()) * distance;
        float y = blockPos.getY() + ((float) facingVec.getY()) * distance;
        float z = blockPos.getZ() + ((float) facingVec.getZ()) * distance;

        pole.setPosition(x, y, z);
        hand.setPosition(x, y, z);
    }

    static void updateRotation(OrientedInstance pole, OrientedInstance hand, float yRot, float xRot, float zRot) {

        Quaternionf q = Axis.YP.rotationDegrees(yRot);
        q.mul(Axis.XP.rotationDegrees(xRot));

        hand.setRotation(q);

        q.mul(Axis.ZP.rotationDegrees(zRot));

        pole.setRotation(q);
    }

	@Override
	public void collectCrumblingInstances(Consumer<Instance> consumer) {
		super.collectCrumblingInstances(consumer);
		consumer.accept(pole);
		consumer.accept(hand);
	}
}
