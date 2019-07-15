package com.simibubi.create.schematic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.simibubi.create.gui.GuiResources;

public enum Tools {

	Deploy(new SchematicDeployTool(), "Deploy", GuiResources.ICON_3x3),
	
	Move(new SchematicMoveTool(), "Move XZ", GuiResources.ICON_3x3),
	MoveY(new SchematicMoveVerticalTool(), "Move Y", GuiResources.ICON_3x3),
	Rotate(new SchematicRotateTool(), "Rotate", GuiResources.ICON_3x3),
	Flip(new SchematicFlipTool(), "Flip", GuiResources.ICON_3x3);
	
	private ISchematicTool tool;
	private String displayName;
	private GuiResources icon;
	
	private Tools(ISchematicTool tool, String name, GuiResources icon) {
		this.tool = tool;
		this.displayName = name;
		this.icon = icon;
	}
	
	public ISchematicTool getTool() {
		return tool;
	}
	
	public String getDisplayName() {
		return displayName;
	}
	
	public GuiResources getIcon() {
		return icon;
	}
	
	public static List<Tools> getTools() {
		List<Tools> tools = new ArrayList<>();
		Collections.addAll(tools, Move, MoveY, Deploy, Rotate, Flip);
		return tools;
	}
	
}
