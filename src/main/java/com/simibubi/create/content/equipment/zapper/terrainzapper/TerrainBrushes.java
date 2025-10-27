package com.simibubi.create.content.equipment.zapper.terrainzapper;

import org.jetbrains.annotations.NotNull;

import com.mojang.serialization.Codec;

import io.netty.buffer.ByteBuf;
import net.createmod.catnip.codecs.stream.CatnipStreamCodecBuilders;
import net.createmod.catnip.lang.Lang;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;

public enum TerrainBrushes implements StringRepresentable {
	Cuboid(new CuboidBrush()),
	Sphere(new SphereBrush()),
	Cylinder(new CylinderBrush()),
	Surface(new DynamicBrush(true)),
	Cluster(new DynamicBrush(false));

	public static final Codec<TerrainBrushes> CODEC = StringRepresentable.fromValues(TerrainBrushes::values);
	public static final StreamCodec<ByteBuf, TerrainBrushes> STREAM_CODEC = CatnipStreamCodecBuilders.ofEnum(TerrainBrushes.class);

	private Brush brush;

	TerrainBrushes(Brush brush) {
		this.brush = brush;
	}

	public Brush get() {
		return brush;
	}

	@Override
	public @NotNull String getSerializedName() {
		return Lang.asId(name());
	}
}
