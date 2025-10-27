package com.simibubi.create.foundation.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jetbrains.annotations.ApiStatus;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Multimap;
import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import com.simibubi.create.Create;
import com.simibubi.create.content.kinetics.fan.processing.SplashingRecipe;
import com.simibubi.create.content.kinetics.saw.CuttingRecipe;
import com.simibubi.create.content.processing.recipe.StandardProcessingRecipe;
import com.simibubi.create.foundation.data.recipe.Mods;
import com.simibubi.create.foundation.mixin.accessor.ConcretePowderBlockAccessor;
import com.simibubi.create.foundation.pack.DynamicPack;
import com.simibubi.create.foundation.recipe.IRecipeTypeInfo;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.createmod.catnip.codecs.CatnipCodecUtils;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagEntry;
import net.minecraft.tags.TagFile;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ConcretePowderBlock;

import net.neoforged.neoforge.common.conditions.WithConditions;

@ApiStatus.Internal
public class RuntimeDataGenerator {
	// (1. variant_prefix, optional, can be null)stripped_(2. wood name)(3. type)(4. empty group)endofline
	private static final Pattern STRIPPED_WOODS_PREFIX_REGEX = Pattern.compile("(\\w*)??stripped_(\\w*)(_log|_wood|_stem|_hyphae|_block|(?<!_)wood)()$");
	// (1. wood name)(2. type)(3. variant_suffix, optional)_stripped(4. 2nd variant_suffix, optional)
	private static final Pattern STRIPPED_WOOD_SUFFIX_REGEX = Pattern.compile("(\\w*)(_log|_wood|_stem|_hyphae|_block|(?<!_)wood)(\\w*)_stripped(\\w*)");
	// startofline(not preceded by stripped_)(1. wood_name)(2. type)(3. (4. variant suffix), optional, that doesn't end in _stripped, can be null)endofline
	private static final Pattern NON_STRIPPED_WOODS_REGEX = Pattern.compile("^(?!stripped_)([a-z_]+)(_log|_wood|_stem|_hyphae|(?<!bioshroom)_block)(([a-z_]+)(?<!_stripped))?$");
	private static final Multimap<ResourceLocation, TagEntry> TAGS = HashMultimap.create();
	private static final Object2ObjectOpenHashMap<ResourceLocation, JsonElement> JSON_FILES = new Object2ObjectOpenHashMap<>();
	private static final Map<ResourceLocation, ResourceLocation> MISMATCHED_WOOD_NAMES = ImmutableMap.<ResourceLocation, ResourceLocation>builder()
		.put(Mods.ARS_N.asResource("blue_archwood"), Mods.ARS_N.asResource("archwood")) // Generate recipes for planks -> everything else
		//.put(Mods.UUE.asResource("chorus_cane"), Mods.UUE.asResource("chorus_nest")) // Has a weird setup with both normal and stripped planks, that it already provides cutting recipes for
		.put(Mods.DD.asResource("blooming"), Mods.DD.asResource("bloom"))
		.build();

	public static void insertIntoPack(DynamicPack dynamicPack) {
		for (ResourceLocation itemId : BuiltInRegistries.ITEM.keySet()) {
			cuttingRecipes(itemId);
			washingRecipes(itemId);
		}

		Create.LOGGER.info("Created {} recipes which will be injected into the game", JSON_FILES.size());
		JSON_FILES.forEach(dynamicPack::put);

		Create.LOGGER.info("Created {} tags which will be injected into the game", TAGS.size());
		for (Map.Entry<ResourceLocation, Collection<TagEntry>> tags : TAGS.asMap().entrySet()) {
			TagFile tagFile = new TagFile(new ArrayList<>(tags.getValue()), false);
			dynamicPack.put(tags.getKey().withPrefix("tags/item/"), TagFile.CODEC.encodeStart(JsonOps.INSTANCE, tagFile).result().orElseThrow());
		}

		JSON_FILES.clear();
		JSON_FILES.trim();
		TAGS.clear();
	}

	// logs/woods -> stripped variants
	// logs/woods both stripped and non stripped -> planks
	// planks -> stairs, slabs, fences, fence gates, doors, trapdoors, pressure plates, buttons and signs
	private static void cuttingRecipes(ResourceLocation itemId) {
		String path = itemId.getPath();

		Matcher match = STRIPPED_WOODS_PREFIX_REGEX.matcher(path);
		boolean hasFoundMatch = match.find();
		boolean strippedInPrefix = hasFoundMatch;

		if (!hasFoundMatch) {
			match = STRIPPED_WOOD_SUFFIX_REGEX.matcher(path);
			hasFoundMatch = match.find();
		}

		// Last ditch attempt. Try to find logs without stripped variants
		boolean noStrippedVariant = false;
		if (!hasFoundMatch && !BuiltInRegistries.ITEM.containsKey(itemId.withPrefix("stripped_"))
			&& !BuiltInRegistries.ITEM.containsKey(itemId.withSuffix("_stripped"))) {
			match = NON_STRIPPED_WOODS_REGEX.matcher(path);
			hasFoundMatch = match.find();
			noStrippedVariant = true;
		}

		if (hasFoundMatch) {
			String prefix = strippedInPrefix && match.group(1) != null ? match.group(1) : "";
			String suffix = !strippedInPrefix && !noStrippedVariant ? match.group(3) + match.group(4) : "";
			String type = match.group(strippedInPrefix ? 3 : 2);
			ResourceLocation matched_name = itemId.withPath(match.group(strippedInPrefix ? 2 : 1));
			// re-add 'wood' to wood types such as Botania's livingwood
			ResourceLocation base = matched_name.withSuffix(type.equals("wood") ? "wood" : "");
			base = MISMATCHED_WOOD_NAMES.getOrDefault(base, base);
			ResourceLocation nonStrippedId = matched_name.withSuffix(type).withPrefix(prefix).withSuffix(suffix);
			ResourceLocation planksId = base.withSuffix("_planks");
			ResourceLocation stairsId = base.withSuffix(base.getNamespace().equals(Mods.BTN.getId()) ? "_planks_stairs" : "_stairs");
			ResourceLocation slabId = base.withSuffix(base.getNamespace().equals(Mods.BTN.getId()) ? "_planks_slab" : "_slab");
			ResourceLocation fenceId = base.withSuffix("_fence");
			ResourceLocation fenceGateId = base.withSuffix("_fence_gate");
			ResourceLocation doorId = base.withSuffix("_door");
			ResourceLocation trapdoorId = base.withSuffix("_trapdoor");
			ResourceLocation pressurePlateId = base.withSuffix("_pressure_plate");
			ResourceLocation buttonId = base.withSuffix("_button");
			ResourceLocation signId = base.withSuffix("_sign");
			// Bamboo, GotD whistlecane
			int planksCount = type.contains("block") ? 3 : 6;

			if (!noStrippedVariant) {
				// Catch mods like JNE that have a non-stripped log prefixed but not the stripped log
				if (BuiltInRegistries.ITEM.containsKey(nonStrippedId)) {
					simpleWoodRecipe(nonStrippedId, itemId);
				}
				simpleWoodRecipe(itemId, planksId, planksCount);
			} else if (BuiltInRegistries.ITEM.containsKey(planksId)) {
				ResourceLocation tag = Create.asResource("runtime_generated/compat/" + itemId.getNamespace() + "/" + base.getPath());
				insertIntoTag(tag, itemId);

				simpleWoodRecipe(TagKey.create(Registries.ITEM, tag), planksId, planksCount);
			}

			if (!path.contains("_wood") && !path.contains("_hyphae") && BuiltInRegistries.ITEM.containsKey(planksId)) {
				simpleWoodRecipe(planksId, stairsId);
				simpleWoodRecipe(planksId, slabId, 2);
				simpleWoodRecipe(planksId, fenceId);
				simpleWoodRecipe(planksId, fenceGateId);
				simpleWoodRecipe(planksId, doorId);
				simpleWoodRecipe(planksId, trapdoorId);
				simpleWoodRecipe(planksId, pressurePlateId);
				simpleWoodRecipe(planksId, buttonId);
				simpleWoodRecipe(planksId, signId);
			}
		}
	}

	private static void washingRecipes(ResourceLocation itemId) {
		Block block = BuiltInRegistries.BLOCK.get(itemId);
		if (block instanceof ConcretePowderBlock concretePowderBlock) {
			Block concreteBlock = ((ConcretePowderBlockAccessor) concretePowderBlock).create$getConcrete();
			simpleSplashingRecipe(itemId, BuiltInRegistries.BLOCK.getKey(concreteBlock));
		}
	}

	private static void insertIntoTag(ResourceLocation tag, ResourceLocation itemId) {
		if (BuiltInRegistries.ITEM.containsKey(itemId))
			TAGS.put(tag, TagEntry.optionalElement(itemId));
	}

	private static void simpleWoodRecipe(ResourceLocation inputId, ResourceLocation outputId) {
		simpleWoodRecipe(inputId, outputId, 1);
	}

	private static void simpleWoodRecipe(ResourceLocation inputId, ResourceLocation outputId, int amount) {
		if (BuiltInRegistries.ITEM.containsKey(outputId)) {
			new StandardBuilder<>(inputId.getNamespace(), CuttingRecipe::new, inputId.getPath(), outputId.getPath())
				.require(BuiltInRegistries.ITEM.get(inputId))
				.output(BuiltInRegistries.ITEM.get(outputId), amount)
				.duration(50)
				.build();
		}
	}

	private static void simpleWoodRecipe(TagKey<Item> inputTag, ResourceLocation outputId, int amount) {
		if (BuiltInRegistries.ITEM.containsKey(outputId)) {
			new StandardBuilder<>(inputTag.location().getNamespace(), CuttingRecipe::new, "tag_" + inputTag.location().getPath(), outputId.getPath())
				.require(inputTag)
				.output(BuiltInRegistries.ITEM.get(outputId), amount)
				.duration(50)
				.build();
		}
	}

	private static void simpleSplashingRecipe(ResourceLocation first, ResourceLocation second) {
		new StandardBuilder<>(first.getNamespace(), SplashingRecipe::new, first.getPath(), second.getPath())
			.require(BuiltInRegistries.BLOCK.get(first))
			.output(BuiltInRegistries.BLOCK.get(second))
			.build();
	}

	private static class StandardBuilder<T extends StandardProcessingRecipe<?>> extends StandardProcessingRecipe.Builder<T> {
		public StandardBuilder(String modid, StandardProcessingRecipe.Factory<T> factory, String from, String to) {
			super(factory, Create.asResource("runtime_generated/compat/" + modid + "/" + from + "_to_" + to));
		}

		@Override
		public T build() {
			T recipe = super.build();

			IRecipeTypeInfo recipeType = recipe.getTypeInfo();
			ResourceLocation typeId = recipeType.getId();

			if (!(recipeType.getSerializer() instanceof StandardProcessingRecipe.Serializer))
				throw new IllegalStateException("Cannot datagen ProcessingRecipe of type: " + typeId);

			ResourceLocation id = ResourceLocation.fromNamespaceAndPath(recipeId.getNamespace(),
				typeId.getPath() + "/" + recipeId.getPath());

			Optional<JsonElement> serialized = CatnipCodecUtils.encode(Recipe.CONDITIONAL_CODEC, JsonOps.INSTANCE, Optional.of(new WithConditions<>(recipe)));
			serialized.ifPresent(r -> JSON_FILES.put(id.withPrefix("recipe/"), r));
			return recipe;
		}
	}
}
