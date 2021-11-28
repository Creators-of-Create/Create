package com.simibubi.create.foundation.ponder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.simibubi.create.foundation.utility.Pair;

import net.minecraft.resources.ResourceLocation;

public class PonderChapterRegistry {

	private final Map<ResourceLocation, Pair<PonderChapter, List<PonderStoryBoardEntry>>> chapters;

	public PonderChapterRegistry() {
		chapters = new HashMap<>();
	}

	PonderChapter addChapter(@Nonnull PonderChapter chapter) {
		synchronized (chapters) {
			chapters.put(chapter.getId(), Pair.of(chapter, new ArrayList<>()));
		}
		return chapter;
	}

	@Nullable
	PonderChapter getChapter(ResourceLocation id) {
		Pair<PonderChapter, List<PonderStoryBoardEntry>> pair = chapters.get(id);
		if (pair == null)
			return null;

		return pair.getFirst();
	}

	public void addStoriesToChapter(@Nonnull PonderChapter chapter, PonderStoryBoardEntry... entries) {
		List<PonderStoryBoardEntry> entryList = chapters.get(chapter.getId()).getSecond();
		synchronized (entryList) {
			Collections.addAll(entryList, entries);
		}
	}

	public List<PonderChapter> getAllChapters() {
		return chapters
				.values()
				.stream()
				.map(Pair::getFirst)
				.collect(Collectors.toList());
	}

	public List<PonderStoryBoardEntry> getStories(PonderChapter chapter) {
		return chapters.get(chapter.getId()).getSecond();
	}

}
