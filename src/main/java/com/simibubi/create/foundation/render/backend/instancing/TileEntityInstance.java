package com.simibubi.create.foundation.render.backend.instancing;

import com.simibubi.create.foundation.render.backend.instancing.impl.ModelData;
import net.minecraft.block.BlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.LightType;
import net.minecraft.world.World;

public abstract class TileEntityInstance<T extends TileEntity> {

    protected final InstancedTileRenderer<?> modelManager;
    protected final T tile;
    protected final World world;
    protected final BlockPos pos;
    protected BlockState lastState;

    public TileEntityInstance(InstancedTileRenderer<?> modelManager, T tile) {
        this.modelManager = modelManager;
        this.tile = tile;
        this.world = tile.getWorld();
        this.pos = tile.getPos();
        this.lastState = tile.getBlockState();
        init();
    }

    public final void update() {
        BlockState currentState = tile.getBlockState();
        if (lastState == currentState) {
            onUpdate();
        } else {
            remove();
            lastState = currentState;
            init();
        }
    }

    /**
     * Acquire all {@link InstanceKey}s and initialize any data you may need to calculate the instance properties.
     */
    protected abstract void init();

    /**
     * Update changed instance data using the {@link InstanceKey}s you got in {@link #init()}.
     * You don't have to update light data. That should be done in {@link #updateLight()}
     */
    protected void onUpdate() { }

    /**
     * Called when a light update occurs in the world. If your model needs it, update light here.
     */
    public void updateLight() { }

    /**
     * Call {@link InstanceKey#delete()} on all acquired keys.
     */
    public abstract void remove();

    public BlockPos getFloatingPos() {
        return pos.subtract(modelManager.getOriginCoordinate());
    }

    protected void relight(BlockPos pos, ModelData... models) {
        relight(world.getLightLevel(LightType.BLOCK, pos), world.getLightLevel(LightType.SKY, pos), models);
    }

    protected void relight(int block, int sky, ModelData... models) {
        for (ModelData model : models) {
            model.setBlockLight(block).setSkyLight(sky);
        }
    }
}
