package com.simibubi.create.content.processing.recipe;

import java.util.Random;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.createmod.catnip.data.Pair;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class ProcessingOutput {

	public static final ProcessingOutput EMPTY = new ProcessingOutput(ItemStack.EMPTY, 1);

	public static final StreamCodec<RegistryFriendlyByteBuf, ProcessingOutput> STREAM_CODEC = StreamCodec.composite(
		ByteBufCodecs.registry(Registries.ITEM), i -> i.item,
		ByteBufCodecs.INT, i -> i.count,
		DataComponentPatch.STREAM_CODEC, i -> i.patch,
		ByteBufCodecs.FLOAT, i -> i.chance,
		ProcessingOutput::new
	);

	private static final Random r = new Random();
	private final Item item;
	private final int count;
	private final DataComponentPatch patch;
	private final float chance;

	private ResourceLocation datagenOutput;

	public ProcessingOutput(ItemStack stack, float chance) {
		this(stack.getItem(), stack.getCount(), stack.getComponentsPatch(), chance);
	}

	public ProcessingOutput(Item item, int count, float chance) {
		this(item, count, DataComponentPatch.EMPTY, chance);
	}

	public ProcessingOutput(Item item, int count, DataComponentPatch patch, float chance) {
		this.item = item;
		this.count = count;
		this.patch = patch;
		this.chance = chance;
	}

	public ProcessingOutput(ResourceLocation item, int count, float chance) {
		this.item = Items.AIR;
		this.datagenOutput = item;
		this.count = count;
		this.patch = DataComponentPatch.EMPTY;
		this.chance = chance;
	}

	public ItemStack getStack() {
		return new ItemStack(datagenOutput != null ? BuiltInRegistries.ITEM.get(datagenOutput) : item, count);
	}

	public float getChance() {
		return chance;
	}

	public ItemStack rollOutput() {
		int count = this.count;
		if (chance < 1F) {
			for (int roll = 0; roll < this.count; roll++)
				if (r.nextFloat() > chance)
					count--;
			if (count == 0)
				return ItemStack.EMPTY;
		}
		ItemStack output = item.getDefaultInstance();
		output.setCount(count);
		if (!patch.isEmpty())
			output.applyComponents(patch);
		return output;
	}

	// Remove in 1.22
	@Deprecated(forRemoval = true)
	private static final Codec<Either<ItemStack, Pair<ResourceLocation, Integer>>> ITEM_CODEC_OLD = Codec.either(
		ItemStack.SINGLE_ITEM_CODEC,
		ResourceLocation.CODEC.comapFlatMap(
			loc -> DataResult.error(() -> "Compat cannot be deserialized"),
			Pair::getFirst
		)
	);

	// Remove in 1.22
	@Deprecated(forRemoval = true)
	public static final Codec<ProcessingOutput> CODEC_OLD = RecordCodecBuilder.create(i -> i.group(
		ITEM_CODEC_OLD.fieldOf("item").forGetter(s -> s.datagenOutput != null ? Either.right(Pair.of(s.datagenOutput, s.count)) : Either.left(s.item.getDefaultInstance())),
		ExtraCodecs.intRange(1, 99).optionalFieldOf("count", 1).forGetter(s -> s.count),
		Codec.FLOAT.validate(ProcessingOutput::validateChance).optionalFieldOf("chance", 1F).forGetter(s -> s.chance)
	).apply(i, (item, count, chance) -> item.map(
		stack -> new ProcessingOutput(stack.getItem(), count, chance),
		compat -> new ProcessingOutput(compat.getFirst(), compat.getSecond(), chance)
	)));

	private static final Codec<Either<Item, ResourceLocation>> ITEM_CODEC = Codec.either(
		BuiltInRegistries.ITEM.byNameCodec(),
		ResourceLocation.CODEC
	);

	public static final Codec<ProcessingOutput> CODEC_NEW = RecordCodecBuilder.create(i -> i.group(
		ITEM_CODEC.fieldOf("id").forGetter(s -> {
			if (s.datagenOutput != null)
				return Either.right(s.datagenOutput);
			return Either.left(s.item);
		}),
		ExtraCodecs.intRange(1, 99).optionalFieldOf("count", 1).forGetter(s -> s.count),
		DataComponentPatch.CODEC.optionalFieldOf("components", DataComponentPatch.EMPTY).forGetter(s -> s.patch),
		Codec.FLOAT.validate(ProcessingOutput::validateChance).optionalFieldOf("chance", 1F).forGetter(s -> s.chance)
	).apply(i, (item, count, components, chance) -> item.map(
		stack -> new ProcessingOutput(stack, count, components, chance),
		compat -> new ProcessingOutput(compat, count, chance)
	)));

	// Remove fallback in 1.22
	public static final Codec<ProcessingOutput> CODEC = Codec.withAlternative(CODEC_NEW, CODEC_OLD);

	private static DataResult<Float> validateChance(float chance) {
		if (chance > 1F)
			return DataResult.error(() -> "Processing output chance must not be greater than 1, but got: " + chance);
		if (chance <= 0F)
			return DataResult.error(() -> "Processing output chance must be greater than 0, but got: " + chance);
		return DataResult.success(chance);
	}

}
