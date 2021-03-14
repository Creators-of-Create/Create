package com.simibubi.create.content.logistics.block.redstone;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.content.contraptions.components.flywheel.FlyWheelInstance;
import com.simibubi.create.content.contraptions.components.flywheel.FlywheelTileEntity;
import com.simibubi.create.foundation.render.backend.RenderMaterials;
import com.simibubi.create.foundation.render.backend.instancing.*;
import com.simibubi.create.foundation.render.backend.instancing.impl.ModelData;
import com.simibubi.create.foundation.utility.AngleHelper;
import com.simibubi.create.foundation.utility.AnimationTickHolder;
import com.simibubi.create.foundation.utility.ColorHelper;
import com.simibubi.create.foundation.utility.MatrixStacker;
import net.minecraft.state.properties.AttachFace;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;

public class AnalogLeverInstance extends TileEntityInstance<AnalogLeverTileEntity> implements ITickableInstance {

    protected InstanceKey<ModelData> handle;
    protected InstanceKey<ModelData> indicator;

    private float rX;
    private float rY;

    public AnalogLeverInstance(InstancedTileRenderer<?> modelManager, AnalogLeverTileEntity tile) {
        super(modelManager, tile);
    }

    @Override
    protected void init() {
        RenderMaterial<?, InstancedModel<ModelData>> mat = modelManager.getMaterial(RenderMaterials.MODELS);

        handle = mat.getModel(AllBlockPartials.ANALOG_LEVER_HANDLE, lastState).createInstance();
        indicator = mat.getModel(AllBlockPartials.ANALOG_LEVER_INDICATOR, lastState).createInstance();

        AttachFace face = lastState.get(AnalogLeverBlock.FACE);
        rX = face == AttachFace.FLOOR ? 0 : face == AttachFace.WALL ? 90 : 180;
        rY = AngleHelper.horizontalAngle(lastState.get(AnalogLeverBlock.HORIZONTAL_FACING));

        setupModel();
        updateLight();
    }

    @Override
    public void tick() {
        if (!tile.clientState.settled())
            setupModel();
    }

    protected void setupModel() {
        MatrixStack ms = new MatrixStack();
        MatrixStacker msr = MatrixStacker.of(ms);

        msr.translate(getFloatingPos());
        transform(msr);

        float state = tile.clientState.get(AnimationTickHolder.getPartialTicks());

        int color = ColorHelper.mixColors(0x2C0300, 0xCD0000, state / 15f);
        indicator.getInstance()
                 .setTransform(ms)
                 .setColor(color);

        float angle = (float) ((state / 15) * 90 / 180 * Math.PI);
        msr.translate(1 / 2f, 1 / 16f, 1 / 2f)
           .rotate(Direction.EAST, angle)
           .translate(-1 / 2f, -1 / 16f, -1 / 2f);

        handle.getInstance()
              .setTransformNoCopy(ms);
    }

    @Override
    public void remove() {
        handle.delete();
        indicator.delete();
    }

    @Override
    public void updateLight() {
        relight(pos, handle.getInstance(), indicator.getInstance());
    }

    private void transform(MatrixStacker msr) {
        msr.centre()
           .rotate(Direction.UP, (float) (rY / 180 * Math.PI))
           .rotate(Direction.EAST, (float) (rX / 180 * Math.PI))
           .unCentre();
    }
}
