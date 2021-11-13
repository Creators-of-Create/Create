package com.simibubi.create.lib.mixin.accessor;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.RailState;

@Mixin(RailState.class)
public interface RailStateAccessor {
	@Accessor("pos")
	BlockPos create$pos();

	@Invoker("removeSoftConnections")
	void create$removeSoftConnections();

	@Invoker("canConnectTo")
	boolean create$canConnectTo(RailState railState);
}
