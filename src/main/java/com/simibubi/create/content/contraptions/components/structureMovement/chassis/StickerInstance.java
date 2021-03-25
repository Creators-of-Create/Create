package com.simibubi.create.content.contraptions.components.structureMovement.chassis;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.foundation.render.backend.instancing.*;
import com.simibubi.create.foundation.render.backend.instancing.impl.ModelData;
import com.simibubi.create.foundation.utility.AngleHelper;
import com.simibubi.create.foundation.utility.AnimationTickHolder;
import com.simibubi.create.foundation.utility.MatrixStacker;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Direction;
import net.minecraft.util.math.MathHelper;

public class StickerInstance extends TileEntityInstance<StickerTileEntity> implements IDynamicInstance {

    float lastOffset = Float.NaN;
    final Direction facing;
    final boolean fakeWorld;
    final int offset;

    private final InstanceKey<ModelData> head;

    public StickerInstance(InstancedTileRenderer<?> modelManager, StickerTileEntity tile) {
        super(modelManager, tile);

        head = getTransformMaterial().getModel(AllBlockPartials.STICKER_HEAD, blockState).createInstance();

        fakeWorld = tile.getWorld() != Minecraft.getInstance().world;
        facing = blockState.get(StickerBlock.FACING);
        offset = blockState.get(StickerBlock.EXTENDED) ? 1 : 0;
    }

    @Override
    public void beginFrame() {
        float offset = tile.piston.getValue(AnimationTickHolder.getPartialTicks());

        if (fakeWorld)
            offset = this.offset;

        if (MathHelper.epsilonEquals(offset, lastOffset))
            return;

        MatrixStack stack = new MatrixStack();
        MatrixStacker.of(stack)
                     .translate(getInstancePosition())
                     .nudge(tile.hashCode())
                     .centre()
                     .rotateY(AngleHelper.horizontalAngle(facing))
                     .rotateX(AngleHelper.verticalAngle(facing) + 90)
                     .unCentre()
                     .translate(0, (offset * offset) * 4 / 16f, 0);

        head.getInstance()
            .setTransform(stack);

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
