package com.simibubi.create;

import java.nio.file.InvalidPathException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.tuple.Pair;

import com.simibubi.create.modules.contraptions.base.KineticBlock;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.BooleanValue;
import net.minecraftforge.common.ForgeConfigSpec.Builder;
import net.minecraftforge.common.ForgeConfigSpec.ConfigValue;
import net.minecraftforge.common.ForgeConfigSpec.DoubleValue;
import net.minecraftforge.common.ForgeConfigSpec.IntValue;

public class CreateConfig {

	public static final ForgeConfigSpec specification;
	public static final CreateConfig parameters;

	static {
		final Pair<CreateConfig, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(CreateConfig::new);

		specification = specPair.getRight();
		parameters = specPair.getLeft();
	}

	// Modules
	public BooleanValue enableSchematics;
	public BooleanValue enableCuriosities;
	public BooleanValue enableContraptions;
	public BooleanValue enablePalettes;
	public BooleanValue enableLogistics;
	public BooleanValue enableGardens;

	// Damage Control
	public BooleanValue freezeRotationPropagator;
	public BooleanValue freezeCrushing;
	public BooleanValue freezeInWorldProcessing;
	public BooleanValue freezeRotationConstructs;
	public BooleanValue freezePistonConstructs;
	public BooleanValue freezeExtractors;

	// Schematics
	public IntValue maxSchematics, maxTotalSchematicSize, maxSchematicPacketSize, schematicIdleTimeout;
	public IntValue schematicannonDelay, schematicannonSkips;
	public DoubleValue schematicannonGunpowderWorth, schematicannonFuelUsage;
	public ConfigValue<String> schematicPath;

	// Curiosities
	public IntValue maxSymmetryWandRange;
	public BooleanValue allowGlassPanesInPartialBlocks;

	// Contraptions
	public IntValue maxBeltLength, crushingDamage, maxMotorSpeed, maxRotationSpeed;
	public IntValue fanMaxPushDistance, fanMaxPullDistance, fanBlockCheckRate, fanRotationArgmax, generatingFanSpeed,
			inWorldProcessingTime;
	public IntValue maxChassisForTranslation, maxChassisForRotation, maxChassisRange, maxPistonPoles;

	public Map<ResourceLocation, DoubleValue> stressCapacityEntries = new HashMap<>();
	public Map<ResourceLocation, DoubleValue> stressEntries = new HashMap<>();

	public DoubleValue mediumSpeed, fastSpeed;
	public DoubleValue mediumStressImpact, highStressImpact;
	public DoubleValue mediumCapacity, highCapacity;

	// Logistics
	public IntValue extractorDelay, extractorAmount, linkRange;

	// Gardens
	public DoubleValue cocoaLogGrowthSpeed;

	CreateConfig(final ForgeConfigSpec.Builder builder) {
		initGeneral(builder);
		initContraptions(builder);
		initSchematics(builder);
		initCuriosities(builder);
		initLogistics(builder);
		initGardens(builder);
	}

	private void initGeneral(Builder builder) {
		builder.comment(
				"Configure which Modules should be accessible. This only affects Creative Menus and Recipes - any blocks and items already present will not stop working or disappear.")
				.push("modules");
		String basePath = "create.config.modules.";
		String name = "";

		name = "enableSchematics";
		enableSchematics = builder.translation(basePath + name).define(name, true);

		name = "enableContraptions";
		enableContraptions = builder.translation(basePath + name).define(name, true);

		name = "enableCuriosities";
		enableCuriosities = builder.translation(basePath + name).define(name, true);

		name = "enableGardens";
		enableGardens = builder.translation(basePath + name).define(name, true);

		name = "enableLogistics";
		enableLogistics = builder.translation(basePath + name).define(name, true);

		name = "enablePalettes";
		enablePalettes = builder.translation(basePath + name).define(name, true);

		builder.pop();
		builder.comment("In case of repeated crashing, you can inhibit related game mechanics for Troubleshooting.")
				.push("damageControl");
		basePath = "create.config.damageControl.";

		name = "freezeCrushing";
		freezeCrushing = builder.comment("", "In case Crushing Wheels crushed your server.")
				.translation(basePath + name).define(name, false);

		name = "freezeExtractors";
		freezeExtractors = builder.comment("", "In case Extractors pulled the plug.").translation(basePath + name)
				.define(name, false);

		name = "freezeInWorldProcessing";
		freezeInWorldProcessing = builder.comment("", "In case Encased Fans tried smelting your hardware.")
				.translation(basePath + name).define(name, false);

		name = "freezeRotationPropagator";
		freezeRotationPropagator = builder
				.comment("", "Pauses rotation logic altogether - Use if crash mentions RotationPropagators.")
				.translation(basePath + name).define(name, false);

		name = "freezeRotationConstructs";
		freezeRotationConstructs = builder.comment("", "In case Mechanical Bearings turned against you.")
				.translation(basePath + name).define(name, false);

		name = "freezePistonConstructs";
		freezePistonConstructs = builder.comment("", "In case Mechanical Pistons pushed it too far.")
				.translation(basePath + name).define(name, false);

		builder.pop();
	}

	private void initGardens(Builder builder) {
		builder.comment("The Gardens Module").push("gardens");
		String basePath = "create.config.gardens.";
		String name = "";

		name = "cocoaLogGrowthSpeed";
		cocoaLogGrowthSpeed = builder.comment("", "% of random Ticks causing a Cocoa log to grow.")
				.translation(basePath + name).defineInRange(name, 20D, 0D, 100D);

		builder.pop();
	}

	private void initLogistics(Builder builder) {
		builder.comment("The Logistics Module").push("logistics");
		String basePath = "create.config.logistics.";
		String name = "";

		name = "extractorDelay";
		extractorDelay = builder
				.comment("", "The amount of game ticks an Extractor waits after pulling an item successfully.")
				.translation(basePath + name).defineInRange(name, 20, 1, Integer.MAX_VALUE);

		name = "extractorAmount";
		extractorAmount = builder
				.comment("", "The amount of items an extractor pulls at a time without an applied filter.")
				.translation(basePath + name).defineInRange(name, 16, 1, 64);

		name = "linkRange";
		linkRange = builder.comment("", "Maximum possible range in blocks of redstone link connections.")
				.translation(basePath + name).defineInRange(name, 128, 4, Integer.MAX_VALUE);

		builder.pop();
	}

	private void initContraptions(Builder builder) {
		builder.comment("The Contraptions Module").push("contraptions");
		String basePath = "create.config.contraptions.";
		String name = "";

		name = "maxBeltLength";
		maxBeltLength = builder.comment("", "Maximum length in blocks of mechanical belts.")
				.translation(basePath + name).defineInRange(name, 20, 5, Integer.MAX_VALUE);

		name = "crushingDamage";
		crushingDamage = builder.comment("", "Damage dealt by active Crushing Wheels.").translation(basePath + name)
				.defineInRange(name, 4, 0, Integer.MAX_VALUE);

		{
			builder.comment("Encased Fan").push("encasedFan");
			basePath = "create.config.contraptions.encasedFan";

			name = "fanBlockCheckRate";
			fanBlockCheckRate = builder
					.comment("", "Game ticks between Fans checking for anything blocking their air flow.")
					.translation(basePath + name).defineInRange(name, 100, 20, Integer.MAX_VALUE);

			name = "fanMaxPushDistance";
			fanMaxPushDistance = builder.comment("", "Maximum distance in blocks Fans can push entities.")
					.translation(basePath + name).defineInRange(name, 20, 1, Integer.MAX_VALUE);

			name = "fanMaxPullDistance";
			fanMaxPullDistance = builder.comment("", "Maximum distance in blocks from where Fans can pull entities.")
					.translation(basePath + name).defineInRange(name, 5, 1, Integer.MAX_VALUE);

			name = "fanRotationArgmax";
			fanRotationArgmax = builder.comment("", "Rotation speed at which the maximum stats of fans are reached.")
					.translation(basePath + name).defineInRange(name, 8192, 64, Integer.MAX_VALUE);

			name = "generatingFanSpeed";
			generatingFanSpeed = builder.comment("", "Rotation speed generated by a vertical fan above fire.")
					.translation(basePath + name).defineInRange(name, 32, 0, Integer.MAX_VALUE);

			name = "inWorldProcessingTime";
			inWorldProcessingTime = builder
					.comment("", "Game ticks required for a Fan-based processing recipe to take effect.")
					.translation(basePath + name).defineInRange(name, 150, 0, Integer.MAX_VALUE);

			builder.pop();
		}

		{
			builder.comment("Mechanical Pistons and Bearings").push("constructs");
			basePath = "create.config.contraptions.constructs.";

			name = "maxChassisRange";
			maxChassisRange = builder.comment("", "Maximum value of a chassis attachment range.")
					.translation(basePath + name).defineInRange(name, 16, 1, Integer.MAX_VALUE);

			name = "maxChassisForRotation";
			maxChassisForRotation = builder
					.comment("", "Maximum amount of chassis blocks movable by a Mechanical Bearing.")
					.translation(basePath + name).defineInRange(name, 16, 1, Integer.MAX_VALUE);

			name = "maxChassisForTranslation";
			maxChassisForTranslation = builder
					.comment("", "Maximum amount of chassis blocks movable by a Mechanical Piston.")
					.translation(basePath + name).defineInRange(name, 16, 1, Integer.MAX_VALUE);

			name = "maxPistonPoles";
			maxPistonPoles = builder.comment("", "Maximum amount of extension poles behind a Mechanical Piston.")
					.translation(basePath + name).defineInRange(name, 64, 1, Integer.MAX_VALUE);

			initStress(builder);

			builder.pop();
		}

		name = "maxMotorSpeed";
		maxMotorSpeed = builder.comment("", "Maximum allowed speed of a configurable motor.")
				.translation(basePath + name).defineInRange(name, 4096, 64, Integer.MAX_VALUE);

		name = "maxRotationSpeed";
		maxRotationSpeed = builder.comment("", "Maximum allowed rotation speed for any Kinetic Tile.")
				.translation(basePath + name).defineInRange(name, 16384, 64, Integer.MAX_VALUE);

		builder.pop();
	}

	private void initCuriosities(Builder builder) {
		builder.comment("The Curiosities Module").push("curiosities");
		String basePath = "create.config.curiosities.";
		String name = "";

		name = "maxSymmetryWandRange";
		maxSymmetryWandRange = builder
				.comment("", "The Maximum Distance to an active mirror for the symmetry wand to trigger.")
				.translation(basePath + name).defineInRange(name, 50, 10, Integer.MAX_VALUE);

		name = "allowGlassPanesInPartialBlocks";
		allowGlassPanesInPartialBlocks = builder
				.comment("", "Allow Glass Panes to be put inside Blocks like Stairs, Slabs, Fences etc.")
				.translation(basePath + name).define(name, true);

		builder.pop();
	}

	private void initSchematics(final ForgeConfigSpec.Builder builder) {
		builder.comment("The Schematics Module").push("schematics");
		String basePath = "create.config.schematics.";
		String name = "";

		name = "maxSchematics";
		maxSchematics = builder
				.comment("", "The amount of Schematics a player can upload until previous ones are overwritten.")
				.translation(basePath + name).defineInRange(name, 10, 1, Integer.MAX_VALUE);

		name = "schematicPath";
		schematicPath = builder.comment("", "The file location where uploaded Schematics are stored.").define(name,
				"schematics/uploaded", this::isValidPath);

		name = "maxTotalSchematicSize";
		maxTotalSchematicSize = builder
				.comment("", "[in KiloBytes]", "The maximum allowed file size of uploaded Schematics.")
				.translation(basePath + name).defineInRange(name, 256, 16, Integer.MAX_VALUE);

		name = "maxSchematicPacketSize";
		maxSchematicPacketSize = builder
				.comment("", "[in Bytes]", "The maximum packet size uploaded Schematics are split into.")
				.translation(basePath + name).defineInRange(name, 1024, 256, 32767);

		name = "schematicIdleTimeout";
		schematicIdleTimeout = builder.comment("",
				"Amount of game ticks without new packets arriving until an active schematic upload process is discarded.")
				.translation(basePath + name).defineInRange(name, 600, 100, Integer.MAX_VALUE);

		{
			builder.comment("Schematicannon").push("schematicannon");
			basePath = "create.config.schematics.schematicannon";

			name = "schematicannonDelay";
			schematicannonDelay = builder
					.comment("", "Amount of game ticks between shots of the cannon. Higher => Slower")
					.translation(basePath + name).defineInRange(name, 10, 1, Integer.MAX_VALUE);

			name = "schematicannonSkips";
			schematicannonSkips = builder
					.comment("", "Amount of block positions per tick scanned by a running cannon. Higher => Faster")
					.translation(basePath + name).defineInRange(name, 10, 1, Integer.MAX_VALUE);

			name = "schematicannonGunpowderWorth";
			schematicannonGunpowderWorth = builder.comment("", "% of Schematicannon's Fuel filled by 1 Gunpowder.")
					.translation(basePath + name).defineInRange(name, 20D, 0D, 100D);

			name = "schematicannonFuelUsage";
			schematicannonFuelUsage = builder.comment("", "% of Schematicannon's Fuel used for each fired block.")
					.translation(basePath + name).defineInRange(name, 0.05D, 0D, 100D);
			builder.pop();
		}

		builder.pop();
	}

	private void initStress(final ForgeConfigSpec.Builder builder) {
		builder.comment("Configure speed/capacity levels for requirements and indicators.").push("rotationLevels");
		String basePath = "create.config.rotationLevels.";
		String name = "";

		name = "mediumSpeed";
		mediumSpeed = builder.comment("", "[in Degrees/Tick]", "Minimum speed of rotation to be considered 'medium'")
				.translation(basePath + name).defineInRange(name, 32D, 0D, 4096D);

		name = "fastSpeed";
		mediumSpeed = builder.comment("", "[in Degrees/Tick]", "Minimum speed of rotation to be considered 'fast'")
				.translation(basePath + name).defineInRange(name, 512D, 0D, 65535D);

		name = "mediumStressImpact";
		mediumStressImpact = builder.comment("", "Minimum stress impact to be considered 'medium'")
				.translation(basePath + name).defineInRange(name, 8D, 0D, 4096D);

		name = "highStressImpact";
		highStressImpact = builder.comment("", "Minimum stress impact to be considered 'high'")
				.translation(basePath + name).defineInRange(name, 32D, 0D, 65535D);

		name = "mediumCapacity";
		mediumCapacity = builder.comment("", "Minimum added Capacity by sources to be considered 'medium'")
				.translation(basePath + name).defineInRange(name, 128D, 0D, 4096D);

		name = "highCapacity";
		highCapacity = builder.comment("", "Minimum added Capacity by sources to be considered 'high'")
				.translation(basePath + name).defineInRange(name, 512D, 0D, 65535D);

		builder.pop();

		builder.comment(
				"Configure the individual stress impact of mechanical blocks. Note that this cost is doubled for every speed increase it receives.")
				.push("stress");

		for (AllBlocks block : AllBlocks.values()) {
			if (block.get() instanceof KineticBlock)
				initStressEntry(block, builder);
		}

		builder.pop();

		builder.comment("Configure how much stress a source can accommodate.").push("capacity");

		for (AllBlocks block : AllBlocks.values()) {
			if (block.get() instanceof KineticBlock)
				initStressCapacityEntry(block, builder);
		}

		builder.pop();

	}

	private void initStressEntry(AllBlocks block, final ForgeConfigSpec.Builder builder) {
		String basePath = "create.config.stress.";
		String name = block.name();
		stressEntries.put(block.get().getRegistryName(), builder.comment("").translation(basePath + name)
				.defineInRange(name, getDefaultStressImpact(block), 0, 2048));
	}

	private void initStressCapacityEntry(AllBlocks block, final ForgeConfigSpec.Builder builder) {
		double defaultStressCapacity = getDefaultStressCapacity(block);
		if (defaultStressCapacity == -1)
			return;
		String basePath = "create.config.stressCapacity.";
		String name = block.name();
		stressCapacityEntries.put(block.get().getRegistryName(),
				builder.comment("").translation(basePath + name).defineInRange(name, defaultStressCapacity, 0, 4096D));
	}

	public static double getDefaultStressCapacity(AllBlocks block) {

		switch (block) {
		case MOTOR:
			return 1024;
		case ENCASED_FAN:
			return 64;
		case WATER_WHEEL:
			return 32;
		case MECHANICAL_BEARING:
			return 128;
		default:
			return -1;
		}
	}

	public static double getDefaultStressImpact(AllBlocks block) {

		switch (block) {
		case CRUSHING_WHEEL:
		case MECHANICAL_PRESS:
		case MOTOR:
			return 32;

		case DRILL:
		case SAW:
		case MECHANICAL_PISTON:
		case STICKY_MECHANICAL_PISTON:
			return 16;

		case ENCASED_FAN:
		case MECHANICAL_MIXER:
		case MECHANICAL_BEARING:
		case MECHANICAL_CRAFTER:
			return 8;

		case WATER_WHEEL:
		case TURNTABLE:
		case GEARBOX:
		case GEARSHIFT:
		case LARGE_COGWHEEL:
			return 4;

		case CLUTCH:
			return 2;

		case BELT:
		case COGWHEEL:
		case ENCASED_BELT:
		case ENCASED_SHAFT:
		case SHAFT:
		default:
			return 1;
		}
	}

	private boolean isValidPath(Object path) {
		if (!(path instanceof String))
			return false;
		try {
			Paths.get((String) path);
			return true;
		} catch (InvalidPathException e) {
			return false;
		}
	}

}
