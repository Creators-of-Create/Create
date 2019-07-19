package com.simibubi.create.schematic.tools;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.google.common.collect.ImmutableList;
import com.simibubi.create.gui.ScreenResources;

public enum Tools {

	Deploy(new DeployTool(), "Deploy", ScreenResources.ICON_TOOL_DEPLOY, ImmutableList.of(
			"Moves the structure to a location.",
			"Right-Click on the ground to place.",
			"Hold [Ctrl] to select at a fixed distance.",
			"[Ctrl]-Scroll to change the distance."
			)),
	Move(new MoveTool(), "Move XZ", ScreenResources.ICON_TOOL_MOVE_XZ, ImmutableList.of(
			"Shifts the Schematic Horizontally",
			"Point at the Schematic and [CTRL]-Scroll to push it."
			)),
	MoveY(new MoveVerticalTool(), "Move Y", ScreenResources.ICON_TOOL_MOVE_Y, ImmutableList.of(
			"Shifts the Schematic Vertically",
			"[CTRL]-Scroll to move it up/down"
			)),
	Rotate(new RotateTool(), "Rotate", ScreenResources.ICON_TOOL_ROTATE, ImmutableList.of(
			"Rotates the Schematic around its center.",
			"[CTRL]-Scroll to rotate by 90 Degrees"
			)),
	Flip(new FlipTool(), "Flip", ScreenResources.ICON_TOOL_MIRROR, ImmutableList.of(
			"Flips the Schematic along the face you select.",
			"Point at the Schematic and [CTRL]-Scroll to flip it."
			));

	private ISchematicTool tool;
	private String displayName;
	private ScreenResources icon;
	private List<String> description;

	private Tools(ISchematicTool tool, String name, ScreenResources icon, List<String> description) {
		this.tool = tool;
		this.displayName = name;
		this.icon = icon;
		this.description = description;
	}

	public ISchematicTool getTool() {
		return tool;
	}

	public String getDisplayName() {
		return displayName;
	}

	public ScreenResources getIcon() {
		return icon;
	}

	public static List<Tools> getTools() {
		List<Tools> tools = new ArrayList<>();
		Collections.addAll(tools, Move, MoveY, Deploy, Rotate, Flip);
		return tools;
	}

	public List<String> getDescription() {
		return description;
	}

}
