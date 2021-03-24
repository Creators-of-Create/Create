package com.simibubi.create.content.contraptions.components.structureMovement.gantry;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.content.contraptions.base.KineticTileEntity;
import com.simibubi.create.content.contraptions.base.KineticTileEntityRenderer;
import com.simibubi.create.content.contraptions.relays.encased.ShaftInstance;
import com.simibubi.create.foundation.render.backend.RenderMaterials;
import com.simibubi.create.foundation.render.backend.instancing.IDynamicInstance;
import com.simibubi.create.foundation.render.backend.instancing.InstanceKey;
import com.simibubi.create.foundation.render.backend.instancing.InstancedTileRenderer;
import com.simibubi.create.foundation.render.backend.instancing.impl.ModelData;
import com.simibubi.create.foundation.utility.AngleHelper;
import com.simibubi.create.foundation.utility.Iterate;
import com.simibubi.create.foundation.utility.MatrixStacker;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3f;

public class GantryCarriageInstance extends ShaftInstance implements IDynamicInstance {

    private final InstanceKey<ModelData> gantryCogs;

    final Direction facing;
    final Boolean alongFirst;
    final Direction.Axis rotationAxis;
    final BlockPos visualPos;

    public GantryCarriageInstance(InstancedTileRenderer<?> dispatcher, KineticTileEntity tile) {
        super(dispatcher, tile);

        gantryCogs = modelManager.getMaterial(RenderMaterials.TRANSFORMED)
                                 .getModel(AllBlockPartials.GANTRY_COGS, blockState)
                                 .createInstance();

        facing = blockState.get(GantryCarriageBlock.FACING);
        alongFirst = blockState.get(GantryCarriageBlock.AXIS_ALONG_FIRST_COORDINATE);
        rotationAxis = KineticTileEntityRenderer.getRotationAxisOf(tile);

        visualPos = facing.getAxisDirection() == Direction.AxisDirection.POSITIVE ? tile.getPos()
                : tile.getPos()
                      .offset(facing.getOpposite());

        updateLight();
    }

    @Override
    public void beginFrame() {
        float angleForTe = GantryCarriageRenderer.getAngleForTe(tile, visualPos, rotationAxis);

        Direction.Axis gantryAxis = Direction.Axis.X;
        for (Direction.Axis axis : Iterate.axes)
            if (axis != rotationAxis && axis != facing.getAxis())
                gantryAxis = axis;

        if (gantryAxis == Direction.Axis.Z)
            if (facing == Direction.DOWN)
                angleForTe *= -1;
        if (gantryAxis == Direction.Axis.Y)
            if (facing == Direction.NORTH || facing == Direction.EAST)
                angleForTe *= -1;

        MatrixStack ms = new MatrixStack();
        MatrixStacker.of(ms)
                     .translate(getFloatingPos())
                     .centre()
                     .rotateY(AngleHelper.horizontalAngle(facing))
                     .rotateX(facing == Direction.UP ? 0 : facing == Direction.DOWN ? 180 : 90)
                     .rotateY(alongFirst ^ facing.getAxis() == Direction.Axis.Z ? 90 : 0)
                     .translate(0, -9 / 16f, 0)
                     .multiply(Vector3f.POSITIVE_X.getRadialQuaternion(-angleForTe))
                     .translate(0, 9 / 16f, 0)
                     .unCentre();

        gantryCogs.getInstance().setTransform(ms);
    }

    @Override
    public void updateLight() {
        relight(pos, gantryCogs.getInstance());
    }

    @Override
    public void remove() {
        super.remove();
        gantryCogs.delete();
    }
}
