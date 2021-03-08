package com.simibubi.create.foundation.ponder;

import com.simibubi.create.foundation.ponder.content.PonderTag;
import net.minecraft.util.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

public class PonderStoryBoardEntry {

	private final String schematicName;
	private final PonderStoryBoard board;
	private final List<PonderTag> tags;
	private final ResourceLocation component;

	public PonderStoryBoardEntry(PonderStoryBoard board, String schematicName, ResourceLocation component) {
		this.board = board;
		this.schematicName = schematicName;
		this.tags = new ArrayList<>();
		this.component = component;
	}

	public interface PonderStoryBoard {
		void program(SceneBuilder scene, SceneBuildingUtil util);
	}
	
	public String getSchematicName() {
		return schematicName;
	}
	
	public PonderStoryBoard getBoard() {
		return board;
	}

	public List<PonderTag> getTags() {
		return tags;
	}

	public ResourceLocation getComponent() {
		return component;
	}

}
