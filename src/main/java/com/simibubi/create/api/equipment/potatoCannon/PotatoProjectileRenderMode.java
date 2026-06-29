package com.simibubi.create.api.equipment.potatoCannon;

import java.util.function.Function;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.simibubi.create.api.registry.CreateBuiltInRegistries;
import com.simibubi.create.content.equipment.potatoCannon.PotatoProjectileEntity;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

// TODO: 1.21.1+ - Move into api package
public interface PotatoProjectileRenderMode {
	Codec<PotatoProjectileRenderMode> CODEC = CreateBuiltInRegistries.POTATO_PROJECTILE_RENDER_MODE.byNameCodec()
		.dispatch(PotatoProjectileRenderMode::codec, Function.identity());

	@OnlyIn(Dist.CLIENT)
	void transform(PoseStack ms, PotatoProjectileEntity entity, float pt);

	MapCodec<? extends PotatoProjectileRenderMode> codec();
}
