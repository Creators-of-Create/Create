package com.simibubi.create.infrastructure.config;

import com.simibubi.create.content.fluids.transfer.FluidManipulationBehaviour.BottomlessFluidMode;
import com.simibubi.create.foundation.config.ConfigBase;

public class CFluids extends ConfigBase {

	public final ConfigInt fluidTankCapacity = i(8, 1, "fluidTankCapacity", Comments.buckets, Comments.fluidTankCapacity);
	public final ConfigInt fluidTankMaxHeight = i(32, 1, "fluidTankMaxHeight", Comments.blocks, Comments.fluidTankMaxHeight);
	public final ConfigInt mechanicalPumpRange =
		i(16, 1, "mechanicalPumpRange", Comments.blocks, Comments.mechanicalPumpRange);

	public final ConfigInt hosePulleyRange = i(128, 1, "hosePulleyRange", Comments.blocks, Comments.hosePulleyRange);
	public final ConfigInt hosePulleyBlockThreshold = i(10000, -1, "hosePulleyBlockThreshold", Comments.blocks,
		Comments.toDisable, Comments.hosePulleyBlockThreshold);
	public final ConfigBool fillInfinite = b(false, "fillInfinite", Comments.fillInfinite);
	public final ConfigEnum<BottomlessFluidMode> bottomlessFluidMode = e(BottomlessFluidMode.ALLOW_BY_TAG, "bottomlessFluidMode",
		Comments.bottomlessFluidMode);

	public ConfigBool placeFluidSourceBlocks = b(true, "placeFluidSourceBlocks", Comments.placeFluidSourceBlocks);

	@Override
	public String getName() {
		return "fluids";
	}

	private static class Comments {
		static String blocks = "[in Blocks]";
		static String buckets = "[in Buckets]";
		static String toDisable = "[-1 to disable this behaviour]";

		static String fluidTankCapacity = "The amount of liquid a tank can hold per block.";
		static String fluidTankMaxHeight = "The maximum height a fluid tank can reach.";
		static String mechanicalPumpRange =
			"The maximum distance a mechanical pump can push or pull liquids on either side.";

		static String hosePulleyRange = "The maximum distance a hose pulley can draw fluid blocks from.";
		static String hosePulleyBlockThreshold =
			"The minimum amount of fluid blocks the hose pulley needs to find before deeming it an infinite source.";
		static String fillInfinite = "Whether hose pulleys should continue filling up above-threshold sources.";
		static String bottomlessFluidMode = "Configure which fluids can be drained infinitely.";

		static String placeFluidSourceBlocks = "Whether open-ended pipes and hose pulleys should be allowed to place fluid sources.";
	}

}
