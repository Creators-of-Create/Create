package com.simibubi.create.foundation.mixin.accessor;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.nbt.NbtAccounter;

@Mixin(NbtAccounter.class)
public interface NbtAccounterAccessor {
	@Accessor("usage")
	long create$getUsage();
}
