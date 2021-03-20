package com.simibubi.create.content.contraptions.components.structureMovement.chassis;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.foundation.render.backend.RenderMaterials;
import com.simibubi.create.foundation.render.backend.instancing.*;
import com.simibubi.create.foundation.render.backend.instancing.impl.ModelData;
import com.simibubi.create.foundation.utility.AngleHelper;
import com.simibubi.create.foundation.utility.AnimationTickHolder;
import com.simibubi.create.foundation.utility.MatrixStacker;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Direction;

public class StickerInstance extends TileEntityInstance<StickerTileEntity> implements IDynamicInstance {

    float lastOffset = Float.NaN;

    private InstanceKey<ModelData> head;

    public StickerInstance(InstancedTileRenderer<?> modelManager, StickerTileEntity tile) {
        super(modelManager, tile);
    }

    @Override
    protected void init() {
        head = modelManager.getMaterial(RenderMaterials.MODELS).getModel(AllBlockPartials.STICKER_HEAD, blockState).createInstance();

        updateLight();
    }

    @Override
    public void beginFrame() {
        blockState = world.getBlockState(pos);

        float offset = tile.piston.getValue(AnimationTickHolder.getPartialTicks());

        if (tile.getWorld() != Minecraft.getInstance().world)
            offset = blockState.get(StickerBlock.EXTENDED) ? 1 : 0;

        if (Math.abs(offset - lastOffset) < 1e-4)
            return;

        Direction facing = blockState.get(StickerBlock.FACING);
        MatrixStack stack = new MatrixStack();
        MatrixStacker.of(stack)
                     .translate(getFloatingPos())
                     .nudge(tile.hashCode())
                     .centre()
                     .rotateY(AngleHelper.horizontalAngle(facing))
                     .rotateX(AngleHelper.verticalAngle(facing) + 90)
                     .unCentre()
                     .translate(0, (offset * offset) * 4 / 16f, 0);

        head.getInstance()
            .setTransformNoCopy(stack);

        lastOffset = offset;
    }

    @Override
    public void updateLight() {
        relight(pos, head.getInstance());
    }

    @Override
    public void remove() {
        head.delete();
    }
}
