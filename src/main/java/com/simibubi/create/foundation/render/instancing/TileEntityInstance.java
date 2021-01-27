package com.simibubi.create.foundation.render.instancing;

import com.simibubi.create.foundation.render.InstancedTileRenderer;
import net.minecraft.block.BlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public abstract class TileEntityInstance<T extends TileEntity> {

    protected final InstancedTileRenderer modelManager;
    protected final T tile;
    protected final World world;
    protected final BlockPos pos;
    protected BlockState lastState;

    public TileEntityInstance(InstancedTileRenderer modelManager, T tile) {
        this.modelManager = modelManager;
        this.tile = tile;
        this.world = tile.getWorld();
        this.pos = tile.getPos();
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
            lastState = currentState;
            init();
        }
    }

    protected abstract void onUpdate();

    public abstract void updateLight();

    public abstract void remove();
}
