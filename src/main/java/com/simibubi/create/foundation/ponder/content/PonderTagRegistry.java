package com.simibubi.create.foundation.ponder.content;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import com.simibubi.create.foundation.ponder.PonderRegistry;
import net.minecraft.util.ResourceLocation;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

public class PonderTagRegistry {

	private final Multimap<ResourceLocation, PonderTag> tags;
	private final Multimap<PonderChapter, PonderTag> chapterTags;

	public PonderTagRegistry() {
		tags = LinkedHashMultimap.create();
		chapterTags = LinkedHashMultimap.create();
	}

	public Set<PonderTag> getTags(ResourceLocation item) {
		return ImmutableSet.copyOf(tags.get(item));
	}

	public Set<PonderTag> getTags(PonderChapter chapter) {
		return ImmutableSet.copyOf(chapterTags.get(chapter));
	}

	public Set<ResourceLocation> getItems(PonderTag tag) {
		return tags
				.entries()
				.stream()
				.filter(e -> e.getValue() == tag)
				.map(Map.Entry::getKey)
				.collect(ImmutableSet.toImmutableSet());
	}

	public Set<PonderChapter> getChapters(PonderTag tag) {
		return chapterTags
				.entries()
				.stream()
				.filter(e -> e.getValue() == tag)
				.map(Map.Entry::getKey)
				.collect(ImmutableSet.toImmutableSet());
	}

	public void add(PonderTag tag, ResourceLocation item) {
		tags.put(item, tag);
	}

	public void add(PonderTag tag, PonderChapter chapter) {
		chapterTags.put(chapter, tag);
	}

	public ItemBuilder forItems(ResourceLocation... items) {
		return new ItemBuilder(items);
	}

	public TagBuilder forTag(PonderTag tag) {
		return new TagBuilder(tag);
	}

	public static class ItemBuilder {

		private final Collection<ResourceLocation> items;

		private ItemBuilder(ResourceLocation... items) {
			this.items = Arrays.asList(items);
		}

		public ItemBuilder add(PonderTag tag) {
			items.forEach(i -> PonderRegistry.tags.add(tag, i));
			return this;
		}

	}

	public static class TagBuilder {

		private final PonderTag tag;

		private TagBuilder(PonderTag tag) {
			this.tag = tag;
		}

		public TagBuilder add(ResourceLocation item) {
			PonderRegistry.tags.add(tag, item);
			return this;
		}
	}
}
