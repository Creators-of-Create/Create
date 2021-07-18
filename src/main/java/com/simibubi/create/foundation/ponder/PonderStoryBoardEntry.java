package com.simibubi.create.foundation.ponder;

import java.util.ArrayList;
import java.util.List;

import com.simibubi.create.foundation.ponder.content.PonderTag;

import net.minecraft.util.ResourceLocation;

public class PonderStoryBoardEntry {

	private final PonderStoryBoard board;
	private final String namespace;
	private final String schematicPath;
	private final ResourceLocation component;
	private final List<PonderTag> tags;

	public PonderStoryBoardEntry(PonderStoryBoard board, String namespace, String schematicPath, ResourceLocation component) {
		this.board = board;
		this.namespace = namespace;
		this.schematicPath = schematicPath;
		this.component = component;
		this.tags = new ArrayList<>();
	}

	public PonderStoryBoard getBoard() {
		return board;
	}

	public String getNamespace() {
		return namespace;
	}

	public String getSchematicPath() {
		return schematicPath;
	}

	public ResourceLocation getComponent() {
		return component;
	}

	public List<PonderTag> getTags() {
		return tags;
	}

	public ResourceLocation getSchematicLocation() {
		return new ResourceLocation(namespace, schematicPath);
	}

	@FunctionalInterface
	public interface PonderStoryBoard {
		void program(SceneBuilder scene, SceneBuildingUtil util);
	}

}
