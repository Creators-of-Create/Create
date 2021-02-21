package com.simibubi.create.foundation.ponder;

public class PonderStoryBoardEntry {

	private String schematicName;
	private PonderStoryBoard board;

	public PonderStoryBoardEntry(PonderStoryBoard board, String schematicName) {
		this.board = board;
		this.schematicName = schematicName;
	}

	public interface PonderStoryBoard {
		public abstract void program(SceneBuilder scene, SceneBuildingUtil util);
	}
	
	public String getSchematicName() {
		return schematicName;
	}
	
	public PonderStoryBoard getBoard() {
		return board;
	}

}
