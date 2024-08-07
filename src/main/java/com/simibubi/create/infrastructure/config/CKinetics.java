package com.simibubi.create.infrastructure.config;

import com.simibubi.create.content.contraptions.ContraptionData;
import com.simibubi.create.content.contraptions.ContraptionMovementSetting;
import com.simibubi.create.foundation.config.ConfigBase;

public class CKinetics extends ConfigBase {

	public final ConfigBool disableStress = b(false, "disableStress", Comments.disableStress);
	public final ConfigInt maxBeltLength = i(20, 5, "maxBeltLength", Comments.maxBeltLength);
	public final ConfigInt crushingDamage = i(4, 0, "crushingDamage", Comments.crushingDamage);
	public final ConfigInt maxRotationSpeed = i(256, 64, "maxRotationSpeed", Comments.rpm, Comments.maxRotationSpeed);
	public final ConfigEnum<DeployerAggroSetting> ignoreDeployerAttacks =
		e(DeployerAggroSetting.CREEPERS, "ignoreDeployerAttacks", Comments.ignoreDeployerAttacks);
	public final ConfigInt kineticValidationFrequency =
		i(60, 5, "kineticValidationFrequency", Comments.kineticValidationFrequency);
	public final ConfigFloat crankHungerMultiplier = f(.01f, 0, 1, "crankHungerMultiplier", Comments.crankHungerMultiplier);
	public final ConfigInt minimumWindmillSails = i(8, 0, "minimumWindmillSails", Comments.minimumWindmillSails);
	public final ConfigInt windmillSailsPerRPM = i(8, 1, "windmillSailsPerRPM", Comments.windmillSailsPerRPM);
	public final ConfigInt maxEjectorDistance = i(32, 0, "maxEjectorDistance", Comments.maxEjectorDistance);
	public final ConfigInt ejectorScanInterval = i(120, 10, "ejectorScanInterval", Comments.ejectorScanInterval);

	public final ConfigGroup fan = group(1, "encasedFan", "Encased Fan");
	public final ConfigInt fanPushDistance = i(20, 5, "fanPushDistance", Comments.fanPushDistance);
	public final ConfigInt fanPullDistance = i(20, 5, "fanPullDistance", Comments.fanPullDistance);
	public final ConfigInt fanBlockCheckRate = i(30, 10, "fanBlockCheckRate", Comments.fanBlockCheckRate);
	public final ConfigInt fanRotationArgmax = i(256, 64, "fanRotationArgmax", Comments.rpm, Comments.fanRotationArgmax);
	public final ConfigInt fanProcessingTime = i(150, 0, "fanProcessingTime", Comments.fanProcessingTime);

	public final ConfigGroup contraptions = group(1, "contraptions", "Moving Contraptions");
	public final ConfigInt maxBlocksMoved = i(2048, 1, "maxBlocksMoved", Comments.maxBlocksMoved);
	public final ConfigInt maxDataSize =
		i(ContraptionData.DEFAULT_LIMIT, 0, "maxDataSize", Comments.bytes, Comments.maxDataDisable, Comments.maxDataSize, Comments.maxDataSize2);
	public final ConfigInt maxChassisRange = i(16, 1, "maxChassisRange", Comments.maxChassisRange);
	public final ConfigInt maxPistonPoles = i(64, 1, "maxPistonPoles", Comments.maxPistonPoles);
	public final ConfigInt maxRopeLength = i(256, 1, "maxRopeLength", Comments.maxRopeLength);
	public final ConfigInt maxCartCouplingLength = i(32, 1, "maxCartCouplingLength", Comments.maxCartCouplingLength);
	public final ConfigInt rollerFillDepth = i(12, 1, "rollerFillDepth", Comments.rollerFillDepth);
	public final ConfigBool survivalContraptionPickup = b(true, "survivalContraptionPickup", Comments.survivalContraptionPickup);
	public final ConfigEnum<ContraptionMovementSetting> spawnerMovement =
		e(ContraptionMovementSetting.NO_PICKUP, "movableSpawners", Comments.spawnerMovement);
	public final ConfigEnum<ContraptionMovementSetting> amethystMovement =
		e(ContraptionMovementSetting.NO_PICKUP, "amethystMovement", Comments.amethystMovement);
	public final ConfigEnum<ContraptionMovementSetting> obsidianMovement =
		e(ContraptionMovementSetting.UNMOVABLE, "movableObsidian", Comments.obsidianMovement);
	public final ConfigEnum<ContraptionMovementSetting> reinforcedDeepslateMovement =
		e(ContraptionMovementSetting.UNMOVABLE, "movableReinforcedDeepslate", Comments.reinforcedDeepslateMovement);
	public final ConfigBool moveItemsToStorage = b(true, "moveItemsToStorage", Comments.moveItemsToStorage);
	public final ConfigBool harvestPartiallyGrown = b(false, "harvestPartiallyGrown", Comments.harvestPartiallyGrown);
	public final ConfigBool harvesterReplants = b(true, "harvesterReplants", Comments.harvesterReplants);
	public final ConfigBool minecartContraptionInContainers =
		b(false, "minecartContraptionInContainers", Comments.minecartContraptionInContainers);
	public final ConfigBool stabiliseStableContraptions = b(false, "stabiliseStableContraptions", Comments.stabiliseStableContraptions, "[Technical]");

	public final ConfigGroup stats = group(1, "stats", Comments.stats);
	public final ConfigFloat mediumSpeed = f(30, 0, 4096, "mediumSpeed", Comments.rpm, Comments.mediumSpeed);
	public final ConfigFloat fastSpeed = f(100, 0, 65535, "fastSpeed", Comments.rpm, Comments.fastSpeed);
	public final ConfigFloat mediumStressImpact =
		f(4, 0, 4096, "mediumStressImpact", Comments.su, Comments.mediumStressImpact);
	public final ConfigFloat highStressImpact = f(8, 0, 65535, "highStressImpact", Comments.su, Comments.highStressImpact);
	public final ConfigFloat mediumCapacity = f(256, 0, 4096, "mediumCapacity", Comments.su, Comments.mediumCapacity);
	public final ConfigFloat highCapacity = f(1024, 0, 65535, "highCapacity", Comments.su, Comments.highCapacity);

	public final CStress stressValues = nested(1, CStress::new, Comments.stress);

	@Override
	public String getName() {
		return "kinetics";
	}

	private static class Comments {
		static String maxBeltLength = "Maximum length in blocks of mechanical belts.";
		static String crushingDamage = "Damage dealt by active Crushing Wheels.";
		static String maxRotationSpeed = "Maximum allowed rotation speed for any Kinetic Block.";
		static String fanPushDistance = "Maximum distance in blocks Fans can push entities.";
		static String fanPullDistance = "Maximum distance in blocks from where Fans can pull entities.";
		static String fanBlockCheckRate = "Game ticks between Fans checking for anything blocking their air flow.";
		static String fanRotationArgmax = "Rotation speed at which the maximum stats of fans are reached.";
		static String fanProcessingTime = "Game ticks required for a Fan-based processing recipe to take effect.";
		static String crankHungerMultiplier =
			"multiplier used for calculating exhaustion from speed when a crank is turned.";
		static String maxBlocksMoved =
			"Maximum amount of blocks in a structure movable by Pistons, Bearings or other means.";
		static String maxDataSize = "Maximum amount of data a contraption can have before it can't be synced with players.";
		static String maxDataSize2 = "Un-synced contraptions will not be visible and will not have collision.";
		static String maxDataDisable = "[0 to disable this limit]";
		static String maxChassisRange = "Maximum value of a chassis attachment range.";
		static String maxPistonPoles = "Maximum amount of extension poles behind a Mechanical Piston.";
		static String maxRopeLength = "Max length of rope available off a Rope Pulley.";
		static String maxCartCouplingLength = "Maximum allowed distance of two coupled minecarts.";
		static String rollerFillDepth = "Maximum depth of blocks filled in using a Mechanical Roller.";
		static String moveItemsToStorage =
			"Whether items mined or harvested by contraptions should be placed in their mounted storage.";
		static String harvestPartiallyGrown = "Whether harvesters should break crops that aren't fully grown.";
		static String harvesterReplants = "Whether harvesters should replant crops after harvesting.";
		static String stats = "Configure speed/capacity levels for requirements and indicators.";
		static String rpm = "[in Revolutions per Minute]";
		static String su = "[in Stress Units]";
		static String bytes = "[in Bytes]";
		static String mediumSpeed = "Minimum speed of rotation to be considered 'medium'";
		static String fastSpeed = "Minimum speed of rotation to be considered 'fast'";
		static String mediumStressImpact = "Minimum stress impact to be considered 'medium'";
		static String highStressImpact = "Minimum stress impact to be considered 'high'";
		static String mediumCapacity = "Minimum added Capacity by sources to be considered 'medium'";
		static String highCapacity = "Minimum added Capacity by sources to be considered 'high'";
		static String stress = "Fine tune the kinetic stats of individual components";
		static String ignoreDeployerAttacks = "Select what mobs should ignore Deployers when attacked by them.";
		static String disableStress = "Disable the Stress mechanic altogether.";
		static String kineticValidationFrequency =
			"Game ticks between Kinetic Blocks checking whether their source is still valid.";
		static String minimumWindmillSails =
			"Amount of sail-type blocks required for a windmill to assemble successfully.";
		static String windmillSailsPerRPM = "Number of sail-type blocks required to increase windmill speed by 1RPM.";
		static String maxEjectorDistance = "Max Distance in blocks a Weighted Ejector can throw";
		static String ejectorScanInterval =
			"Time in ticks until the next item launched by an ejector scans blocks for potential collisions";
		static String survivalContraptionPickup = "Whether minecart contraptions can be picked up in survival mode.";
		static String spawnerMovement = "Configure how Spawner blocks can be moved by contraptions.";
		static String amethystMovement = "Configure how Budding Amethyst can be moved by contraptions.";
		static String obsidianMovement = "Configure how Obsidian blocks can be moved by contraptions.";
		static String reinforcedDeepslateMovement = "Configure how Reinforced Deepslate blocks can be moved by contraptions.";
		static String minecartContraptionInContainers = "Whether minecart contraptions can be placed into container items.";
		static String stabiliseStableContraptions = "Whether stabilised bearings create a separated entity even on non-rotating contraptions.";
	}

	public enum DeployerAggroSetting {
		ALL, CREEPERS, NONE
	}

}
