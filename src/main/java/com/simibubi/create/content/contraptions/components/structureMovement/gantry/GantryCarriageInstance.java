package com.simibubi.create.content.contraptions.components.structureMovement.gantry;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.content.contraptions.base.KineticTileEntity;
import com.simibubi.create.content.contraptions.base.KineticTileEntityRenderer;
import com.simibubi.create.content.contraptions.relays.encased.ShaftInstance;
import com.simibubi.create.foundation.render.backend.core.ModelData;
import com.simibubi.create.foundation.render.backend.instancing.IDynamicInstance;
import com.simibubi.create.foundation.render.backend.instancing.InstancedTileRenderer;
import com.simibubi.create.foundation.utility.AngleHelper;
import com.simibubi.create.foundation.utility.Iterate;
import com.simibubi.create.foundation.utility.MatrixStacker;

import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3f;

public class GantryCarriageInstance extends ShaftInstance implements IDynamicInstance {

    private final ModelData gantryCogs;

    final Direction facing;
    final Boolean alongFirst;
    final Direction.Axis rotationAxis;
    final float rotationMult;
    final BlockPos visualPos;

    private float lastAngle = Float.NaN;

    public GantryCarriageInstance(InstancedTileRenderer<?> dispatcher, KineticTileEntity tile) {
        super(dispatcher, tile);

        gantryCogs = getTransformMaterial()
                                 .getModel(AllBlockPartials.GANTRY_COGS, blockState)
                                 .createInstance();

        facing = blockState.get(GantryCarriageBlock.FACING);
        alongFirst = blockState.get(GantryCarriageBlock.AXIS_ALONG_FIRST_COORDINATE);
        rotationAxis = KineticTileEntityRenderer.getRotationAxisOf(tile);

        rotationMult = getRotationMultiplier(getGantryAxis(), facing);

        visualPos = facing.getAxisDirection() == Direction.AxisDirection.POSITIVE ? tile.getPos()
                : tile.getPos()
                      .offset(facing.getOpposite());

        animateCogs(getCogAngle());
    }

    @Override
    public void beginFrame() {
        float cogAngle = getCogAngle();

        if (MathHelper.epsilonEquals(cogAngle, lastAngle)) return;

        animateCogs(cogAngle);
    }

    private float getCogAngle() {
        return GantryCarriageRenderer.getAngleForTe(tile, visualPos, rotationAxis) * rotationMult;
    }

    private void animateCogs(float cogAngle) {
        MatrixStack ms = new MatrixStack();
        MatrixStacker.of(ms)
                     .translate(getInstancePosition())
                     .centre()
                     .rotateY(AngleHelper.horizontalAngle(facing))
                     .rotateX(facing == Direction.UP ? 0 : facing == Direction.DOWN ? 180 : 90)
                     .rotateY(alongFirst ^ facing.getAxis() == Direction.Axis.Z ? 90 : 0)
                     .translate(0, -9 / 16f, 0)
                     .multiply(Vector3f.POSITIVE_X.getRadialQuaternion(-cogAngle))
                     .translate(0, 9 / 16f, 0)
                     .unCentre();

        gantryCogs.setTransform(ms);
    }

    static float getRotationMultiplier(Direction.Axis gantryAxis, Direction facing) {
        float multiplier = 1;
        if (gantryAxis == Direction.Axis.Z)
            if (facing == Direction.DOWN)
                multiplier *= -1;
        if (gantryAxis == Direction.Axis.Y)
            if (facing == Direction.NORTH || facing == Direction.EAST)
                multiplier *= -1;

        return multiplier;
    }

    private Direction.Axis getGantryAxis() {
        Direction.Axis gantryAxis = Direction.Axis.X;
        for (Direction.Axis axis : Iterate.axes)
            if (axis != rotationAxis && axis != facing.getAxis())
                gantryAxis = axis;
        return gantryAxis;
    }

    @Override
    public void updateLight() {
        relight(pos, gantryCogs, rotatingModel);
    }

    @Override
    public void remove() {
        super.remove();
        gantryCogs.delete();
    }
}
