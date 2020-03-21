package com.simibubi.create.config;

import java.util.HashMap;
import java.util.Map;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.foundation.utility.Lang;
import com.simibubi.create.modules.contraptions.base.KineticBlock;
import com.simibubi.create.modules.contraptions.components.flywheel.engine.EngineBlock;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.Builder;
import net.minecraftforge.common.ForgeConfigSpec.ConfigValue;

public class CStress extends ConfigBase {

	public Map<ResourceLocation, ConfigValue<Double>> capacities = new HashMap<>();
	public Map<ResourceLocation, ConfigValue<Double>> impacts = new HashMap<>();

	@Override
	protected void registerAll(Builder builder) {
		builder.comment("", Comments.su, Comments.impact).push("impact");
		for (AllBlocks block : AllBlocks.values())
			if (block.get() instanceof KineticBlock)
				initStressEntry(block, builder);
		builder.pop();

		builder.comment("", Comments.su, Comments.capacity).push("capacity");
		for (AllBlocks block : AllBlocks.values())
			if (block.get() instanceof KineticBlock || block.get() instanceof EngineBlock)
				initStressCapacityEntry(block, builder);
		builder.pop();
	}

	private void initStressEntry(AllBlocks block, final ForgeConfigSpec.Builder builder) {
		String name = Lang.asId(block.name());
		double defaultStressImpact = StressConfigDefaults.getDefaultStressImpact(block);
		impacts.put(block.get().getRegistryName(), builder.define(name, defaultStressImpact));
	}

	private void initStressCapacityEntry(AllBlocks block, final ForgeConfigSpec.Builder builder) {
		double defaultStressCapacity = StressConfigDefaults.getDefaultStressCapacity(block);
		if (defaultStressCapacity == -1)
			return;
		String name = Lang.asId(block.name());
		capacities.put(block.get().getRegistryName(), builder.define(name, defaultStressCapacity));
	}

	@Override
	public String getName() {
		return "stressValues.v" + StressConfigDefaults.forcedUpdateVersion;
	}

	private static class Comments {
		static String su = "[in Stress Units]";
		static String impact =
			"Configure the individual stress impact of mechanical blocks. Note that this cost is doubled for every speed increase it receives.";
		static String capacity = "Configure how much stress a source can accommodate for.";
	}

}
