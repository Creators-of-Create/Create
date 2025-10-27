package com.simibubi.create.content.processing.recipe;

import net.createmod.catnip.codecs.stream.CatnipStreamCodecBuilders;

import org.jetbrains.annotations.NotNull;

import com.mojang.serialization.Codec;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock.HeatLevel;

import net.createmod.catnip.lang.Lang;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;

public enum HeatCondition implements StringRepresentable {

	NONE(0xffffff), HEATED(0xE88300), SUPERHEATED(0x5C93E8),

	;

	private int color;

	public static final Codec<HeatCondition> CODEC = StringRepresentable.fromEnum(HeatCondition::values);
	public static final StreamCodec<ByteBuf, HeatCondition> STREAM_CODEC = CatnipStreamCodecBuilders.ofEnum(HeatCondition.class);

	HeatCondition(int color) {
		this.color = color;
	}

	public boolean testBlazeBurner(BlazeBurnerBlock.HeatLevel level) {
		if (this == SUPERHEATED)
			return level == HeatLevel.SEETHING;
		if (this == HEATED)
			return level != HeatLevel.NONE && level != HeatLevel.SMOULDERING;
		return true;
	}

	public BlazeBurnerBlock.HeatLevel visualizeAsBlazeBurner() {
		if (this == SUPERHEATED)
			return HeatLevel.SEETHING;
		if (this == HEATED)
			return HeatLevel.KINDLED;
		return HeatLevel.NONE;
	}

	@Override
	public @NotNull String getSerializedName() {
		return Lang.asId(name());
	}

	public String getTranslationKey() {
		return "recipe.heat_requirement." + getSerializedName();
	}

	public int getColor() {
		return color;
	}


}
