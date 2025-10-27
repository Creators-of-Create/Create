package com.simibubi.create.foundation.codec;

import java.util.Vector;
import java.util.function.BiFunction;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public interface CreateStreamCodecs {
	/**
	 * @deprecated Vector should be replaced with list
	 */
	@Deprecated(forRemoval = true)
	static <B extends ByteBuf, V> StreamCodec.CodecOperation<B, V, Vector<V>> vector() {
		return codec -> ByteBufCodecs.collection(Vector::new, codec);
	}

	/**
	 * @deprecated All uses should be converted to proper codecs, but will require notable refactoring
	 */
	@Deprecated(forRemoval = true)
	static <C> StreamCodec<RegistryFriendlyByteBuf, C> ofLegacyNbtWithRegistries(
			BiFunction<C, HolderLookup.Provider, CompoundTag> writer,
			BiFunction<HolderLookup.Provider, CompoundTag, C> reader
	) {
		return new StreamCodec<>() {
			@Override
			public C decode(RegistryFriendlyByteBuf buffer) {
				return reader.apply(buffer.registryAccess(), buffer.readNbt());
			}

			@Override
			public void encode(RegistryFriendlyByteBuf buffer, C value) {
				buffer.writeNbt(writer.apply(value, buffer.registryAccess()));
			}
		};
	}
}
