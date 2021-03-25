package com.simibubi.create.content.contraptions.components.flywheel.engine;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.foundation.render.backend.instancing.InstanceKey;
import com.simibubi.create.foundation.render.backend.instancing.InstancedTileRenderer;
import com.simibubi.create.foundation.render.backend.instancing.TileEntityInstance;
import com.simibubi.create.foundation.render.backend.instancing.impl.ModelData;
import com.simibubi.create.foundation.utility.AngleHelper;
import com.simibubi.create.foundation.utility.MatrixStacker;
import net.minecraft.block.Block;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.Direction;

public class EngineInstance extends TileEntityInstance<EngineTileEntity> {

    protected InstanceKey<ModelData> frame;

    public EngineInstance(InstancedTileRenderer<?> modelManager, EngineTileEntity tile) {
        super(modelManager, tile);

        Block block = blockState
                .getBlock();
        if (!(block instanceof EngineBlock))
            return;

        EngineBlock engineBlock = (EngineBlock) block;
        AllBlockPartials frame = engineBlock.getFrameModel();

        Direction facing = blockState.get(BlockStateProperties.HORIZONTAL_FACING);

        this.frame = getTransformMaterial().getModel(frame, blockState).createInstance();

        float angle = AngleHelper.rad(AngleHelper.horizontalAngle(facing));

        MatrixStack ms = new MatrixStack();
        MatrixStacker msr = MatrixStacker.of(ms);

        msr.translate(getInstancePosition())
           .nudge(tile.hashCode())
           .centre()
           .rotate(Direction.UP, angle)
           .unCentre()
           .translate(0, 0, -1);

        this.frame.getInstance()
                  .setTransform(ms);
    }

    @Override
    public void remove() {
        frame.delete();
    }

    @Override
    public void updateLight() {
        relight(pos, frame.getInstance());
    }
}
