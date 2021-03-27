package com.simibubi.create.content.contraptions.components.flywheel;

import java.util.Collections;
import java.util.List;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.content.contraptions.base.KineticTileInstance;
import com.simibubi.create.content.contraptions.base.RotatingData;
import com.simibubi.create.foundation.render.backend.instancing.*;

import com.simibubi.create.foundation.render.backend.instancing.impl.ModelData;
import com.simibubi.create.foundation.utility.AngleHelper;
import com.simibubi.create.foundation.utility.AnimationTickHolder;
import com.simibubi.create.foundation.utility.MatrixStacker;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.Direction;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.MathHelper;

public class FlyWheelInstance extends KineticTileInstance<FlywheelTileEntity> implements IDynamicInstance {

    protected final Direction facing;
    protected final Direction connection;

    protected boolean connectedLeft;
    protected float connectorAngleMult;

    protected final InstanceKey<RotatingData> shaft;

    protected final InstanceKey<ModelData> wheel;

    protected List<InstanceKey<ModelData>> connectors;
    protected InstanceKey<ModelData> upperRotating;
    protected InstanceKey<ModelData> lowerRotating;
    protected InstanceKey<ModelData> upperSliding;
    protected InstanceKey<ModelData> lowerSliding;

    protected float lastAngle = Float.NaN;

    public FlyWheelInstance(InstancedTileRenderer<?> modelManager, FlywheelTileEntity tile) {
        super(modelManager, tile);

        facing = blockState.get(BlockStateProperties.HORIZONTAL_FACING);

        shaft = setup(shaftModel().createInstance());

        wheel = AllBlockPartials.FLYWHEEL.renderOnHorizontalModel(modelManager, blockState.rotate(Rotation.CLOCKWISE_90)).createInstance();

        connection = FlywheelBlock.getConnection(blockState);
        if (connection != null) {
            connectedLeft = blockState.get(FlywheelBlock.CONNECTION) == FlywheelBlock.ConnectionState.LEFT;

            boolean flipAngle = connection.getAxis() == Direction.Axis.X ^ connection.getAxisDirection() == Direction.AxisDirection.NEGATIVE;

            connectorAngleMult = flipAngle ? -1 : 1;

            RenderMaterial<?, InstancedModel<ModelData>> mat = getTransformMaterial();

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
        MatrixStacker msr = MatrixStacker.of(ms);

        msr.translate(getInstancePosition());

        if (connection != null) {
            float rotation = angle * connectorAngleMult;

            ms.push();
            rotateToFacing(msr, connection);

            ms.push();
            transformConnector(msr, true, true, rotation, connectedLeft);
            upperRotating.getInstance().setTransform(ms);
            ms.pop();

            ms.push();
            transformConnector(msr, false, true, rotation, connectedLeft);
            lowerRotating.getInstance().setTransform(ms);
            ms.pop();

            ms.push();
            transformConnector(msr, true, false, rotation, connectedLeft);
            upperSliding.getInstance().setTransform(ms);
            ms.pop();

            ms.push();
            transformConnector(msr, false, false, rotation, connectedLeft);
            lowerSliding.getInstance().setTransform(ms);
            ms.pop();

            ms.pop();
        }

        msr.centre()
           .rotate(Direction.getFacingFromAxis(Direction.AxisDirection.POSITIVE, facing.getAxis()), AngleHelper.rad(angle))
           .unCentre();

        wheel.getInstance().setTransform(ms);
    }

    @Override
    protected void update() {
        updateRotation(shaft.getInstance());
    }

    @Override
    public void updateLight() {
        relight(pos, shaft.getInstance(), wheel.getInstance());

        if (connection != null) {
            relight(this.pos.offset(connection), connectors.stream().map(InstanceKey::getInstance));
        }
    }

    @Override
    public void remove() {
        shaft.delete();
        wheel.delete();

        connectors.forEach(InstanceKey::delete);
        connectors.clear();
    }

    protected InstancedModel<RotatingData> shaftModel() {
        return AllBlockPartials.SHAFT_HALF.renderOnDirectionalSouthRotating(renderer, blockState, facing.getOpposite());
    }

    protected void transformConnector(MatrixStacker ms, boolean upper, boolean rotating, float angle, boolean flip) {
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

    protected void rotateToFacing(MatrixStacker buffer, Direction facing) {
        buffer.centre()
              .rotate(Direction.UP, AngleHelper.rad(AngleHelper.horizontalAngle(facing)))
              .unCentre();
    }
}
