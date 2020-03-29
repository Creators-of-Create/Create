package com.simibubi.create.foundation.utility.data;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;

import net.minecraft.util.ResourceLocation;

public interface ITaggable<T extends ITaggable<T>> {

	enum TagType {
		BLOCK, ITEM
	}

	default T withTags(ResourceLocation... tagsIn) {
		return this.withTags(TagType.BLOCK, tagsIn).withTags(TagType.ITEM, tagsIn);
	}

	@SuppressWarnings("unchecked")
	default T withTags(TagType type, ResourceLocation... tagsIn) {
		Collections.addAll(getTagSet(type), tagsIn);
		return (T) this;
	}

	default T withTagsInNamespace(String namespace, String... tagsIn) {
		return withTags(Arrays.stream(tagsIn).map(s -> new ResourceLocation(namespace, s)).toArray(ResourceLocation[]::new));
	}

	default T withTagsInNamespace(TagType type, String namespace, String... tagsIn) {
		return withTags(type, Arrays.stream(tagsIn).map(s -> new ResourceLocation(namespace, s)).toArray(ResourceLocation[]::new));
	}

	default T withCreateTags(String... tagsIn) {
		return withTagsInNamespace("create", tagsIn);
	}

	default T withForgeTags(String... tagsIn) {
		return withTagsInNamespace("forge", tagsIn);
	}

	default T withVanillaTags(String... tagsIn) {
		return withTagsInNamespace("minecraft", tagsIn);
	}

	default T withVanillaTags(TagType type, String... tagsIn) {
		return withTagsInNamespace(type, "minecraft", tagsIn);
	}

	//take a look at AllBlocks.TaggedBlock for more info
	Set<ResourceLocation> getTagSet(TagType type);
}
