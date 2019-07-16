package com.simibubi.create.schematic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.simibubi.create.gui.GuiResources;

public enum Tools {

	Deploy(new SchematicDeployTool(), "Deploy", GuiResources.ICON_TOOL_DEPLOY),
	
	Move(new SchematicMoveTool(), "Move XZ", GuiResources.ICON_TOOL_MOVE_XZ),
	MoveY(new SchematicMoveVerticalTool(), "Move Y", GuiResources.ICON_TOOL_MOVE_Y),
	Rotate(new SchematicRotateTool(), "Rotate", GuiResources.ICON_TOOL_ROTATE),
	Flip(new SchematicFlipTool(), "Flip", GuiResources.ICON_TOOL_MIRROR);
	
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
