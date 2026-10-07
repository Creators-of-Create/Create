package com.simibubi.create.content.contraptions.wrench;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.kinetics.base.DirectionalKineticBlock;
import com.simibubi.create.content.kinetics.base.HorizontalAxisKineticBlock;
import com.simibubi.create.content.kinetics.base.HorizontalKineticBlock;
import com.simibubi.create.content.kinetics.base.RotatedPillarKineticBlock;
import com.simibubi.create.content.kinetics.transmission.sequencer.SequencedGearshiftBlock;
import com.simibubi.create.content.redstone.DirectedDirectionalBlock;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.HopperBlock;
import net.minecraft.world.level.block.state.properties.Property;

public class RadialWrenchRegistry {
	public static final Map<Property<?>, String> VALID_PROPERTIES = new HashMap<>();

	static {
		registerRotationProperty(RotatedPillarKineticBlock.AXIS, "Axis");
		registerRotationProperty(DirectionalKineticBlock.FACING, "Facing");
		registerRotationProperty(HorizontalAxisKineticBlock.HORIZONTAL_AXIS, "Axis");
		registerRotationProperty(HorizontalKineticBlock.HORIZONTAL_FACING, "Facing");
		registerRotationProperty(HopperBlock.FACING, "Facing");
		registerRotationProperty(DirectedDirectionalBlock.TARGET, "Target");

		registerRotationProperty(SequencedGearshiftBlock.VERTICAL, "Vertical");
	}

	public static final Set<ResourceLocation> BLOCK_BLACKLIST = new HashSet<>();

	static {
		registerBlacklistedBlock(AllBlocks.LARGE_WATER_WHEEL.getId());
		registerBlacklistedBlock(AllBlocks.WATER_WHEEL_STRUCTURAL.getId());
	}

	public static void registerRotationProperty(Property<?> property, String label) {
		if (VALID_PROPERTIES.containsKey(property))
			return;

		VALID_PROPERTIES.put(property, label);
	}

	public static void registerBlacklistedBlock(ResourceLocation location) {
		if (BLOCK_BLACKLIST.contains(location))
			return;

		BLOCK_BLACKLIST.add(location);
	}
}
