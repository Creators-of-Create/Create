package com.simibubi.create.infrastructure.config;


import net.createmod.catnip.config.ConfigBase;

public class CLogistics extends ConfigBase {

	public final ConfigInt defaultExtractionTimer = i(8, 1, "defaultExtractionTimer", Comments.defaultExtractionTimer);
	public final ConfigInt psiTimeout = i(60, 1, "psiTimeout", Comments.psiTimeout);
	public final ConfigInt mechanicalArmRange = i(5, 1, "mechanicalArmRange", Comments.mechanicalArmRange);
	public final ConfigInt packagePortRange = i(5, 1, "packagePortRange", Comments.packagePortRange);
	public final ConfigInt linkRange = i(256, 1, "linkRange", Comments.linkRange);
	public final ConfigInt displayLinkRange = i(64, 1, "displayLinkRange", Comments.displayLinkRange);
	public final ConfigInt vaultCapacity = i(20, 1, 2048, "vaultCapacity", Comments.vaultCapacity);
	public final ConfigInt chainConveyorCapacity = i(20, 1, "chainConveyorCapacity", Comments.chainConveyorCapacity);
	public final ConfigInt brassTunnelTimer = i(10, 1, 10, "brassTunnelTimer", Comments.brassTunnelTimer);
	public final ConfigInt factoryGaugeTimer = i(100, 5, "factoryGaugeTimer", Comments.factoryGaugeTimer);
	public final ConfigBool seatHostileMobs = b(true, "seatHostileMobs", Comments.seatHostileMobs);

	@Override
	public String getName() {
		return "logistics";
	}

	private static class Comments {
		static String defaultExtractionTimer =
			"The amount of ticks a funnel waits between item transferrals, when it is not re-activated by redstone.";
		static String linkRange = "Maximum possible range in blocks of redstone link connections.";
		static String displayLinkRange =
			"Maximum possible distance in blocks between display links and their target.";
		static String psiTimeout =
			"The amount of ticks a portable storage interface waits for transfers until letting contraptions move along.";
		static String mechanicalArmRange = "Maximum distance in blocks a Mechanical Arm can reach across.";
		static String packagePortRange = "Maximum distance in blocks a Package Port can be placed at from its target.";
		static String vaultCapacity = "The total amount of stacks a vault can hold per block in size.";
		static String chainConveyorCapacity = "The amount of packages a chain conveyor can carry at a time.";
		static String brassTunnelTimer = "The amount of ticks a brass tunnel waits between distributions.";
		static String factoryGaugeTimer = "The amount of ticks a factory gauge waits between requests.";
		static String seatHostileMobs = "Whether hostile mobs walking near a seat will start riding it.";
	}

}
