package com.simibubi.create.api.equipment.potatoCannon;

import java.util.function.Function;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.simibubi.create.api.registry.CreateBuiltInRegistries;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.BlockHitResult;

// TODO: 1.21.1+ - Move into api package
public interface PotatoProjectileBlockHitAction {
	Codec<PotatoProjectileBlockHitAction> CODEC = CreateBuiltInRegistries.POTATO_PROJECTILE_BLOCK_HIT_ACTION.byNameCodec()
		.dispatch(PotatoProjectileBlockHitAction::codec, Function.identity());

	boolean execute(LevelAccessor level, ItemStack projectile, BlockHitResult ray);

	MapCodec<? extends PotatoProjectileBlockHitAction> codec();
}
