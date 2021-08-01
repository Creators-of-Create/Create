package com.simibubi.create.foundation.ponder.content;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.simibubi.create.foundation.ponder.PonderStoryBoardEntry;
import com.simibubi.create.foundation.utility.Pair;

public class PonderChapterRegistry {

	private final Map<String, Pair<PonderChapter, List<PonderStoryBoardEntry>>> chapters;

	public PonderChapterRegistry() {
		chapters = new HashMap<>();
	}

	public void addStoriesToChapter(@Nonnull PonderChapter chapter, PonderStoryBoardEntry... entries) {
		List<PonderStoryBoardEntry> entryList = chapters.get(chapter.getId()).getSecond();
		synchronized (entryList) {
			entryList.addAll(Arrays.asList(entries));
		}
	}

	PonderChapter addChapter(@Nonnull PonderChapter chapter) {
		synchronized (chapters) {
			chapters.put(chapter.getId(), Pair.of(chapter, new ArrayList<>()));
		}
		return chapter;
	}

	@Nullable
	PonderChapter getChapter(String id) {
		Pair<PonderChapter, List<PonderStoryBoardEntry>> pair = chapters.get(id);
		if (pair == null)
			return null;

		return pair.getFirst();
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
