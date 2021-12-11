package com.simibubi.create.lib.mixin.accessor;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.world.entity.Entity;

@Mixin(Entity.class)
public interface EntityAccessor {
	@Invoker("canAddPassenger")
	boolean create$canBeRidden(Entity entity);

	@Invoker("getEncodeId")
	String create$getEntityString();

	@Accessor
	void setRemovalReason(Entity.RemovalReason removalReason);

	@Accessor
	boolean isOnGround();
}
