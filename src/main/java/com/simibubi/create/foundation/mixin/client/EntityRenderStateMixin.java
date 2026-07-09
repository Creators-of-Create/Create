package com.simibubi.create.foundation.mixin.client;

import java.util.UUID;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import com.simibubi.create.foundation.render.CreateEntityRenderStateExtension;

import net.minecraft.client.renderer.entity.state.EntityRenderState;

@Mixin(EntityRenderState.class)
public class EntityRenderStateMixin implements CreateEntityRenderStateExtension {
	@Unique
	@Nullable
	private UUID create$entityUUID;

	@Override
	public void create$setEntityUUID(@Nullable UUID uuid) {
		create$entityUUID = uuid;
	}

	@Override
	@Nullable
	public UUID create$getEntityUUID() {
		return create$entityUUID;
	}
}
