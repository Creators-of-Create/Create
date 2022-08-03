package com.simibubi.create.content.schematics.client.tools;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.simibubi.create.foundation.gui.AllIcons;
import com.simibubi.create.foundation.utility.Lang;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public enum Tools {

	Deploy(new DeployTool(), AllIcons.I_TOOL_DEPLOY),
	Move(new MoveTool(), AllIcons.I_TOOL_MOVE_XZ),
	MoveY(new MoveVerticalTool(), AllIcons.I_TOOL_MOVE_Y),
	Rotate(new RotateTool(), AllIcons.I_TOOL_ROTATE),
	Print(new PlaceTool(), AllIcons.I_CONFIRM),
	Flip(new FlipTool(), AllIcons.I_TOOL_MIRROR);

	private ISchematicTool tool;
	private AllIcons icon;

	private Tools(ISchematicTool tool, AllIcons icon) {
		this.tool = tool;
		this.icon = icon;
	}

	public ISchematicTool getTool() {
		return tool;
	}

	public MutableComponent getDisplayName() {
		return Lang.translateDirect("schematic.tool." + Lang.asId(name()));
	}

	public AllIcons getIcon() {
		return icon;
	}

	public static List<Tools> getTools(boolean creative) {
		List<Tools> tools = new ArrayList<>();
		Collections.addAll(tools, Move, MoveY, Deploy, Rotate, Flip);
		if (creative)
			tools.add(Print);
		return tools;
	}

	public List<Component> getDescription() {
		return Lang.translatedOptions("schematic.tool." + Lang.asId(name()) + ".description", "0", "1", "2", "3");
	}

}
