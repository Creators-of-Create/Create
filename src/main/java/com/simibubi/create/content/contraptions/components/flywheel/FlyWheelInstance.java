package com.simibubi.create.content.contraptions.components.flywheel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.content.contraptions.base.IRotate;
import com.simibubi.create.content.contraptions.base.KineticTileInstance;
import com.simibubi.create.content.contraptions.base.RotatingData;
import com.simibubi.create.foundation.render.SuperByteBuffer;
import com.simibubi.create.foundation.render.backend.RenderMaterials;
import com.simibubi.create.foundation.render.backend.instancing.*;

import com.simibubi.create.foundation.render.backend.instancing.impl.ModelData;
import com.simibubi.create.foundation.utility.AngleHelper;
import com.simibubi.create.foundation.utility.AnimationTickHolder;
import com.simibubi.create.foundation.utility.MatrixStacker;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.LightType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;

public class FlyWheelInstance extends KineticTileInstance<FlywheelTileEntity> implements ITickableInstance {
    public static void register(TileEntityType<? extends FlywheelTileEntity> type) {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
                InstancedTileRenderRegistry.instance.register(type, FlyWheelInstance::new));
    }

    protected Direction facing;
    protected boolean connectedLeft;
    protected float connectorAngleMult;

    protected Direction connection;

    protected InstanceKey<RotatingData> shaft;

    protected InstanceKey<ModelData> wheel;
    protected InstanceKey<ModelData> upperRotating;
    protected InstanceKey<ModelData> lowerRotating;
    protected InstanceKey<ModelData> upperSliding;
    protected InstanceKey<ModelData> lowerSliding;

    protected List<InstanceKey<ModelData>> connectors;

    protected float lastAngle = Float.NaN;

    protected boolean firstFrame = true;

    public FlyWheelInstance(InstancedTileRenderer<?> modelManager, FlywheelTileEntity tile) {
        super(modelManager, tile);
    }

    @Override
    protected void init() {
        facing = lastState.get(BlockStateProperties.HORIZONTAL_FACING);

        Direction.Axis axis = ((IRotate) lastState.getBlock()).getRotationAxis(lastState);
        shaft = setup(shaftModel().createInstance(), tile.getSpeed(), axis);

        wheel = AllBlockPartials.FLYWHEEL.renderOnHorizontalModel(modelManager, lastState.rotate(Rotation.CLOCKWISE_90)).createInstance();

        connection = FlywheelBlock.getConnection(lastState);
        if (connection != null) {
            connectedLeft = lastState.get(FlywheelBlock.CONNECTION) == FlywheelBlock.ConnectionState.LEFT;

            boolean flipAngle = connection.getAxis() == Direction.Axis.X ^ connection.getAxisDirection() == Direction.AxisDirection.NEGATIVE;

            connectorAngleMult = flipAngle ? -1 : 1;

            RenderMaterial<?, InstancedModel<ModelData>> mat = modelManager.getMaterial(RenderMaterials.MODELS);

            upperRotating = mat.getModel(AllBlockPartials.FLYWHEEL_UPPER_ROTATING, lastState).createInstance();
            lowerRotating = mat.getModel(AllBlockPartials.FLYWHEEL_LOWER_ROTATING, lastState).createInstance();
            upperSliding = mat.getModel(AllBlockPartials.FLYWHEEL_UPPER_SLIDING, lastState).createInstance();
            lowerSliding = mat.getModel(AllBlockPartials.FLYWHEEL_LOWER_SLIDING, lastState).createInstance();

            connectors = Lists.newArrayList(upperRotating, lowerRotating, upperSliding, lowerSliding);
        } else {
            connectors = Collections.emptyList();
        }

        updateLight();
        firstFrame = true;
    }

    @Override
    public void tick() {

        float partialTicks = AnimationTickHolder.getPartialTicks();

        float speed = tile.visualSpeed.get(partialTicks) * 3 / 10f;
        float angle = tile.angle + speed * partialTicks;

        if (!firstFrame && Math.abs(angle - lastAngle) < 0.001) return;

        MatrixStack ms = new MatrixStack();
        MatrixStacker msr = MatrixStacker.of(ms);

        msr.translate(getFloatingPos());

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

        wheel.getInstance().setTransformNoCopy(ms);

        lastAngle = angle;
        firstFrame = false;
    }

    @Override
    protected void onUpdate() {
        Direction.Axis axis = ((IRotate) lastState.getBlock()).getRotationAxis(lastState);
        updateRotation(shaft, axis);
    }

    @Override
    public void updateLight() {
        int block = world.getLightLevel(LightType.BLOCK, pos);
        int sky = world.getLightLevel(LightType.SKY, pos);

        shaft.getInstance().setBlockLight(block).setSkyLight(sky);
        wheel.getInstance().setBlockLight(block).setSkyLight(sky);

        if (connection != null) {
            BlockPos pos = this.pos.offset(connection);

            int connectionBlock = world.getLightLevel(LightType.BLOCK, pos);
            int connectionSky = world.getLightLevel(LightType.SKY, pos);
            connectors.stream()
                      .map(InstanceKey::getInstance)
                      .forEach(data -> data.setBlockLight(connectionBlock).setSkyLight(connectionSky));
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
        return AllBlockPartials.SHAFT_HALF.renderOnDirectionalSouthRotating(modelManager, lastState, facing.getOpposite());
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
