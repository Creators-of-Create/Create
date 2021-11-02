package com.simibubi.create.foundation.ponder.content;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import com.tterrag.registrate.util.entry.ItemProviderEntry;

import net.minecraft.util.IItemProvider;
import net.minecraft.util.ResourceLocation;

public class PonderTagRegistry {

	private final Multimap<ResourceLocation, PonderTag> tags;
	private final Multimap<PonderChapter, PonderTag> chapterTags;

	private final List<PonderTag> listedTags;

	public PonderTagRegistry() {
		tags = LinkedHashMultimap.create();
		chapterTags = LinkedHashMultimap.create();
		listedTags = new ArrayList<>();
	}

	public Set<PonderTag> getTags(ResourceLocation item) {
		return ImmutableSet.copyOf(tags.get(item));
	}

	public Set<PonderTag> getTags(PonderChapter chapter) {
		return ImmutableSet.copyOf(chapterTags.get(chapter));
	}

	public Set<ResourceLocation> getItems(PonderTag tag) {
		return tags.entries()
			.stream()
			.filter(e -> e.getValue() == tag)
			.map(Map.Entry::getKey)
			.collect(ImmutableSet.toImmutableSet());
	}

	public Set<PonderChapter> getChapters(PonderTag tag) {
		return chapterTags.entries()
			.stream()
			.filter(e -> e.getValue() == tag)
			.map(Map.Entry::getKey)
			.collect(ImmutableSet.toImmutableSet());
	}

	public List<PonderTag> getListedTags() {
		return listedTags;
	}

	public void listTag(PonderTag tag) {
		listedTags.add(tag);
	}

	public void add(PonderTag tag, ResourceLocation item) {
		synchronized (tags) {
			tags.put(item, tag);
		}
	}

	public void add(PonderTag tag, PonderChapter chapter) {
		synchronized (chapterTags) {
			chapterTags.put(chapter, tag);
		}
	}

	public ItemBuilder forItems(ResourceLocation... items) {
		return new ItemBuilder(items);
	}

	public TagBuilder forTag(PonderTag tag) {
		return new TagBuilder(tag);
	}

	public class ItemBuilder {

		private final Collection<ResourceLocation> items;

		private ItemBuilder(ResourceLocation... items) {
			this.items = Arrays.asList(items);
		}

		public ItemBuilder add(PonderTag tag) {
			items.forEach(i -> PonderTagRegistry.this.add(tag, i));
			return this;
		}

	}

	public class TagBuilder {

		private final PonderTag tag;

		private TagBuilder(PonderTag tag) {
			this.tag = tag;
		}

		public TagBuilder add(ResourceLocation item) {
			PonderTagRegistry.this.add(tag, item);
			return this;
		}

		public TagBuilder add(IItemProvider item) {
			return add(item.asItem()
				.getRegistryName());
		}

		public TagBuilder add(ItemProviderEntry<?> entry) {
			return add(entry.get());
		}

	}

}
