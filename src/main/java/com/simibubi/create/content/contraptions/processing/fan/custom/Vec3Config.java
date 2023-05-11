package com.simibubi.create.content.contraptions.processing.fan.custom;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record Vec3Config(float x, float y, float z) {

	public static final Vec3Config ZERO = new Vec3Config(0, 0, 0);

	public static final Codec<Vec3Config> CODEC = RecordCodecBuilder.create(i -> i.group(
			Codec.FLOAT.fieldOf("x").forGetter(e -> e.x),
			Codec.FLOAT.fieldOf("y").forGetter(e -> e.y),
			Codec.FLOAT.fieldOf("z").forGetter(e -> e.z)
	).apply(i, Vec3Config::new));

}
