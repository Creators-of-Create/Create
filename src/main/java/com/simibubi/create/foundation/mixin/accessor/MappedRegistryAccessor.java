package com.simibubi.create.foundation.mixin.accessor;

import java.util.Map;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import it.unimi.dsi.fastutil.objects.Reference2IntMap;
import net.minecraft.core.Holder;
import net.minecraft.core.MappedRegistry;

/**
 * This is very dangerous and should only be used during datagen
 */
@Mixin(MappedRegistry.class)
public interface MappedRegistryAccessor<T> {
	@Accessor
	Reference2IntMap<T> getToId();

	@Accessor
	Map<T, Holder.Reference<T>> getByValue();
}
