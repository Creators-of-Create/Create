package com.simibubi.create.foundation.mixin.accessor;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.entity.EntityTickList;

@Mixin(ServerLevel.class)
public interface ServerLevelAccessor {
	@Accessor("entityTickList")
	EntityTickList create$getEntityTickList();
}
