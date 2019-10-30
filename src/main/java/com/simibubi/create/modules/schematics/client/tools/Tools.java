package com.simibubi.create.modules.schematics.client.tools;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.simibubi.create.ScreenResources;
import com.simibubi.create.foundation.utility.Lang;

public enum Tools {

	Deploy(new DeployTool(), ScreenResources.I_TOOL_DEPLOY),
	Move(new MoveTool(), ScreenResources.I_TOOL_MOVE_XZ),
	MoveY(new MoveVerticalTool(), ScreenResources.I_TOOL_MOVE_Y),
	Rotate(new RotateTool(), ScreenResources.I_TOOL_ROTATE),
	Print(new PlaceTool(), ScreenResources.I_CONFIRM),
	Flip(new FlipTool(), ScreenResources.I_TOOL_MIRROR);

	private ISchematicTool tool;
	private ScreenResources icon;

	private Tools(ISchematicTool tool, ScreenResources icon) {
		this.tool = tool;
		this.icon = icon;
	}

	public ISchematicTool getTool() {
		return tool;
	}

	public String getDisplayName() {
		return Lang.translate("schematic.tool." + Lang.asId(name()));
	}

	public ScreenResources getIcon() {
		return icon;
	}

	public static List<Tools> getTools(boolean creative) {
		List<Tools> tools = new ArrayList<>();
		Collections.addAll(tools, Move, MoveY, Deploy, Rotate, Flip);
		if (creative)
			tools.add(Print);
		return tools;
	}

	public List<String> getDescription() {
		return Lang.translatedOptions("schematic.tool." + Lang.asId(name()) + ".description", "0", "1", "2", "3");
	}

}
