package com.simibubi.create.content.contraptions.components.press;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.content.contraptions.base.KineticTileEntity;
import com.simibubi.create.content.contraptions.relays.encased.ShaftInstance;
import com.simibubi.create.foundation.render.backend.RenderMaterials;
import com.simibubi.create.foundation.render.backend.instancing.ITickableInstance;
import com.simibubi.create.foundation.render.backend.instancing.InstanceKey;
import com.simibubi.create.foundation.render.backend.instancing.InstancedTileRenderRegistry;
import com.simibubi.create.foundation.render.backend.instancing.InstancedTileRenderer;
import com.simibubi.create.foundation.render.backend.instancing.impl.ModelData;
import com.simibubi.create.foundation.utility.AnimationTickHolder;
import com.simibubi.create.foundation.utility.MatrixStacker;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.world.LightType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;

public class PressInstance extends ShaftInstance implements ITickableInstance {

    private InstanceKey<ModelData> pressHead;

    public PressInstance(InstancedTileRenderer<?> dispatcher, KineticTileEntity tile) {
        super(dispatcher, tile);
    }

    @Override
    protected void init() {
        super.init();

        pressHead = modelManager.getMaterial(RenderMaterials.MODELS)
                                .getModel(AllBlockPartials.MECHANICAL_PRESS_HEAD, lastState)
                                .createInstance();

        updateLight();
        transformModels((MechanicalPressTileEntity) tile);
    }

    @Override
    public void tick() {
        MechanicalPressTileEntity press = (MechanicalPressTileEntity) tile;
        if (!press.running)
            return;

        transformModels(press);
    }

    private void transformModels(MechanicalPressTileEntity press) {
        float renderedHeadOffset = getRenderedHeadOffset(press);

        MatrixStack ms = new MatrixStack();

        MatrixStacker msr = MatrixStacker.of(ms);
        msr.translate(getFloatingPos());
        msr.translate(0, -renderedHeadOffset, 0);

        pressHead.getInstance()
                 .setTransformNoCopy(ms);
    }

    private float getRenderedHeadOffset(MechanicalPressTileEntity press) {
        return press.getRenderedHeadOffset(AnimationTickHolder.getPartialTicks());
    }

    @Override
    public void updateLight() {
        super.updateLight();

        pressHead.getInstance()
                 .setBlockLight(world.getLightLevel(LightType.BLOCK, pos))
                 .setSkyLight(world.getLightLevel(LightType.SKY, pos));
    }

    @Override
    public void remove() {
        super.remove();
        pressHead.delete();
    }
}
