package com.simibubi.create.foundation.config;

public class CSchematics extends ConfigBase {

	public ConfigInt maxSchematics = i(10, 1, "maxSchematics", Comments.maxSchematics);
	public ConfigInt maxTotalSchematicSize = i(256, 16, "maxSchematics", Comments.kb, Comments.maxSize);
	public ConfigInt maxSchematicPacketSize =
		i(1024, 256, 32767, "maxSchematicPacketSize", Comments.b, Comments.maxPacketSize);
	public ConfigInt schematicIdleTimeout = i(600, 100, "schematicIdleTimeout", Comments.idleTimeout);

	public ConfigGroup schematicannon = group(0, "schematicannon", "Schematicannon");
	public ConfigInt schematicannonDelay = i(10, 1, "schematicannonDelay", Comments.delay);
	public ConfigInt schematicannonSkips = i(10, 1, "schematicannonSkips", Comments.skips);
	public ConfigFloat schematicannonGunpowderWorth = f(20, 0, 100, "schematicannonGunpowderWorth", Comments.gunpowderWorth);
	public ConfigFloat schematicannonFuelUsage = f(0.05f, 0, 100, "schematicannonFuelUsage", Comments.fuelUsage);

	@Override
	public String getName() {
		return "schematics";
	}

	private static class Comments {
		static String kb = "[in KiloBytes]";
		static String b = "[in Bytes]";
		static String maxSchematics =
			"The amount of Schematics a player can upload until previous ones are overwritten.";
		static String maxSize = "The maximum allowed file size of uploaded Schematics.";
		static String maxPacketSize = "The maximum packet size uploaded Schematics are split into.";
		static String idleTimeout =
			"Amount of game ticks without new packets arriving until an active schematic upload process is discarded.";
		static String delay = "Amount of game ticks between shots of the cannon. Higher => Slower";
		static String skips = "Amount of block positions per tick scanned by a running cannon. Higher => Faster";
		static String gunpowderWorth = "% of Schematicannon's Fuel filled by 1 Gunpowder.";
		static String fuelUsage = "% of Schematicannon's Fuel used for each fired block.";
	}

}
