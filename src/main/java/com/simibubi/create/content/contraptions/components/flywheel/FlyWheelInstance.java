package com.simibubi.create.content.contraptions.components.flywheel;

import static net.minecraft.state.properties.BlockStateProperties.HORIZONTAL_FACING;

import java.util.Collections;
import java.util.List;

import com.google.common.collect.Lists;
import com.jozufozu.flywheel.backend.instancing.IDynamicInstance;
import com.jozufozu.flywheel.backend.instancing.InstanceData;
import com.jozufozu.flywheel.backend.instancing.Instancer;
import com.jozufozu.flywheel.backend.material.InstanceMaterial;
import com.jozufozu.flywheel.backend.material.MaterialManager;
import com.jozufozu.flywheel.core.materials.ModelData;
import com.jozufozu.flywheel.util.transform.MatrixTransformStack;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.content.contraptions.base.KineticTileInstance;
import com.simibubi.create.content.contraptions.base.RotatingData;
import com.simibubi.create.foundation.utility.AngleHelper;
import com.simibubi.create.foundation.utility.AnimationTickHolder;

import net.minecraft.block.BlockState;
import net.minecraft.util.Direction;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.MathHelper;

public class FlyWheelInstance extends KineticTileInstance<FlywheelTileEntity> implements IDynamicInstance {

    protected final Direction facing;
    protected final Direction connection;

    protected boolean connectedLeft;
    protected float connectorAngleMult;

    protected final RotatingData shaft;

    protected final ModelData wheel;

    protected List<ModelData> connectors;
    protected ModelData upperRotating;
    protected ModelData lowerRotating;
    protected ModelData upperSliding;
    protected ModelData lowerSliding;

    protected float lastAngle = Float.NaN;

    public FlyWheelInstance(MaterialManager<?> modelManager, FlywheelTileEntity tile) {
		super(modelManager, tile);

		facing = blockState.getValue(HORIZONTAL_FACING);

		shaft = setup(shaftModel().createInstance());

		BlockState referenceState = blockState.rotate(Rotation.CLOCKWISE_90);
		wheel = getTransformMaterial().getModel(AllBlockPartials.FLYWHEEL, referenceState, referenceState.getValue(HORIZONTAL_FACING)).createInstance();

		connection = FlywheelBlock.getConnection(blockState);
		if (connection != null) {
			connectedLeft = blockState.getValue(FlywheelBlock.CONNECTION) == FlywheelBlock.ConnectionState.LEFT;

			boolean flipAngle = connection.getAxis() == Direction.Axis.X ^ connection.getAxisDirection() == Direction.AxisDirection.NEGATIVE;

			connectorAngleMult = flipAngle ? -1 : 1;

			InstanceMaterial<ModelData> mat = getTransformMaterial();

			upperRotating = mat.getModel(AllBlockPartials.FLYWHEEL_UPPER_ROTATING, blockState).createInstance();
			lowerRotating = mat.getModel(AllBlockPartials.FLYWHEEL_LOWER_ROTATING, blockState).createInstance();
			upperSliding = mat.getModel(AllBlockPartials.FLYWHEEL_UPPER_SLIDING, blockState).createInstance();
			lowerSliding = mat.getModel(AllBlockPartials.FLYWHEEL_LOWER_SLIDING, blockState).createInstance();

			connectors = Lists.newArrayList(upperRotating, lowerRotating, upperSliding, lowerSliding);
		} else {
            connectors = Collections.emptyList();
        }

        animate(tile.angle);
    }

    @Override
    public void beginFrame() {

        float partialTicks = AnimationTickHolder.getPartialTicks();

        float speed = tile.visualSpeed.get(partialTicks) * 3 / 10f;
        float angle = tile.angle + speed * partialTicks;

        if (Math.abs(angle - lastAngle) < 0.001) return;

        animate(angle);

        lastAngle = angle;
    }

    private void animate(float angle) {
        MatrixStack ms = new MatrixStack();
        MatrixTransformStack msr = MatrixTransformStack.of(ms);

        msr.translate(getInstancePosition());

        if (connection != null) {
            float rotation = angle * connectorAngleMult;

            ms.pushPose();
            rotateToFacing(msr, connection);

            ms.pushPose();
            transformConnector(msr, true, true, rotation, connectedLeft);
            upperRotating.setTransform(ms);
            ms.popPose();

            ms.pushPose();
            transformConnector(msr, false, true, rotation, connectedLeft);
            lowerRotating.setTransform(ms);
            ms.popPose();

            ms.pushPose();
            transformConnector(msr, true, false, rotation, connectedLeft);
            upperSliding.setTransform(ms);
            ms.popPose();

            ms.pushPose();
            transformConnector(msr, false, false, rotation, connectedLeft);
            lowerSliding.setTransform(ms);
            ms.popPose();

            ms.popPose();
        }

        msr.centre()
           .rotate(Direction.get(Direction.AxisDirection.POSITIVE, facing.getAxis()), AngleHelper.rad(angle))
           .unCentre();

        wheel.setTransform(ms);
    }

    @Override
    public void update() {
        updateRotation(shaft);
    }

    @Override
    public void updateLight() {
        relight(pos, shaft, wheel);

        if (connection != null) {
            relight(this.pos.relative(connection), connectors.stream());
        }
    }

    @Override
    public void remove() {
        shaft.delete();
        wheel.delete();

        connectors.forEach(InstanceData::delete);
        connectors.clear();
    }

    protected Instancer<RotatingData> shaftModel() {
		Direction opposite = facing.getOpposite();
		return getRotatingMaterial().getModel(AllBlockPartials.SHAFT_HALF, blockState, opposite);
	}

    protected void transformConnector(MatrixTransformStack ms, boolean upper, boolean rotating, float angle, boolean flip) {
        float shift = upper ? 1 / 4f : -1 / 8f;
        float offset = upper ? 1 / 4f : 1 / 4f;
        float radians = (float) (angle / 180 * Math.PI);
        float shifting = MathHelper.sin(radians) * shift + offset;

        float maxAngle = upper ? -5 : -15;
        float minAngle = upper ? -45 : 5;
        float barAngle = 0;

        if (rotating)
            barAngle = MathHelper.lerp((MathHelper.sin((float) (radians + Math.PI / 2)) + 1) / 2, minAngle, maxAngle);

        float pivotX = (upper ? 8f : 3f) / 16;
        float pivotY = (upper ? 8f : 2f) / 16;
        float pivotZ = (upper ? 23f : 21.5f) / 16f;

        ms.translate(pivotX, pivotY, pivotZ + shifting);
        if (rotating)
            ms.rotate(Direction.EAST, AngleHelper.rad(barAngle));
        ms.translate(-pivotX, -pivotY, -pivotZ);

        if (flip && !upper)
            ms.translate(9 / 16f, 0, 0);
    }

    protected void rotateToFacing(MatrixTransformStack buffer, Direction facing) {
        buffer.centre()
              .rotate(Direction.UP, AngleHelper.rad(AngleHelper.horizontalAngle(facing)))
              .unCentre();
    }
}
