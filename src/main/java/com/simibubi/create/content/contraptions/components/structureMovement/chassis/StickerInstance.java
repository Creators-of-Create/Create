package com.simibubi.create.content.contraptions.components.structureMovement.chassis;

import com.jozufozu.flywheel.backend.instancing.IDynamicInstance;
import com.jozufozu.flywheel.backend.material.MaterialManager;
import com.jozufozu.flywheel.backend.instancing.tile.TileEntityInstance;
import com.jozufozu.flywheel.core.materials.model.ModelData;
import com.jozufozu.flywheel.util.transform.MatrixTransformStack;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.foundation.utility.AngleHelper;
import com.simibubi.create.foundation.utility.AnimationTickHolder;

import net.minecraft.client.Minecraft;
import net.minecraft.util.Direction;
import net.minecraft.util.math.MathHelper;

public class StickerInstance extends TileEntityInstance<StickerTileEntity> implements IDynamicInstance {

    float lastOffset = Float.NaN;
    final Direction facing;
    final boolean fakeWorld;
    final int offset;

    private final ModelData head;

    public StickerInstance(MaterialManager modelManager, StickerTileEntity tile) {
        super(modelManager, tile);

        head = getTransformMaterial().getModel(AllBlockPartials.STICKER_HEAD, blockState).createInstance();

        fakeWorld = tile.getLevel() != Minecraft.getInstance().level;
        facing = blockState.getValue(StickerBlock.FACING);
        offset = blockState.getValue(StickerBlock.EXTENDED) ? 1 : 0;

        animateHead(offset);
    }

    @Override
    public void beginFrame() {
        float offset = tile.piston.getValue(AnimationTickHolder.getPartialTicks());

        if (fakeWorld)
            offset = this.offset;

        if (MathHelper.equal(offset, lastOffset))
            return;

        animateHead(offset);

        lastOffset = offset;
    }

    private void animateHead(float offset) {
        MatrixStack stack = new MatrixStack();
        MatrixTransformStack.of(stack)
                     .translate(getInstancePosition())
                     .nudge(tile.hashCode())
                     .centre()
                     .rotateY(AngleHelper.horizontalAngle(facing))
                     .rotateX(AngleHelper.verticalAngle(facing) + 90)
                     .unCentre()
                     .translate(0, (offset * offset) * 4 / 16f, 0);

        head.setTransform(stack);
    }

    @Override
    public void updateLight() {
        relight(pos, head);
    }

    @Override
    public void remove() {
        head.delete();
    }
}
