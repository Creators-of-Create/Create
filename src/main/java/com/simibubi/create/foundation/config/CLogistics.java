package com.simibubi.create.foundation.config;
import com.simibubi.create.foundation.config.ConfigBase.ConfigInt;


public class CLogistics extends ConfigBase {

	public ConfigInt defaultExtractionLimit = i(64, 1, 64, "defaultExtractionLimit", Comments.defaultExtractionLimit);
	public ConfigInt defaultExtractionTimer = i(8, 1, "defaultExtractionTimer", Comments.defaultExtractionTimer);
	public ConfigInt psiTimeout = i(20, 1, "psiTimeout", Comments.psiTimeout);
	public ConfigInt mechanicalArmRange = i(5, 1, "mechanicalArmRange", Comments.mechanicalArmRange);
	public ConfigInt linkRange = i(128, 1, "linkRange", Comments.linkRange);

	@Override
	public String getName() {
		return "logistics";
	}

	private static class Comments {
		static String defaultExtractionLimit =
			"The maximum amount of items a funnel pulls at a time without an applied filter.";
		static String defaultExtractionTimer =
			"The amount of ticks a funnel waits between item transferrals, when it is not re-activated by redstone.";
		static String linkRange = "Maximum possible range in blocks of redstone link connections.";
		static String psiTimeout =
			"The amount of ticks a portable storage interface waits for transfers until letting contraptions move along.";
		static String mechanicalArmRange = "Maximum distance in blocks a Mechanical Arm can reach across.";
	}

}
