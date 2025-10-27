package com.simibubi.create.content.logistics.packagePort;

import com.mojang.serialization.MapCodec;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public interface PackagePortTargetType {
	MapCodec<? extends PackagePortTarget> codec();

	StreamCodec<? super RegistryFriendlyByteBuf, ? extends PackagePortTarget> streamCodec();
}
