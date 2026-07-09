package com.simibubi.create.foundation.render;

import java.util.UUID;

import org.jetbrains.annotations.Nullable;

public interface CreateEntityRenderStateExtension {
	void create$setEntityUUID(@Nullable UUID uuid);

	@Nullable
	UUID create$getEntityUUID();
}
