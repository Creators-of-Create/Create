package com.simibubi.create.content.equipment.clipboard;

import java.util.List;
import java.util.Optional;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.simibubi.create.content.equipment.clipboard.ClipboardOverrides.ClipboardType;

import net.createmod.catnip.codecs.stream.CatnipStreamCodecBuilders;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record ClipboardContent(ClipboardType type, List<List<ClipboardEntry>> pages, boolean readOnly,
							   int previouslyOpenedPage, Optional<CompoundTag> copiedValues) {
	public static final ClipboardContent EMPTY = new ClipboardContent(ClipboardType.EMPTY, List.of(), false, 0, Optional.empty());

	public static final Codec<List<List<ClipboardEntry>>> PAGES_CODEC = ClipboardEntry.CODEC.listOf().listOf();
	public static final StreamCodec<RegistryFriendlyByteBuf, List<List<ClipboardEntry>>> PAGES_STREAM_CODEC = CatnipStreamCodecBuilders.list(CatnipStreamCodecBuilders.list(ClipboardEntry.STREAM_CODEC));

	public static final Codec<ClipboardContent> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		ClipboardType.CODEC.fieldOf("type").forGetter(ClipboardContent::type),
		PAGES_CODEC.fieldOf("pages").forGetter(ClipboardContent::pages),
		Codec.BOOL.fieldOf("read_only").forGetter(ClipboardContent::readOnly),
		Codec.INT.fieldOf("previously_opened_page").forGetter(ClipboardContent::previouslyOpenedPage),
		CompoundTag.CODEC.optionalFieldOf("copied_values").forGetter(ClipboardContent::copiedValues)
	).apply(instance, ClipboardContent::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, ClipboardContent> STREAM_CODEC = StreamCodec.composite(
		ClipboardType.STREAM_CODEC, ClipboardContent::type,
		PAGES_STREAM_CODEC, ClipboardContent::pages,
		ByteBufCodecs.BOOL, ClipboardContent::readOnly,
		ByteBufCodecs.VAR_INT, ClipboardContent::previouslyOpenedPage,
		ByteBufCodecs.optional(ByteBufCodecs.COMPOUND_TAG), ClipboardContent::copiedValues,
		ClipboardContent::new
	);

	public ClipboardContent(ClipboardType type, List<List<ClipboardEntry>> pages, boolean readOnly) {
		this(type, pages, readOnly, 0, Optional.empty());
	}

	public ClipboardContent setType(ClipboardType type) {
		return new ClipboardContent(type, this.pages, this.readOnly, this.previouslyOpenedPage, this.copiedValues);
	}

	public ClipboardContent setPages(List<List<ClipboardEntry>> pages) {
		return new ClipboardContent(this.type, pages, this.readOnly, this.previouslyOpenedPage, this.copiedValues);
	}

	public ClipboardContent setReadOnly(boolean readOnly) {
		return new ClipboardContent(this.type, this.pages, readOnly, this.previouslyOpenedPage, this.copiedValues);
	}

	public ClipboardContent setPreviouslyOpenedPage(int previouslyOpenedPage) {
		return new ClipboardContent(this.type, this.pages, this.readOnly, previouslyOpenedPage, this.copiedValues);
	}

	public ClipboardContent setCopiedValues(CompoundTag copiedValues) {
		return new ClipboardContent(this.type, this.pages, this.readOnly, this.previouslyOpenedPage, Optional.of(copiedValues));
	}
}
