package com.jozufozu.flywheel.backend.instancing;

import java.util.Map;

import javax.annotation.Nullable;

import com.google.common.collect.Maps;
import com.jozufozu.flywheel.backend.instancing.entity.EntityInstance;
import com.jozufozu.flywheel.backend.instancing.entity.IEntityInstanceFactory;
import com.jozufozu.flywheel.backend.instancing.tile.ITileInstanceFactory;
import com.jozufozu.flywheel.backend.instancing.tile.TileEntityInstance;
import com.simibubi.create.AllEntityTypes;
import com.simibubi.create.content.contraptions.components.structureMovement.glue.GlueInstance;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;

public class InstancedRenderRegistry {
	private static final InstancedRenderRegistry INSTANCE = new InstancedRenderRegistry();

	public static InstancedRenderRegistry getInstance() {
		return INSTANCE;
	}

	private final Map<TileEntityType<?>, ITileInstanceFactory<?>> tiles = Maps.newHashMap();
	private final Map<EntityType<?>, IEntityInstanceFactory<?>> entities = Maps.newHashMap();

	public <T extends TileEntity> void register(TileEntityType<? extends T> type, ITileInstanceFactory<? super T> rendererFactory) {
		this.tiles.put(type, rendererFactory);
	}

	public <T extends Entity> void register(EntityType<? extends T> type, IEntityInstanceFactory<? super T> rendererFactory) {
		this.entities.put(type, rendererFactory);
	}

	static {
		INSTANCE.register(AllEntityTypes.SUPER_GLUE.get(), GlueInstance::new);
	}

	@SuppressWarnings("unchecked")
	@Nullable
	public <T extends TileEntity> TileEntityInstance<? super T> create(MaterialManager<?> manager, T tile) {
		TileEntityType<?> type = tile.getType();
		ITileInstanceFactory<? super T> factory = (ITileInstanceFactory<? super T>) this.tiles.get(type);

		if (factory == null) return null;
		else return factory.create(manager, tile);
	}


	@SuppressWarnings("unchecked")
	@Nullable
	public <T extends Entity> EntityInstance<? super T> create(MaterialManager<?> manager, T tile) {
		EntityType<?> type = tile.getType();
		IEntityInstanceFactory<? super T> factory = (IEntityInstanceFactory<? super T>) this.entities.get(type);

		if (factory == null) return null;
		else return factory.create(manager, tile);
	}

}
