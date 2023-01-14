package com.simibubi.create.foundation.mixin.accessor;

import net.minecraft.nbt.NbtAccounter;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(NbtAccounter.class)
public interface NbtAccounterAccessor {
	@Accessor("usage")
	long create$getUsage();
}
