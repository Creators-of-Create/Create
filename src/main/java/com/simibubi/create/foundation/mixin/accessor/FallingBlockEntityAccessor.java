package com.simibubi.create.foundation.mixin.accessor;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

@Mixin(FallingBlockEntity.class)
public interface FallingBlockEntityAccessor {
	@Invoker("<init>")
	static FallingBlockEntity create$callInit(Level level, double x, double y, double z, BlockState state) {
		throw new AssertionError();
	}
}
