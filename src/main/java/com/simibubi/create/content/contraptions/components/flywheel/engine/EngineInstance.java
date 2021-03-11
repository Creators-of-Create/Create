package com.simibubi.create.content.contraptions.components.flywheel.engine;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.foundation.render.backend.RenderMaterials;
import com.simibubi.create.foundation.render.backend.instancing.InstanceKey;
import com.simibubi.create.foundation.render.backend.instancing.InstancedTileRenderRegistry;
import com.simibubi.create.foundation.render.backend.instancing.InstancedTileRenderer;
import com.simibubi.create.foundation.render.backend.instancing.TileEntityInstance;
import com.simibubi.create.foundation.render.backend.instancing.impl.ModelData;
import com.simibubi.create.foundation.utility.AngleHelper;
import com.simibubi.create.foundation.utility.MatrixStacker;
import net.minecraft.block.Block;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.LightType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;

public class EngineInstance extends TileEntityInstance<EngineTileEntity> {
    public static void register(TileEntityType<? extends EngineTileEntity> type) {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
                InstancedTileRenderRegistry.instance.register(type, EngineInstance::new));
    }

    protected InstanceKey<ModelData> frame;

    public EngineInstance(InstancedTileRenderer<?> modelManager, EngineTileEntity tile) {
        super(modelManager, tile);
    }

    @Override
    protected void init() {
        Block block = lastState
                        .getBlock();
        if (!(block instanceof EngineBlock))
            return;

        EngineBlock engineBlock = (EngineBlock) block;
        AllBlockPartials frame = engineBlock.getFrameModel();

        Direction facing = lastState.get(BlockStateProperties.HORIZONTAL_FACING);

        this.frame = modelManager.getMaterial(RenderMaterials.MODELS).getModel(frame, lastState).createInstance();

        float angle = AngleHelper.rad(AngleHelper.horizontalAngle(facing));

        MatrixStack ms = new MatrixStack();
        MatrixStacker msr = MatrixStacker.of(ms);

        msr.translate(getFloatingPos())
           .nudge(tile.hashCode())
           .centre()
           .rotate(Direction.UP, angle)
           .unCentre()
           .translate(0, 0, -1);

        this.frame.getInstance()
                  .setTransformNoCopy(ms);

        updateLight();
    }

    @Override
    public void remove() {
        frame.delete();
    }

    @Override
    public void updateLight() {
        int block = world.getLightLevel(LightType.BLOCK, pos);
        int sky = world.getLightLevel(LightType.SKY, pos);

        frame.getInstance().setBlockLight(block).setSkyLight(sky);
    }
}
