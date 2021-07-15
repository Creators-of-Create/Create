package com.simibubi.create.foundation.config;
import com.simibubi.create.foundation.config.ConfigBase.ConfigInt;


public class CFluids extends ConfigBase {

	public ConfigInt fluidTankCapacity = i(8, 1, "fluidTankCapacity", Comments.buckets, Comments.fluidTankCapacity);
	public ConfigInt fluidTankMaxHeight = i(32, 1, "fluidTankMaxHeight", Comments.blocks, Comments.fluidTankMaxHeight);
	public ConfigInt mechanicalPumpRange =
		i(16, 1, "mechanicalPumpRange", Comments.blocks, Comments.mechanicalPumpRange);

	public ConfigInt hosePulleyBlockThreshold = i(10000, -1, "hosePulleyBlockThreshold", Comments.blocks,
		Comments.toDisable, Comments.hosePulleyBlockThreshold);
	public ConfigInt hosePulleyRange = i(128, 1, "hosePulleyRange", Comments.blocks, Comments.hosePulleyRange);

	@Override
	public String getName() {
		return "fluids";
	}

	private static class Comments {
		static String blocks = "[in Blocks]";
		static String buckets = "[in Buckets]";
		static String fluidTankCapacity = "The amount of liquid a tank can hold per block.";
		static String fluidTankMaxHeight = "The maximum height a fluid tank can reach.";
		static String mechanicalPumpRange =
			"The maximum distance a mechanical pump can push or pull liquids on either side.";

		static String hosePulleyRange = "The maximum distance a hose pulley can draw fluid blocks from.";
		static String toDisable = "[-1 to disable this behaviour]";
		static String hosePulleyBlockThreshold =
			"The minimum amount of fluid blocks the hose pulley needs to find before deeming it an infinite source.";
	}

}
