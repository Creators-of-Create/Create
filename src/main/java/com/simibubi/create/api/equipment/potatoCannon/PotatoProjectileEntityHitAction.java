package com.simibubi.create.api.equipment.potatoCannon;

import java.util.function.Function;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.simibubi.create.api.registry.CreateBuiltInRegistries;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.EntityHitResult;

// TODO: 1.21.1+ - Move into api package
public interface PotatoProjectileEntityHitAction {
	Codec<PotatoProjectileEntityHitAction> CODEC = CreateBuiltInRegistries.POTATO_PROJECTILE_ENTITY_HIT_ACTION.byNameCodec()
		.dispatch(PotatoProjectileEntityHitAction::codec, Function.identity());

	enum Type {
		PRE_HIT,
		ON_HIT
	}

	/**
	 * @return true if the hit should be canceled if the type is {@link Type#PRE_HIT PRE_HIT},
	 * true if this shouldn't recover the projectile if the type is {@link Type#ON_HIT ON_HIT}
	 */
	boolean execute(ItemStack projectile, EntityHitResult ray, Type type);

	MapCodec<? extends PotatoProjectileEntityHitAction> codec();
}
