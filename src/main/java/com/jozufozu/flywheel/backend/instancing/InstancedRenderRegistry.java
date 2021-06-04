package com.jozufozu.flywheel.backend.instancing;

import java.util.Map;

import javax.annotation.Nullable;

import com.google.common.collect.Maps;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;

public class InstancedRenderRegistry {
	private static final InstancedRenderRegistry INSTANCE = new InstancedRenderRegistry();

	public static InstancedRenderRegistry getInstance() {
		return INSTANCE;
	}

	private final Map<TileEntityType<?>, ITileInstanceFactory<?>> renderers = Maps.newHashMap();

	public <T extends TileEntity> void register(TileEntityType<? extends T> type, ITileInstanceFactory<? super T> rendererFactory) {
		this.renderers.put(type, rendererFactory);
	}

	@SuppressWarnings("unchecked")
	@Nullable
	public <T extends TileEntity> TileEntityInstance<? super T> create(MaterialManager<?> manager, T tile) {
		TileEntityType<?> type = tile.getType();
		ITileInstanceFactory<? super T> factory = (ITileInstanceFactory<? super T>) this.renderers.get(type);

		if (factory == null) return null;
		else return factory.create(manager, tile);
	}

}
