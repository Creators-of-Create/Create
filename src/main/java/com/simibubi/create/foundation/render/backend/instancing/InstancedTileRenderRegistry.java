package com.simibubi.create.foundation.render.backend.instancing;

import com.google.common.collect.Maps;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;

import javax.annotation.Nullable;
import java.util.Map;

public class InstancedTileRenderRegistry {
    public static final InstancedTileRenderRegistry instance = new InstancedTileRenderRegistry();

    private final Map<TileEntityType<?>, IRendererFactory<?>> renderers = Maps.newHashMap();

    public <T extends TileEntity> void register(TileEntityType<? extends T> type, IRendererFactory<? super T> rendererFactory) {
        this.renderers.put(type, rendererFactory);
    }

    @SuppressWarnings("unchecked")
    @Nullable
    public <T extends TileEntity> TileEntityInstance<? super T> create(InstancedTileRenderer<?> manager, T tile) {
        TileEntityType<?> type = tile.getType();
        IRendererFactory<? super T> factory = (IRendererFactory<? super T>) this.renderers.get(type);

        if (factory == null) return null;
        else return factory.create(manager, tile);
    }

}
