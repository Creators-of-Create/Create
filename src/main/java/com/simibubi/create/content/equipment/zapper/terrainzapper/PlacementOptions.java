package com.simibubi.create.content.equipment.zapper.terrainzapper;

import org.jetbrains.annotations.NotNull;

import com.mojang.serialization.Codec;
import com.simibubi.create.foundation.gui.AllIcons;

import io.netty.buffer.ByteBuf;
import net.createmod.catnip.codecs.stream.CatnipStreamCodecBuilders;
import net.createmod.catnip.lang.Lang;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;

public enum PlacementOptions implements StringRepresentable {
	Merged(AllIcons.I_CENTERED),
	Attached(AllIcons.I_ATTACHED),
	Inserted(AllIcons.I_INSERTED);

	public static final Codec<PlacementOptions> CODEC = StringRepresentable.fromValues(PlacementOptions::values);
	public static final StreamCodec<ByteBuf, PlacementOptions> STREAM_CODEC = CatnipStreamCodecBuilders.ofEnum(PlacementOptions.class);

	public final String translationKey;
	public final AllIcons icon;

	PlacementOptions(AllIcons icon) {
		this.translationKey = Lang.asId(name());
		this.icon = icon;
	}

	@Override
	public @NotNull String getSerializedName() {
		return Lang.asId(name());
	}
}
