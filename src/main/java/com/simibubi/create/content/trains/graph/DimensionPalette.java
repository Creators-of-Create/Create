package com.simibubi.create.content.trains.graph;

import java.util.ArrayList;
import java.util.List;

import io.netty.buffer.ByteBuf;
import net.createmod.catnip.codecs.stream.CatnipStreamCodecBuilders;
import net.createmod.catnip.nbt.NBTHelper;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

public class DimensionPalette {
	public static final StreamCodec<ByteBuf, DimensionPalette> STREAM_CODEC = CatnipStreamCodecBuilders.list(ResourceKey.streamCodec(Registries.DIMENSION))
		.map(DimensionPalette::new, i -> i.gatheredDims);

	private final List<ResourceKey<Level>> gatheredDims;

	public DimensionPalette() {
		gatheredDims = new ArrayList<>();
	}

	public DimensionPalette(List<ResourceKey<Level>> gatheredDims) {
		this.gatheredDims = gatheredDims;
	}

	public int encode(ResourceKey<Level> dimension) {
		int indexOf = gatheredDims.indexOf(dimension);
		if (indexOf == -1) {
			indexOf = gatheredDims.size();
			gatheredDims.add(dimension);
		}
		return indexOf;
	}

	public ResourceKey<Level> decode(int index) {
		if (gatheredDims.size() <= index || index < 0)
			return Level.OVERWORLD;
		return gatheredDims.get(index);
	}

	public void send(FriendlyByteBuf buffer) {
		buffer.writeInt(gatheredDims.size());
		gatheredDims.forEach(rk -> buffer.writeResourceLocation(rk.location()));
	}

	public static DimensionPalette receive(FriendlyByteBuf buffer) {
		DimensionPalette palette = new DimensionPalette();
		int length = buffer.readInt();
		for (int i = 0; i < length; i++)
			palette.gatheredDims.add(ResourceKey.create(Registries.DIMENSION, buffer.readResourceLocation()));
		return palette;
	}

	public void write(CompoundTag tag) {
		tag.put("DimensionPalette", NBTHelper.writeCompoundList(gatheredDims, rk -> {
			CompoundTag c = new CompoundTag();
			c.putString("Id", rk.location()
				.toString());
			return c;
		}));
	}

	public static DimensionPalette read(CompoundTag tag) {
		DimensionPalette palette = new DimensionPalette();
		NBTHelper.iterateCompoundList(tag.getList("DimensionPalette", Tag.TAG_COMPOUND), c -> palette.gatheredDims
			.add(ResourceKey.create(Registries.DIMENSION, ResourceLocation.parse(c.getString("Id")))));
		return palette;
	}

}
