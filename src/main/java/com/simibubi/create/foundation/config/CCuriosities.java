package com.simibubi.create.foundation.config;

public class CCuriosities extends ConfigBase {

	public ConfigInt maxSymmetryWandRange = i(50, 10, "maxSymmetryWandRange", Comments.symmetryRange);
//	public ConfigInt zapperUndoLogLength = i(10, 0, "zapperUndoLogLength", Comments.zapperUndoLogLength); NYI

	@Override
	public String getName() {
		return "curiosities";
	}

	private static class Comments {
		static String symmetryRange = "The Maximum Distance to an active mirror for the symmetry wand to trigger.";
//		static String zapperUndoLogLength = "The maximum amount of operations, a blockzapper can remember for undoing. (0 to disable undo)";
	}

}
