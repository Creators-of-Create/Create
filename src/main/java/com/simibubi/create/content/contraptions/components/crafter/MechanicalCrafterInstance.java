package com.simibubi.create.content.contraptions.components.crafter;

import java.util.function.Supplier;

import com.jozufozu.flywheel.backend.instancing.InstancedTileRenderer;
import com.jozufozu.flywheel.backend.instancing.Instancer;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.content.contraptions.base.KineticTileEntity;
import com.simibubi.create.content.contraptions.base.RotatingData;
import com.simibubi.create.content.contraptions.base.SingleRotatingInstance;
import com.simibubi.create.foundation.utility.MatrixStacker;

import net.minecraft.util.Direction;

public class MechanicalCrafterInstance extends SingleRotatingInstance {

    public MechanicalCrafterInstance(InstancedTileRenderer<?> modelManager, KineticTileEntity tile) {
        super(modelManager, tile);
    }

    @Override
    protected Instancer<RotatingData> getModel() {
        Direction facing = blockState.get(MechanicalCrafterBlock.HORIZONTAL_FACING);

        Supplier<MatrixStack> ms = () -> {
            MatrixStack stack = new MatrixStack();
            MatrixStacker stacker = MatrixStacker.of(stack).centre();

            if (facing.getAxis() == Direction.Axis.X)
                stacker.rotateZ(90);
            else if (facing.getAxis() == Direction.Axis.Z)
                stacker.rotateX(90);

            stacker.unCentre();
            return stack;
        };
        return getRotatingMaterial().getModel(AllBlockPartials.SHAFTLESS_COGWHEEL, blockState, facing, ms);
    }
}
