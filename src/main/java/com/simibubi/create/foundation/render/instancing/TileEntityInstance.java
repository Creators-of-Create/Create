package com.simibubi.create.foundation.render.instancing;

import com.simibubi.create.foundation.render.InstancedTileRenderDispatcher;
import net.minecraft.block.BlockState;
import net.minecraft.tileentity.TileEntity;

public abstract class TileEntityInstance<T extends TileEntity> {

    protected final InstancedTileRenderDispatcher modelManager;
    protected final T tile;
    protected BlockState lastState;

    public TileEntityInstance(InstancedTileRenderDispatcher modelManager, T tile) {
        this.modelManager = modelManager;
        this.tile = tile;
        this.lastState = tile.getBlockState();
        init();
    }

    protected abstract void init();

    public final void update() {
        BlockState currentState = tile.getBlockState();
        if (lastState == currentState) {
            onUpdate();
        } else {
            remove();
            init();
            lastState = currentState;
        }
    }

    protected abstract void onUpdate();

    public abstract void updateLight();

    public abstract void remove();
}
