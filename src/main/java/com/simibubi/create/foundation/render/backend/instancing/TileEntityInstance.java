package com.simibubi.create.foundation.render.backend.instancing;

import com.simibubi.create.foundation.render.backend.instancing.impl.IFlatLight;
import net.minecraft.block.BlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.LightType;
import net.minecraft.world.World;

import java.util.Arrays;
import java.util.stream.Stream;

public abstract class TileEntityInstance<T extends TileEntity> {

    protected final InstancedTileRenderer<?> modelManager;
    protected final T tile;
    protected final World world;
    protected final BlockPos pos;
    protected final BlockState blockState;

    public TileEntityInstance(InstancedTileRenderer<?> modelManager, T tile) {
        this.modelManager = modelManager;
        this.tile = tile;
        this.world = tile.getWorld();
        this.pos = tile.getPos();
        this.blockState = tile.getBlockState();
    }

    /**
     * Update instance data here. Good for when data doesn't change very often and when animations are GPU based.
     * Don't query lighting data here, that's handled separately in {@link #updateLight()}.
     *
     * If your animations are complex and more CPU driven, use {@link IDynamicInstance} or {@link ITickableInstance}.
     */
    protected void update() { }

    /**
     * Called when a light update occurs in the world. If your model needs it, update light here.
     */
    public void updateLight() { }

    /**
     * Call {@link InstanceKey#delete()} on all acquired keys.
     */
    public abstract void remove();

    public boolean shouldReset() {
        return tile.getBlockState() != blockState;
    }

    public BlockPos getFloatingPos() {
        return pos.subtract(modelManager.getOriginCoordinate());
    }

    protected void relight(BlockPos pos, IFlatLight<?>... models) {
        relight(world.getLightLevel(LightType.BLOCK, pos), world.getLightLevel(LightType.SKY, pos), models);
    }

    protected void relight(BlockPos pos, Stream<IFlatLight<?>> models) {
        relight(world.getLightLevel(LightType.BLOCK, pos), world.getLightLevel(LightType.SKY, pos), models);
    }

    protected void relight(int block, int sky, IFlatLight<?>... models) {
        relight(block, sky, Arrays.stream(models));
    }

    protected void relight(int block, int sky, Stream<IFlatLight<?>> models) {
        models.forEach(model -> model.setBlockLight(block).setSkyLight(sky));
    }
}
