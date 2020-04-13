package com.simibubi.create.foundation.utility.data;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.Tag;
import net.minecraft.tags.TagCollection;
import net.minecraft.util.ResourceLocation;

public interface ITaggable<T extends ITaggable<T>> {
    
    interface TagType<T> {
        
        TagCollection<T> getCollection();
    }

    class Impl implements ITaggable<Impl> {

        private Map<TagType<?>, Set<ResourceLocation>> tags = new HashMap<>();

        @Override
        public Set<ResourceLocation> getTagSet(TagType<?> type) {
            return tags.computeIfAbsent(type, $ -> new HashSet<>());
        }
    }
    
    static ITaggable<Impl> create() {
        return new Impl();
    }
    
    static TagType<Block> BLOCK = BlockTags::getCollection;
    static TagType<Item> ITEM = ItemTags::getCollection;

	default T withTags(ResourceLocation... tagsIn) {
		return this.withTags(BLOCK, tagsIn).withTags(ITEM, tagsIn);
	}

	@SuppressWarnings("unchecked")
	default T withTags(TagType<?> type, ResourceLocation... tagsIn) {
		Collections.addAll(getTagSet(type), tagsIn);
		return (T) this;
	}

	default T withTagsInNamespace(String namespace, String... tagsIn) {
		return withTags(Arrays.stream(tagsIn).map(s -> new ResourceLocation(namespace, s)).toArray(ResourceLocation[]::new));
	}

	default T withTagsInNamespace(TagType<?> type, String namespace, String... tagsIn) {
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

	default T withVanillaTags(TagType<?> type, String... tagsIn) {
		return withTagsInNamespace(type, "minecraft", tagsIn);
	}

	//take a look at AllBlocks.TaggedBlock for more info
	Set<ResourceLocation> getTagSet(TagType<?> type);
	
	default <C> Set<Tag<C>> getDataTags(TagType<C> type) {
	    return getTagSet(type).stream().map(type.getCollection()::getOrCreate).collect(Collectors.toSet());
	}
}
