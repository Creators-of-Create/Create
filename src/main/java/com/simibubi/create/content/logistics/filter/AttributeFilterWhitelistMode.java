package com.simibubi.create.content.logistics.filter;

import org.jetbrains.annotations.NotNull;

import com.mojang.serialization.Codec;

import io.netty.buffer.ByteBuf;
import net.createmod.catnip.codecs.stream.CatnipStreamCodecBuilders;
import net.createmod.catnip.lang.Lang;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;

public enum AttributeFilterWhitelistMode implements StringRepresentable {
	WHITELIST_DISJ, WHITELIST_CONJ, BLACKLIST;

	public static final Codec<AttributeFilterWhitelistMode> CODEC = StringRepresentable.fromValues(AttributeFilterWhitelistMode::values);
	public static final StreamCodec<ByteBuf, AttributeFilterWhitelistMode> STREAM_CODEC = CatnipStreamCodecBuilders.ofEnum(AttributeFilterWhitelistMode.class);

	@Override
	public @NotNull String getSerializedName() {
		return Lang.asId(name());
	}
}
