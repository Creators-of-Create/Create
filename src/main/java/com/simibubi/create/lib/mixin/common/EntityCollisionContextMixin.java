package com.simibubi.create.lib.mixin.common;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.simibubi.create.lib.extensions.EntityCollisionContextExtensions;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.shapes.EntityCollisionContext;

@Mixin(EntityCollisionContext.class)
public abstract class EntityCollisionContextMixin implements EntityCollisionContextExtensions {
	@Unique
	private Entity create$cachedEntity;

	@Inject(method = "<init>(Lnet/minecraft/world/entity/Entity;)V", at = @At("TAIL"))
	private void create$onTailEntityInit(Entity entity, CallbackInfo ci) {
		create$cachedEntity = entity;
	}

	@Override
	public @Nullable Entity create$getCachedEntity() {
		return create$cachedEntity;
	}
}
