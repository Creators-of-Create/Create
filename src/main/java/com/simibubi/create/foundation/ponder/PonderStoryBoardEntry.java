package com.simibubi.create.foundation.ponder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.minecraft.resources.ResourceLocation;

public class PonderStoryBoardEntry {

	private final PonderStoryBoard board;
	private final String namespace;
	private final ResourceLocation schematicLocation;
	private final ResourceLocation component;
	private final List<PonderTag> tags;

	public PonderStoryBoardEntry(PonderStoryBoard board, String namespace, ResourceLocation schematicLocation, ResourceLocation component) {
		this.board = board;
		this.namespace = namespace;
		this.schematicLocation = schematicLocation;
		this.component = component;
		this.tags = new ArrayList<>();
	}

	public PonderStoryBoardEntry(PonderStoryBoard board, String namespace, String schematicPath, ResourceLocation component) {
		this(board, namespace, new ResourceLocation(namespace, schematicPath), component);
	}

	public PonderStoryBoard getBoard() {
		return board;
	}

	public String getNamespace() {
		return namespace;
	}

	public ResourceLocation getSchematicLocation() {
		return schematicLocation;
	}

	public ResourceLocation getComponent() {
		return component;
	}

	public List<PonderTag> getTags() {
		return tags;
	}

	// Builder start

	public PonderStoryBoardEntry highlightTag(PonderTag tag) {
		tags.add(tag);
		return this;
	}

	public PonderStoryBoardEntry highlightTags(PonderTag... tags) {
		Collections.addAll(this.tags, tags);
		return this;
	}

	public PonderStoryBoardEntry highlightAllTags() {
		tags.add(PonderTag.HIGHLIGHT_ALL);
		return this;
	}

	public PonderStoryBoardEntry chapter(PonderChapter chapter) {
		PonderRegistry.CHAPTERS.addStoriesToChapter(chapter, this);
		return this;
	}

	public PonderStoryBoardEntry chapters(PonderChapter... chapters) {
		for (PonderChapter c : chapters)
			chapter(c);
		return this;
	}

	// Builder end

	@FunctionalInterface
	public interface PonderStoryBoard {
		void program(SceneBuilder scene, SceneBuildingUtil util);
	}

}
