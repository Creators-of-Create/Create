package com.simibubi.create.config;

public class CLogistics extends ConfigBase {

	public ConfigInt extractorDelay = i(20, 10, "extractorDelay", Comments.extractorDelay);
	public ConfigInt extractorInventoryScanDelay = i(40, 10, "extractorInventoryScanDelay", Comments.extractorInventoryScanDelay);
	public ConfigInt extractorAmount = i(16, 1, 64, "extractorAmount", Comments.extractorAmount);
	public ConfigInt linkRange = i(128, 1, "extractorDelay", Comments.linkRange);
	
	@Override
	public String getName() {
		return "logistics";
	}

	private static class Comments {
		static String extractorDelay = "The amount of game ticks an Extractor waits after pulling an item successfully.";
		static String extractorInventoryScanDelay = "The amount of game ticks an Extractor waits before checking again if the attached inventory contains items to extract.";
		static String extractorAmount = "The amount of items an extractor pulls at a time without an applied filter.";
		static String linkRange = "Maximum possible range in blocks of redstone link connections.";
	}

}
