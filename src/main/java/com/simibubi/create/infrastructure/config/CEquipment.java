package com.simibubi.create.infrastructure.config;

import com.simibubi.create.foundation.config.ConfigBase;

public class CEquipment extends ConfigBase {

	public final ConfigInt maxSymmetryWandRange = i(50, 10, "maxSymmetryWandRange", Comments.symmetryRange);
	public final ConfigInt placementAssistRange = i(12, 3, "placementAssistRange", Comments.placementRange);
	public final ConfigInt toolboxRange = i(10, 1, "toolboxRange", Comments.toolboxRange);
	public final ConfigInt airInBacktank = i(900, 1, "airInBacktank", Comments.maxAirInBacktank);
	public final ConfigInt enchantedBacktankCapacity = i(300, 1, "enchantedBacktankCapacity", Comments.enchantedBacktankCapacity);

	public final ConfigInt maxExtendoGripActions = i(1000, 0, "maxExtendoGripActions", Comments.maxExtendoGripActions);
	public final ConfigInt maxPotatoCannonShots = i(200, 0, "maxPotatoCannonShots", Comments.maxPotatoCannonShots);

//	public ConfigInt zapperUndoLogLength = i(10, 0, "zapperUndoLogLength", Comments.zapperUndoLogLength); NYI

	@Override
	public String getName() {
		return "equipment";
	}

	private static class Comments {
		static String symmetryRange = "The Maximum Distance to an active mirror for the symmetry wand to trigger.";
		static String maxAirInBacktank =
			"The Maximum volume of Air that can be stored in a backtank = Seconds of underwater breathing";
		static String enchantedBacktankCapacity =
			"The volume of Air added by each level of the backtanks Capacity Enchantment";
		static String placementRange =
			"The Maximum Distance a Block placed by Create's placement assist will have to its interaction point.";
		static String toolboxRange =
			"The Maximum Distance at which a Toolbox can interact with Players' Inventories.";
		static String maxExtendoGripActions =
			"Amount of free Extendo Grip actions provided by one filled Copper Backtank. Set to 0 makes Extendo Grips unbreakable";
		static String maxPotatoCannonShots =
			"Amount of free Potato Cannon shots provided by one filled Copper Backtank. Set to 0 makes Potato Cannons unbreakable";
//		static String zapperUndoLogLength = "The maximum amount of operations a blockzapper can remember for undoing. (0 to disable undo)";
	}

}
