package com.simibubi.create.content.logistics.block.redstone;

import com.jozufozu.flywheel.backend.instancing.IDynamicInstance;
import com.jozufozu.flywheel.backend.instancing.tile.TileEntityInstance;
import com.jozufozu.flywheel.backend.material.Material;
import com.jozufozu.flywheel.backend.material.MaterialManager;
import com.jozufozu.flywheel.core.materials.ModelData;
import com.jozufozu.flywheel.util.transform.MatrixTransformStack;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.foundation.utility.AngleHelper;
import com.simibubi.create.foundation.utility.AnimationTickHolder;
import com.simibubi.create.foundation.utility.Color;

import net.minecraft.state.properties.AttachFace;
import net.minecraft.util.Direction;

public class AnalogLeverInstance extends TileEntityInstance<AnalogLeverTileEntity> implements IDynamicInstance {

    protected final ModelData handle;
    protected final ModelData indicator;

    final float rX;
    final float rY;

    public AnalogLeverInstance(MaterialManager modelManager, AnalogLeverTileEntity tile) {
        super(modelManager, tile);

        Material<ModelData> mat = getTransformMaterial();

        handle = mat.getModel(AllBlockPartials.ANALOG_LEVER_HANDLE, blockState).createInstance();
        indicator = mat.getModel(AllBlockPartials.ANALOG_LEVER_INDICATOR, blockState).createInstance();

        AttachFace face = blockState.getValue(AnalogLeverBlock.FACE);
        rX = face == AttachFace.FLOOR ? 0 : face == AttachFace.WALL ? 90 : 180;
        rY = AngleHelper.horizontalAngle(blockState.getValue(AnalogLeverBlock.FACING));

        animateLever();
    }

    @Override
    public void beginFrame() {
        if (!tile.clientState.settled())
            animateLever();
    }

    protected void animateLever() {
        MatrixStack ms = new MatrixStack();
        MatrixTransformStack msr = MatrixTransformStack.of(ms);

        msr.translate(getInstancePosition());
        transform(msr);

        float state = tile.clientState.get(AnimationTickHolder.getPartialTicks());

        int color = Color.mixColors(0x2C0300, 0xCD0000, state / 15f);
        indicator.setTransform(ms)
                 .setColor(color);

        float angle = (float) ((state / 15) * 90 / 180 * Math.PI);
        msr.translate(1 / 2f, 1 / 16f, 1 / 2f)
           .rotate(Direction.EAST, angle)
           .translate(-1 / 2f, -1 / 16f, -1 / 2f);

        handle.setTransform(ms);
    }

    @Override
    public void remove() {
        handle.delete();
        indicator.delete();
    }

    @Override
    public void updateLight() {
        relight(pos, handle, indicator);
    }

    private void transform(MatrixTransformStack msr) {
        msr.centre()
           .rotate(Direction.UP, (float) (rY / 180 * Math.PI))
           .rotate(Direction.EAST, (float) (rX / 180 * Math.PI))
           .unCentre();
    }
}
