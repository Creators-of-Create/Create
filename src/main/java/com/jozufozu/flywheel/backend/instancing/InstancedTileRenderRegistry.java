package com.jozufozu.flywheel.backend.instancing;

import java.util.Map;

import javax.annotation.Nullable;

import com.google.common.collect.Maps;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;

public class InstancedTileRenderRegistry {
	private static final InstancedTileRenderRegistry INSTANCE = new InstancedTileRenderRegistry();

	public static InstancedTileRenderRegistry getInstance() {
		return INSTANCE;
	}

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
